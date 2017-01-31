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
import connectors.VatRegistrationConnector
import enums.DownstreamOutcome
import models.api.{VatChoice, VatScheme, VatTradingDetails}
import models.view.{Summary, SummaryRow, SummarySection}
import org.joda.time.DateTime
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[VatRegistrationService])
trait RegistrationService {

  def getRegistrationSummary()(implicit executionContext: ExecutionContext): Future[Summary]

}

class VatRegistrationService @Inject()(vatRegistrationConnector: VatRegistrationConnector) extends RegistrationService {

  def assertRegistrationFootprint()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] = {
    vatRegistrationConnector.createNewRegistration()
  }

  override def getRegistrationSummary()(implicit ec: ExecutionContext): Future[Summary] = {
    Future.successful(
      registrationToSummary(
        VatScheme("VAT123456",
          VatTradingDetails("ACME INC"),
          VatChoice(DateTime.now, VatChoice.NECESSITY_VOLUNTARY)
        )
      )
    )
  }

  def registrationToSummary(apiModel: VatScheme): Summary = {
    Summary(
      Seq(
        getVatDetailsSection(apiModel.vatChoice),
        getCompanyDetailsSection(apiModel.tradingDetails)
      )
    )
  }

  private def getVatDetailsSection(vatDetails: VatChoice) = {

    def getRegisterVoluntarily: SummaryRow = {
      SummaryRow(
        "vatDetails.registerVoluntarily",
        vatDetails.necessity match {
          case VatChoice.NECESSITY_VOLUNTARY => Right("Yes")
          case _ => Left("No")
        },
        Some(controllers.userJourney.routes.SummaryController.show())
      )
    }

    def getStartDate: SummaryRow = {
      SummaryRow("vatDetails.startDate",
        Right(vatDetails.startDate.toString("d M y")),
        Some(controllers.userJourney.routes.StartDateController.show())
      )
    }

    SummarySection(
      id = "vatDetails",
      Seq(
        getRegisterVoluntarily,
        getStartDate
      )
    )
  }

  private def getCompanyDetailsSection(companyDetails: VatTradingDetails) = SummarySection(
    id = "companyDetails",
    Seq(
      SummaryRow(
        "companyDetails.tradingName",
        Right(companyDetails.tradingName),
        Some(controllers.userJourney.routes.TradingNameController.show())
      )
    )
  )
}


