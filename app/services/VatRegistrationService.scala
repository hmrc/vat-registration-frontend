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
import enums.CacheKeys
import enums.DownstreamOutcome._
import models.api.{VatChoice, VatFinancials, VatScheme, VatTradingDetails}
import models.s4l.{S4LVatChoice, S4LVatFinancials}
import models.view._
import models.{ApiModelTransformer, ViewModelTransformer}
import play.api.libs.json.Format
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[VatRegistrationService])
trait RegistrationService {

  def assertRegistrationFootprint()(implicit hc: HeaderCarrier): Future[Value]

  def submitVatScheme()(implicit hc: HeaderCarrier): Future[Value]

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

  private def s4l[T: Format](cacheKey: CacheKeys.Value)(implicit headerCarrier: HeaderCarrier) =
    s4LService.fetchAndGet[T](cacheKey.toString)

  private def update[C, G](a: Option[C], vatScheme: => VatScheme, group: G)
                          (implicit apiTransformer: ApiModelTransformer[C], vmTransformer: ViewModelTransformer[C, G]): G =
    vmTransformer.toApi(a.getOrElse(apiTransformer.toViewModel(vatScheme)), group)

  def getVatScheme()(implicit hc: HeaderCarrier): Future[VatScheme] =
    fetchRegistrationId.flatMap(vatRegConnector.getRegistration)

  def deleteVatScheme()(implicit hc: HeaderCarrier): Future[Boolean] =
    fetchRegistrationId.flatMap(vatRegConnector.deleteVatScheme)

  def assertRegistrationFootprint()(implicit hc: HeaderCarrier): Future[Value] = {
    for {
      vatScheme <- vatRegConnector.createNewRegistration()
      _ <- keystoreConnector.cache[String]("RegistrationId", vatScheme.id)
    } yield Success
  }

  def submitVatScheme()(implicit hc: HeaderCarrier): Future[Value] =
    submitTradingDetails |@| submitVatChoice |@| submitVatFinancials map { case res@_ => Success }

  def submitVatFinancials()(implicit hc: HeaderCarrier): Future[VatFinancials] = {

    def vatFinancialsFromS4L =
      s4l[EstimateVatTurnover](CacheKeys.EstimateVatTurnover) |@|
        s4l[EstimateZeroRatedSales](CacheKeys.EstimateZeroRatedSales) |@|
        s4l[VatChargeExpectancy](CacheKeys.VatChargeExpectancy) |@|
        s4l[VatReturnFrequency](CacheKeys.VatReturnFrequency) |@|
        s4l[AccountingPeriod](CacheKeys.AccountingPeriod) map S4LVatFinancials

    for {
      vs <- getVatScheme()
      vfS4L <- vatFinancialsFromS4L
      vatFinancials = vs.financials.getOrElse(VatFinancials.empty)
      estimateVatTurnoverVf = update(vfS4L.estimateVatTurnover, vs, vatFinancials)
      zeroRatedSalesEstimateVf = update(vfS4L.zeroRatedSalesEstimate, vs, estimateVatTurnoverVf)
      vatChargeExpectancyVf = update(vfS4L.vatChargeExpectancy, vs, zeroRatedSalesEstimateVf)
      vatReturnFrequencyVf = update(vfS4L.vatReturnFrequency, vs, vatChargeExpectancyVf)
      accountingPeriodVf = update(vfS4L.accountingPeriod, vs, vatReturnFrequencyVf)
      response <- vatRegConnector.upsertVatFinancials(vs.id, accountingPeriodVf)
    } yield response
  }

  def submitTradingDetails()(implicit hc: HeaderCarrier): Future[VatTradingDetails] = {
    for {
      vs <- getVatScheme()
      tradingName <- s4l[TradingName](CacheKeys.TradingName)
      vatTradingDetails = vs.tradingDetails.getOrElse(VatTradingDetails())
      tradingDetails = update(tradingName, vs, vatTradingDetails)
      response <- vatRegConnector.upsertVatTradingDetails(vs.id, tradingDetails)
    } yield response
  }

  def submitVatChoice()(implicit hc: HeaderCarrier): Future[VatChoice] = {

    def vatChoiceFromS4L =
      s4l[StartDate](CacheKeys.StartDate) |@|
        s4l[VoluntaryRegistration](CacheKeys.VoluntaryRegistration) map S4LVatChoice

    for {
      vs <- getVatScheme()
      vcS4L <- vatChoiceFromS4L
      vatChoice = vs.vatChoice.getOrElse(VatChoice())
      sdVatChoice = update(vcS4L.startDate, vs, vatChoice)
      vrVatChoice = update(vcS4L.voluntaryRegistration, vs, sdVatChoice)
      response <- vatRegConnector.upsertVatChoice(vs.id, vrVatChoice)
    } yield response
  }

}
