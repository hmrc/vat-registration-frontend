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
import enums.{CacheKeys, DownstreamOutcome}
import models.api.{VatChoice, VatFinancials, VatScheme, VatTradingDetails}
import models.view._
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

  def getVatScheme()(implicit hc: HeaderCarrier): Future[VatScheme] =
    fetchRegistrationId.flatMap(vatRegConnector.getRegistration)

  def deleteVatScheme()(implicit hc: HeaderCarrier): Future[Boolean] =
    fetchRegistrationId.flatMap(vatRegConnector.deleteVatScheme)

  def assertRegistrationFootprint()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] = {
    for {
      vatScheme <- vatRegConnector.createNewRegistration()
      _ <- keystoreConnector.cache[String]("RegistrationId", vatScheme.id)
    } yield DownstreamOutcome.Success
  }

  def submitVatScheme()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] = {
    val tradingDetails = submitTradingDetails()
    val vatChoice = submitVatChoice()
    val financials = submitVatFinancials()
    for {
      _ <- tradingDetails
      _ <- vatChoice
      _ <- financials
    } yield DownstreamOutcome.Success
  }

  def submitVatFinancials()(implicit hc: HeaderCarrier): Future[VatFinancials] = {
    for {
      vatScheme <- getVatScheme()
      vatFinancials = vatScheme.financials.getOrElse(VatFinancials.empty)

      estimateVatTurnover <- s4LService.fetchAndGet[EstimateVatTurnover](CacheKeys.EstimateVatTurnover.toString)
      zeroRatedSalesEstimate <- s4LService.fetchAndGet[EstimateZeroRatedSales](CacheKeys.EstimateZeroRatedSales.toString)
      vatChargeExpectancy <- s4LService.fetchAndGet[VatChargeExpectancy](CacheKeys.VatChargeExpectancy.toString)
      vatReturnFrequency <- s4LService.fetchAndGet[VatReturnFrequency](CacheKeys.VatReturnFrequency.toString)
      accountingPeriod <- s4LService.fetchAndGet[AccountingPeriod](CacheKeys.AccountingPeriod.toString)

      estimateVatTurnoverVf = estimateVatTurnover.getOrElse(EstimateVatTurnover(vatScheme)).toApi(vatFinancials)
      zeroRatedSalesEstimateVf = zeroRatedSalesEstimate.getOrElse(EstimateZeroRatedSales(vatScheme)).toApi(estimateVatTurnoverVf)
      vatChargeExpectancyVf = vatChargeExpectancy.getOrElse(VatChargeExpectancy(vatScheme)).toApi(zeroRatedSalesEstimateVf)
      vatReturnFrequencyVf = vatReturnFrequency.getOrElse(VatReturnFrequency(vatScheme)).toApi(vatChargeExpectancyVf)
      accountingPeriodVf = accountingPeriod.getOrElse(AccountingPeriod(vatScheme)).toApi(vatReturnFrequencyVf)
      response <- vatRegConnector.upsertVatFinancials(vatScheme.id, zeroRatedSalesEstimateVf)
    } yield response
  }

  def submitTradingDetails()(implicit hc: HeaderCarrier): Future[VatTradingDetails] = {
    for {
      vatScheme <- getVatScheme()
      vatTradingDetails = vatScheme.tradingDetails.getOrElse(VatTradingDetails.empty)
      tradingName <- s4LService.fetchAndGet[TradingName](CacheKeys.TradingName.toString)
      tradingDetails = tradingName.getOrElse(TradingName(vatScheme)).toApi(vatTradingDetails)
      response <- vatRegConnector.upsertVatTradingDetails(vatScheme.id, tradingDetails)
    } yield response
  }

  def submitVatChoice()(implicit hc: HeaderCarrier): Future[VatChoice] = {
    for {
      vatScheme <- getVatScheme()
      vatChoice = vatScheme.vatChoice.getOrElse(VatChoice.empty)
      startDate <- s4LService.fetchAndGet[StartDate](CacheKeys.StartDate.toString)
      voluntaryRegistration <- s4LService.fetchAndGet[VoluntaryRegistration](CacheKeys.VoluntaryRegistration.toString)
      sdVatChoice = startDate.getOrElse(StartDate(vatScheme)).toApi(vatChoice)
      vrVatChoice = voluntaryRegistration.getOrElse(VoluntaryRegistration(vatScheme)).toApi(sdVatChoice)
      response <- vatRegConnector.upsertVatChoice(vatScheme.id, vrVatChoice)
    } yield response
  }

}
