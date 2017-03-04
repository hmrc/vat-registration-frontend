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

package controllers.userJourney

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import models.api._
import models.view._
import play.api.UnexpectedException
import play.api.mvc._
import services.{S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class SummaryController @Inject()(ds: CommonPlayDependencies)
                                 (implicit s4LService: S4LService, vatRegistrationService: VatRegistrationService)
  extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async { implicit user =>
    implicit request =>
      for {
        _ <- vatRegistrationService.submitVatScheme()
        summary <- getRegistrationSummary()
        _ <- s4LService.clear()
      } yield Ok(views.html.pages.summary(summary))
  }

  //$COVERAGE-OFF$

  // Summary page methods
  def getRegistrationSummary()(implicit hc: HeaderCarrier): Future[Summary] =
    vatRegistrationService.getVatScheme().map(registrationToSummary)

  def registrationToSummary(vatScheme: VatScheme): Summary = Summary(
    Seq(
      getVatDetailsSection(vatScheme.vatChoice.getOrElse(VatChoice())),
      getCompanyDetailsSection(
        vatScheme.tradingDetails.getOrElse(VatTradingDetails()),
        vatScheme.financials.getOrElse(VatFinancials(
          bankAccount = None,
          turnoverEstimate = 0L,
          reclaimVatOnMostReturns = false,
          vatAccountingPeriod = VatAccountingPeriod(None, ""))))
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
        case VatChoice.NECESSITY_VOLUNTARY =>
          val startdate = vatChoice.startDate.toString("dd/MM/yyyy")
          if (startdate == "31/12/1969" || startdate == "01/01/1970") {
            Right(messagesApi("pages.summary.vatDetails.mandatoryStartDate"))
          } else {
            Right(vatChoice.startDate.toString("d MMMM y"))
          }
        case _ => Right(messagesApi("pages.summary.vatDetails.mandatoryStartDate"))
      },
      vatChoice.necessity match {
        case VatChoice.NECESSITY_VOLUNTARY => Some(controllers.userJourney.routes.StartDateController.show())
        case _ => None
      }
    )

    SummarySection(
      id = "vatDetails",
      Seq(
        (getTaxableTurnover, true),
        (getNecessity, vatChoice.necessity == VatChoice.NECESSITY_VOLUNTARY),
        (getStartDate, true)
      )
    )

  }

  private def getCompanyDetailsSection(vatTradingDetails: VatTradingDetails, vatFinancials: VatFinancials) = {

    def getTradingName: SummaryRow = SummaryRow(
      "companyDetails.tradingName",
      vatTradingDetails.tradingName match {
        case "" => Right("No")
        case _ => Right(vatTradingDetails.tradingName)
      },
      Some(controllers.userJourney.routes.TradingNameController.show())
    )

    def getEstimatedSalesValue: SummaryRow = SummaryRow(
      "companyDetails.estimatedSalesValue",
      Right(s"£${vatFinancials.turnoverEstimate.toString}"),
      Some(controllers.userJourney.routes.EstimateVatTurnoverController.show())
    )

    def getZeroRatedSales: SummaryRow = SummaryRow(
      "companyDetails.zeroRatedSales",
      vatFinancials.zeroRatedSalesEstimate match {
        case Some(_) => Right("Yes")
        case None => Right("No")
      },
      Some(controllers.userJourney.routes.ZeroRatedSalesController.show())
    )

    def getEstimatedZeroRatedSales: SummaryRow = SummaryRow(
      "companyDetails.zeroRatedSalesValue",
      Right(s"£${vatFinancials.zeroRatedSalesEstimate.getOrElse("").toString}"),
      Some(controllers.userJourney.routes.EstimateZeroRatedSalesController.show())
    )

    def getVatChargeExpectancy: SummaryRow = SummaryRow(
      "companyDetails.reclaimMoreVat",
      if (vatFinancials.reclaimVatOnMostReturns) {
        Right(messagesApi("pages.summary.companyDetails.reclaimMoreVat.yes"))
      } else {
        Right(messagesApi("pages.summary.companyDetails.reclaimMoreVat.no"))
      },
      Some(controllers.userJourney.routes.VatChargeExpectancyController.show())
    )

    def getAccountingPeriod: SummaryRow = SummaryRow(
      "companyDetails.accountingPeriod",
      vatFinancials.vatAccountingPeriod.frequency match {
        case VatReturnFrequency.MONTHLY => Right(messagesApi("pages.summary.companyDetails.accountingPeriod.monthly"))
        case VatReturnFrequency.QUARTERLY => vatFinancials.vatAccountingPeriod.periodStart match {
          case Some(period) => Right(messagesApi(s"pages.summary.companyDetails.accountingPeriod.${period.substring(0, 3)}"))
          case None => throw UnexpectedException(Some(s"selected quarterly accounting period, but periodStart was None"))
        }
      },
      Some(controllers.userJourney.routes.VatReturnFrequencyController.show())
    )

    SummarySection(
      id = "companyDetails",
      Seq(
        (getTradingName, true),
        (getEstimatedSalesValue, true),
        (getZeroRatedSales, true),
        (getEstimatedZeroRatedSales, vatFinancials.zeroRatedSalesEstimate.isDefined),
        (getVatChargeExpectancy, true),
        (getAccountingPeriod, true)
      )
    )

  }

  //$COVERAGE-ON$

}
