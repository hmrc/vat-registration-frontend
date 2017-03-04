/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import javax.inject.Inject

import cats.data.State
import com.google.inject.ImplementedBy
import connectors.{KeystoreConnector, VatRegistrationConnector}
import enums.DownstreamOutcome
import enums.DownstreamOutcome._
import models.api._
import models.s4l.{S4LVatChoice, S4LVatFinancials}
import models.view._
import models.{ApiModelTransformer, CacheKey, ViewModelTransformer}
import play.api.libs.json.Format
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[VatRegistrationService])
trait RegistrationService {

  def assertRegistrationFootprint()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value]

  def submitVatScheme()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value]

  def submitTradingDetails()(implicit hc: HeaderCarrier): Future[VatTradingDetails]

  def submitVatChoice()(implicit hc: HeaderCarrier): Future[VatChoice]

  def getVatScheme()(implicit hc: HeaderCarrier): Future[VatScheme]

  def deleteVatScheme()(implicit hc: HeaderCarrier): Future[Boolean]
}

class VatRegistrationService @Inject()(s4LService: S4LService, vatRegConnector: VatRegistrationConnector)
  extends RegistrationService
    with CommonService {

  override val keystoreConnector: KeystoreConnector = KeystoreConnector

  import cats.instances.future._
  import cats.syntax.cartesian._

  private def s4l[T: Format : CacheKey]()(implicit headerCarrier: HeaderCarrier) = s4LService.fetchAndGet[T]()

  private def update[C, G](c: Option[C], vs: VatScheme)(implicit apiTransformer: ApiModelTransformer[C], vmTransformer: ViewModelTransformer[C, G]) =
    State[G, Unit](s => (vmTransformer.toApi(c.getOrElse(apiTransformer.toViewModel(vs)), s), ()))


  def getVatScheme()(implicit hc: HeaderCarrier): Future[VatScheme] =
    fetchRegistrationId.flatMap(vatRegConnector.getRegistration)

  def deleteVatScheme()(implicit hc: HeaderCarrier): Future[Boolean] =
    fetchRegistrationId.flatMap(vatRegConnector.deleteVatScheme)

  def assertRegistrationFootprint()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] =
    for {
      vatScheme <- vatRegConnector.createNewRegistration()
      _ <- keystoreConnector.cache[String]("RegistrationId", vatScheme.id)
    } yield Success

  def submitVatScheme()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] =
    submitTradingDetails |@| submitVatChoice |@| submitVatFinancials map { case res@_ => Success }

  def submitVatFinancials()(implicit hc: HeaderCarrier): Future[VatFinancials] = {

    def mergeWithS4L(vs: VatScheme) = {
      val vatFinancialsFromS4L = s4l[EstimateVatTurnover]() |@|
        s4l[EstimateZeroRatedSales]() |@|
        s4l[VatChargeExpectancy]() |@|
        s4l[VatReturnFrequency]() |@|
        s4l[AccountingPeriod]() |@|
        s4l[CompanyBankAccountDetails] map S4LVatFinancials

      vatFinancialsFromS4L.map { vf =>
        val updateOperation = for {
          _ <- update(vf.estimateVatTurnover, vs)
          _ <- update(vf.zeroRatedSalesEstimate, vs)
          _ <- update(vf.vatChargeExpectancy, vs)
          _ <- update(vf.vatReturnFrequency, vs)
          _ <- update(vf.accountingPeriod, vs)
          _ <- update(vf.companyBankAccountDetails, vs)
        } yield ()
        updateOperation.runS(vs.financials.getOrElse(VatFinancials.default)).value
      }
    }

    for {
      vs <- getVatScheme()
      vatFinancials <- mergeWithS4L(vs)
      response <- vatRegConnector.upsertVatFinancials(vs.id, vatFinancials)
    } yield response
  }

  def submitTradingDetails()(implicit hc: HeaderCarrier): Future[VatTradingDetails] = {
    //for now we only store trading name for trading details, so the below is total overkill
    //but trading details object will grow
    def mergeWithS4L(vs: VatScheme) = {
      s4l[TradingName]().map { tn =>
        val updateOperation = for {
          _ <- update(tn, vs)
        } yield ()
        updateOperation.runS(vs.tradingDetails.getOrElse(VatTradingDetails())).value
      }
    }

    for {
      vs <- getVatScheme()
      vatTradingDetails <- mergeWithS4L(vs)
      response <- vatRegConnector.upsertVatTradingDetails(vs.id, vatTradingDetails)
    } yield response
  }

  def submitVatChoice()(implicit hc: HeaderCarrier): Future[VatChoice] = {
    def mergeWithS4L(vs: VatScheme) = {
      val vatChoiceFromS4L =
        s4l[StartDate]() |@| s4l[VoluntaryRegistration]() map S4LVatChoice
      vatChoiceFromS4L.map { vc =>
        val updateOperation = for {
          _ <- update(vc.startDate, vs)
          _ <- update(vc.voluntaryRegistration, vs)
        } yield ()
        updateOperation.runS(vs.vatChoice.getOrElse(VatChoice())).value
      }
    }

    for {
      vs <- getVatScheme()
      vatChoice <- mergeWithS4L(vs)
      response <- vatRegConnector.upsertVatChoice(vs.id, vatChoice)
    } yield response
  }

}
