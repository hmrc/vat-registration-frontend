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

package controllers.builders

import java.time.format.DateTimeFormatter

import models.api.VatEligibilityChoice.NECESSITY_VOLUNTARY
import models.api.{VatChoice, VatEligibilityChoice, VatStartDate, VatTradingDetails}
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason
import models.view.{SummaryRow, SummarySection}
import play.api.mvc.Call
import uk.gov.hmrc.play.config.ServicesConfig


case class SummaryVatDetailsSectionBuilder (vatTradingDetails: Option[VatTradingDetails] = None,
                                            vatEligiblityChoice: Option[VatEligibilityChoice] = None,
                                            useEligibilityFrontend: Boolean = false)
  extends SummarySectionBuilder with ServicesConfig{

  override val sectionId: String = "vatDetails"
  val monthYearPresentationFormatter = DateTimeFormatter.ofPattern("MMMM y")
  val serviceName = "vat-registration-eligibility-frontend"

  private val voluntaryRegistration = vatEligiblityChoice.exists(_.registeringVoluntarily)

  val taxableTurnoverRow: SummaryRow = SummaryRow(
    s"$sectionId.taxableTurnover",
    s"app.common.${if (voluntaryRegistration) "no" else "yes"}",
    if (useEligibilityFrontend) {
      Some(getUrl(serviceName,"sales-over-threshold"))
    } else {
      Some(controllers.vatTradingDetails.vatChoice.routes.TaxableTurnoverController.show())
    }
  )

  val overThresholdSelectionRow: SummaryRow = SummaryRow(
    s"$sectionId.overThresholdSelection",
    vatEligiblityChoice.flatMap(_.vatThresholdPostIncorp.map(_.overThresholdSelection)).collect {
      case true => "app.common.yes"
      case false => "app.common.no"
    }.getOrElse(""),
    if(useEligibilityFrontend){
      Some(getUrl(serviceName,"turnover-over-threshold"))
    } else {
      Some(controllers.vatTradingDetails.vatChoice.routes.OverThresholdController.show())
    }
  )

  val overThresholdDateRow: SummaryRow = SummaryRow(
    s"$sectionId.overThresholdDate",
    vatEligiblityChoice.flatMap(_.vatThresholdPostIncorp.flatMap(_.overThresholdDate))
      .map(_.format(monthYearPresentationFormatter))
      .getOrElse(""),
    if (useEligibilityFrontend) {
      Some(getUrl(serviceName,"turnover-over-threshold"))
    } else {
      Some(controllers.vatTradingDetails.vatChoice.routes.OverThresholdController.show())
    }
  )

  val necessityRow: SummaryRow = SummaryRow(
    s"$sectionId.necessity",
    s"app.common.${if (voluntaryRegistration) "yes" else "no"}",
    if (voluntaryRegistration) {
      if (useEligibilityFrontend) {
        Some(getUrl(serviceName, "register-voluntary"))
      } else {
        Some(controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationController.show())
      }
    } else {
      None
    }
  )

  val voluntaryReasonRow: SummaryRow = SummaryRow(
    s"$sectionId.voluntaryRegistrationReason",
    vatEligiblityChoice.flatMap(_.reason).collect {
      case VoluntaryRegistrationReason.SELLS => "pages.summary.voluntaryReason.sells"
      case VoluntaryRegistrationReason.INTENDS_TO_SELL => "pages.summary.voluntaryReason.intendsToSell"
    }.getOrElse("app.common.no"),
    if(useEligibilityFrontend){
      Some(getUrl(serviceName,"registration-reason"))
    } else {
      Some(controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationReasonController.show())
    }
  )

  val startDateRow: SummaryRow = SummaryRow(
    s"$sectionId.startDate",
    vatTradingDetails.map(_.vatChoice).collect {
      case VatChoice(VatStartDate(_, Some(date))) => date.format(presentationFormatter)
    }.getOrElse(s"pages.summary.$sectionId.mandatoryStartDate"),
    if (voluntaryRegistration) Some(controllers.vatTradingDetails.vatChoice.routes.StartDateController.show()) else None
  )

  val tradingNameRow: SummaryRow = SummaryRow(
    s"$sectionId.tradingName",
    vatTradingDetails.flatMap(_.tradingName.tradingName).getOrElse("app.common.no"),
    Some(controllers.vatTradingDetails.routes.TradingNameController.show())
  )

  val section: SummarySection =
    SummarySection(
      sectionId,
      rows = Seq(
        (taxableTurnoverRow, !vatEligiblityChoice.flatMap(_.vatThresholdPostIncorp).isDefined),
        (overThresholdSelectionRow, vatEligiblityChoice.flatMap(_.vatThresholdPostIncorp).isDefined),
        (overThresholdDateRow, vatEligiblityChoice.flatMap(_.vatThresholdPostIncorp).flatMap(_.overThresholdDate).isDefined),
        (necessityRow, voluntaryRegistration),
        (voluntaryReasonRow, voluntaryRegistration),
        (startDateRow, true),
        (tradingNameRow, true)
      )
    )

  def getUrl(serviceName: String, uri: String): Call = {
    val basePath = getConfString(s"$serviceName.www.host", "")
    val mainUri = getConfString(s"$serviceName.uri","/register-for-vat/")
    val serviceUri = getConfString(s"$serviceName.uris.$uri",uri)
    Call("Get", s"$basePath$mainUri$serviceUri")
  }


}
