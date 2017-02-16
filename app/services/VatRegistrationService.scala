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
import models.api.{VatChoice, VatScheme, VatTradingDetails}
import models.view._
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[VatRegistrationService])
trait RegistrationService {

  def assertRegistrationFootprint()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value]
  def submitVatScheme()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value]
  def submitTradingDetails()(implicit hc: HeaderCarrier): Future[VatTradingDetails]
  def submitVatChoice()(implicit hc: HeaderCarrier): Future[VatChoice]
  def getRegistrationSummary()(implicit hc: HeaderCarrier): Future[Summary]
  def getVatScheme()(implicit hc: HeaderCarrier): Future[VatScheme]
}

class VatRegistrationService @Inject() (s4LService: S4LService, vatRegConnector: VatRegistrationConnector, messagesApi: MessagesApi)
  extends RegistrationService
  with CommonService {

  override val keystoreConnector: KeystoreConnector = KeystoreConnector

  def assertRegistrationFootprint()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] = {
    for {
      vatScheme <- vatRegConnector.createNewRegistration()
      _ <- keystoreConnector.cache[String]("RegistrationId", vatScheme.id)
    } yield DownstreamOutcome.Success
  }

  def submitVatScheme()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] = {
    val tradingDetailsSubmission = submitTradingDetails()
    val vatChoiceSubmission = submitVatChoice()

    for {
      _ <- tradingDetailsSubmission
      _ <- vatChoiceSubmission
    } yield DownstreamOutcome.Success
  }

  def submitTradingDetails()(implicit hc: HeaderCarrier): Future[VatTradingDetails] = {
    for {
      regId <- fetchRegistrationId
      vatScheme <- vatRegConnector.getRegistration(regId)
      vatTradingDetails = vatScheme.tradingDetails.getOrElse(VatTradingDetails.empty)
      tradingName <- s4LService.fetchAndGet[TradingName](CacheKeys.TradingName.toString)
      tradingDetails = tradingName.getOrElse(TradingName(vatScheme)).toApi(vatTradingDetails)
      response <- vatRegConnector.upsertVatTradingDetails(regId, tradingDetails)
    } yield response
  }

  def submitVatChoice()(implicit hc: HeaderCarrier): Future[VatChoice] = {
    for {
      regId <- fetchRegistrationId
      vatScheme <- vatRegConnector.getRegistration(regId)
      vatChoice = vatScheme.vatChoice.getOrElse(VatChoice.empty)
      startDate <- s4LService.fetchAndGet[StartDate](CacheKeys.StartDate.toString)
      voluntaryRegistration <- s4LService.fetchAndGet[VoluntaryRegistration](CacheKeys.VoluntaryRegistration.toString)
      sdVatChoice = startDate.getOrElse(StartDate(vatScheme)).toApi(vatChoice)
      vrVatChoice = voluntaryRegistration.getOrElse(VoluntaryRegistration(vatScheme)).toApi(sdVatChoice)
      response <- vatRegConnector.upsertVatChoice(regId, vrVatChoice)
    } yield response
  }

  def getRegistrationSummary()(implicit hc: HeaderCarrier): Future[Summary] =
    getVatScheme().map(registrationToSummary(_))

  def getVatScheme()(implicit hc: HeaderCarrier): Future[VatScheme] =
    for {
      regId <- fetchRegistrationId
      vatScheme <- vatRegConnector.getRegistration(regId)
    } yield vatScheme

//  def retrieveViewModel[V](v: ApiModelTransformer[V], key: String)(implicit hc: HeaderCarrier): Future[V] = {
//    s4LService.fetchAndGet[V](key) flatMap {
//      case Some(viewModel) => Future.successful(viewModel)
//      case None => for {
//        vatScheme <- getVatScheme()
//        viewModel = v.apply(vatScheme)
//      } yield viewModel
//    }
//  }

  def registrationToSummary(vatScheme: VatScheme): Summary = Summary(
    Seq(
      getVatDetailsSection(vatScheme.vatChoice.getOrElse(VatChoice.empty)),
      getCompanyDetailsSection(vatScheme.tradingDetails.getOrElse(VatTradingDetails.empty))
    )
  )

  private def getVatDetailsSection(vatChoice: VatChoice) = {

    def getTaxableTurnover: SummaryRow = SummaryRow(
      "vatDetails.taxableTurnover",
      vatChoice.necessity match {
        case VatChoice.NECESSITY_VOLUNTARY => Right("No")
        case _ => Right("Yes")
      },
      Some(controllers.userJourney.routes.TaxableTurnoverController.show())
    )

    def getNecessity: SummaryRow = SummaryRow(
      "vatDetails.necessity",
      vatChoice.necessity match {
        case VatChoice.NECESSITY_VOLUNTARY => Right("Yes")
        case _ => Right("No")
      },
      vatChoice.necessity match {
        case VatChoice.NECESSITY_VOLUNTARY => Some(controllers.userJourney.routes.VoluntaryRegistrationController.show())
        case _ => None
      }
    )

    def getStartDate: SummaryRow = SummaryRow(
      "vatDetails.startDate",
      vatChoice.necessity match {
        case VatChoice.NECESSITY_VOLUNTARY => {
          if (vatChoice.startDate.toString("dd/MM/yyyy") == "01/01/1970") {
            Right(messagesApi("pages.summary.vatDetails.mandatoryStartDate"))
          } else {
            Right(vatChoice.startDate.toString("d MMMM y"))
          }
        }
        case _ => Right(messagesApi("pages.summary.vatDetails.mandatoryStartDate"))
      },
      vatChoice.necessity match {
        case VatChoice.NECESSITY_VOLUNTARY => Some(controllers.userJourney.routes.StartDateController.show())
        case _ => None
      }
    )

    if(vatChoice.necessity == VatChoice.NECESSITY_VOLUNTARY) {
      SummarySection(
        id = "vatDetails",
        Seq(
          getTaxableTurnover,
          getNecessity,
          getStartDate
        )
      )
    } else {
      SummarySection(
        id = "vatDetails",
        Seq(
          getTaxableTurnover,
          getStartDate
        )
      )
    }

  }

  private def getCompanyDetailsSection(vatTradingDetails: VatTradingDetails) = SummarySection(
    id = "companyDetails",
    Seq(
      SummaryRow(
        "companyDetails.tradingName",
        vatTradingDetails.tradingName match {
          case "" => Right("No")
          case _ => Right(vatTradingDetails.tradingName)
        },
        Some(controllers.userJourney.routes.TradingNameController.show())
      )
    )
  )
}

