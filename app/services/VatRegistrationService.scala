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

import com.google.inject.ImplementedBy
import connectors.{KeystoreConnector, VatRegistrationConnector}
import enums.DownstreamOutcome
import enums.DownstreamOutcome._
import models.api._
import models.s4l.{S4LVatChoice, S4LVatFinancials}
import models.view._
import models.{CacheKey, ViewModelTransformer}
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

  private def update[C, G](c: Option[C], vs: VatScheme)(implicit vmTransformer: ViewModelTransformer[C, G]): G => G =
    g => c.map(vmTransformer.toApi(_, g)).getOrElse(g)

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
    submitTradingDetails |@| submitVatChoice |@| submitVatFinancials |@| submitSicAndCompliance map { case res@_ => Success }

  def submitVatFinancials()(implicit hc: HeaderCarrier): Future[VatFinancials] = {

    def mergeWithS4L(vs: VatScheme) =
      (s4l[EstimateVatTurnover]() |@|
        s4l[EstimateZeroRatedSales]() |@|
        s4l[VatChargeExpectancy]() |@|
        s4l[VatReturnFrequency]() |@|
        s4l[AccountingPeriod]() |@|
        s4l[CompanyBankAccountDetails]).map(S4LVatFinancials).map { vf =>
        (update(vf.estimateVatTurnover, vs) andThen update(vf.zeroRatedSalesEstimate, vs) andThen
          update(vf.vatChargeExpectancy, vs) andThen update(vf.vatReturnFrequency, vs) andThen
          update(vf.accountingPeriod, vs) andThen update(vf.companyBankAccountDetails, vs)) (vs.financials.getOrElse(VatFinancials.default))
      }

    for {
      vs <- getVatScheme()
      vatFinancials <- mergeWithS4L(vs)
      response <- vatRegConnector.upsertVatFinancials(vs.id, vatFinancials)
    } yield response
  }

  def submitSicAndCompliance()(implicit hc: HeaderCarrier): Future[SicAndCompliance] = {
    //revisit this; make it look like other `mergeWithS4L` once there's >1 thing coming from S4L
    def mergeWithS4L(vs: VatScheme) = s4l[BusinessActivityDescription]().map { description =>
      update(description, vs)
    }.map(_.apply(vs.sicAndCompliance.getOrElse(SicAndCompliance())))

    for {
      vs <- getVatScheme()
      businessActivityDescription <- mergeWithS4L(vs)
      response <- vatRegConnector.upsertSicAndCompliance(vs.id, businessActivityDescription)
    } yield response
  }

  def submitTradingDetails()(implicit hc: HeaderCarrier): Future[VatTradingDetails] = {
    //revisit this; make it look like other `mergeWithS4L` once there's >1 thing coming from S4L
    def mergeWithS4L(vs: VatScheme) = s4l[TradingName]().map { tn =>
      update(tn, vs)
    }.map(_.apply(vs.tradingDetails.getOrElse(VatTradingDetails())))

    for {
      vs <- getVatScheme()
      vatTradingDetails <- mergeWithS4L(vs)
      response <- vatRegConnector.upsertVatTradingDetails(vs.id, vatTradingDetails)
    } yield response
  }

  def submitVatChoice()(implicit hc: HeaderCarrier): Future[VatChoice] = {
    def mergeWithS4L(vs: VatScheme) =
      (s4l[StartDate]() |@| s4l[VoluntaryRegistration]()).map(S4LVatChoice).map { vc =>
        (update(vc.startDate, vs) andThen update(vc.voluntaryRegistration, vs)) (vs.vatChoice.getOrElse(VatChoice()))
      }

    for {
      vs <- getVatScheme()
      vatChoice <- mergeWithS4L(vs)
      response <- vatRegConnector.upsertVatChoice(vs.id, vatChoice)
    } yield response
  }

}
