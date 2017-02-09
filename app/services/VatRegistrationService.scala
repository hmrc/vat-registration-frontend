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
import models.api.{VatChoice, VatScheme, VatTradingDetails}
import models.view._
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[VatRegistrationService])
trait RegistrationService {

  def assertRegistrationFootprint()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value]
  def submitVatChoice(startDate: StartDate)(implicit hc: HeaderCarrier): Future[VatChoice]
  def submitTradingDetails(tradingName: TradingName)(implicit hc: HeaderCarrier): Future[VatTradingDetails]
  def getRegistrationSummary()(implicit hc: HeaderCarrier): Future[Summary]

}

class VatRegistrationService @Inject()(vatRegConnector: VatRegistrationConnector)
  extends RegistrationService
  with CommonService {

  override val keystoreConnector: KeystoreConnector = KeystoreConnector

  def assertRegistrationFootprint()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] = {
    for {
      vatScheme <- vatRegConnector.createNewRegistration()
      cache <- keystoreConnector.cache[String]("RegistrationId", vatScheme.id)
    } yield DownstreamOutcome.Success
  }

  def submitVatChoice(startDate: StartDate)(implicit hc: HeaderCarrier): Future[VatChoice] = {
    for {
      regId <- fetchRegistrationId
      response <- vatRegConnector.upsertVatChoice(regId, viewModelToVatChoice(startDate))
    } yield response
  }

  def submitTradingDetails(tradingName: TradingName)(implicit hc: HeaderCarrier): Future[VatTradingDetails] = {
    for {
      regId <- fetchRegistrationId
      response <- vatRegConnector.upsertVatTradingDetails(regId, viewModelToTradingDetails(tradingName))
    } yield response
  }

  private[services] def viewModelToVatChoice(startDate: StartDate): VatChoice = VatChoice(
      startDate = startDate.toDate,
      necessity = VatChoice.NECESSITY_VOLUNTARY // Until we play the 83k threshold story
    )

  private[services] def viewModelToTradingDetails(tradingName: TradingName): VatTradingDetails =
    VatTradingDetails(tradingName.toString)

  def getRegistrationSummary()(implicit hc: HeaderCarrier): Future[Summary] =
    for {
      regId <- fetchRegistrationId
      response <- vatRegConnector.getRegistration(regId)
    } yield registrationToSummary(response)

  def registrationToSummary(vatScheme: VatScheme): Summary = Summary(
      Seq(
        getVatDetailsSection(vatScheme.vatChoice),
        getCompanyDetailsSection(vatScheme.tradingDetails)
      )
    )

  private def getVatDetailsSection(vatChoice: VatChoice) = {

    def getRegisterVoluntarily: SummaryRow = SummaryRow(
        "vatDetails.registerVoluntarily",
        vatChoice.necessity match {
          case VatChoice.NECESSITY_VOLUNTARY => Right("Yes")
          case _ => Right("No")
        },
        Some(controllers.userJourney.routes.SummaryController.show())
      )

    def getStartDate: SummaryRow = SummaryRow(
        "vatDetails.startDate",
        Right(vatChoice.startDate.toString("d MMMM y")),
        Some(controllers.userJourney.routes.StartDateController.show())
      )

    SummarySection(
      id = "vatDetails",
      Seq(
        getRegisterVoluntarily,
        getStartDate
      )
    )
  }

  private def getCompanyDetailsSection(vatTradingDetails: VatTradingDetails) = SummarySection(
    id = "companyDetails",
    Seq(
      SummaryRow(
        "companyDetails.tradingName",
        Right(vatTradingDetails.tradingName),
        Some(controllers.userJourney.routes.TradingNameController.show())
      )
    )
  )
}


