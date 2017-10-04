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

import java.time.LocalDate

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.{VatChoice, VatEligibilityChoice}
import models.view.SummaryRow
import models.view.vatTradingDetails.vatChoice.StartDateView

class SummaryVatDetailsSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  val serviceName = "vat-registration-eligibility-frontend"
  "The section builder composing a vat details section" should {

    "with taxableTurnoverRow render" should {

      "a 'No' if it's a voluntary registration" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_VOLUNTARY).vatEligibilityChoice)
        builder.taxableTurnoverRow mustBe
          SummaryRow("vatDetails.taxableTurnover", "app.common.no", Some(controllers.vatTradingDetails.vatChoice.routes.TaxableTurnoverController.show()))
      }

      "a 'Yes' if it's a mandatory" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
            validServiceEligibility(VatEligibilityChoice.NECESSITY_OBLIGATORY).vatEligibilityChoice)
        builder.taxableTurnoverRow mustBe
          SummaryRow("vatDetails.taxableTurnover", "app.common.yes", Some(controllers.vatTradingDetails.vatChoice.routes.TaxableTurnoverController.show()))
      }

      "a 'No' if it's a voluntary registration and point to eligibility frontend if switch is on" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_VOLUNTARY).vatEligibilityChoice,
          useEligibilityFrontend = true)
        builder.taxableTurnoverRow mustBe SummaryRow("vatDetails.taxableTurnover", "app.common.no", Some(builder.getUrl(serviceName,"sales-over-threshold")))
      }

      "a 'Yes' if it's a mandatory registration and point to eligibility frontend if switch is on" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_OBLIGATORY).vatEligibilityChoice,
        useEligibilityFrontend = true)
        builder.taxableTurnoverRow mustBe SummaryRow("vatDetails.taxableTurnover", "app.common.yes", Some(builder.getUrl(serviceName,"sales-over-threshold")))
      }
    }

    "with necessityRow render" should {

      "a 'Yes' if it's a voluntary registration" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_VOLUNTARY).vatEligibilityChoice)
        builder.necessityRow mustBe
          SummaryRow("vatDetails.necessity", "app.common.yes", Some(controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationController.show()))
      }

      "a 'No' if it's a mandatory registration" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_OBLIGATORY).vatEligibilityChoice)
        builder.necessityRow mustBe SummaryRow("vatDetails.necessity", "app.common.no", None)
      }

      "a 'Yes' if it's a voluntary registration registration and point to eligibility frontend if switch is on" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_VOLUNTARY).vatEligibilityChoice,
          useEligibilityFrontend = true)
        builder.necessityRow mustBe SummaryRow("vatDetails.necessity", "app.common.yes", Some(builder.getUrl(serviceName,"register-voluntary")))
      }
    }

    "with overThresholdDate render" should {

      "a month and year displayed if a date is entered" in {
        val builder = SummaryVatDetailsSectionBuilder(
          vatTradingDetails = Some(validVatTradingDetails),
          vatEligiblityChoice = Some(validEligibilityChoice)
        )
        builder.overThresholdDateRow mustBe
          SummaryRow("vatDetails.overThresholdDate", testDate.format(testMonthYearPresentationFormatter), Some(controllers.vatTradingDetails.vatChoice.routes.OverThresholdController.show()))
      }

      "a month and year displayed if a date is entered and point to eligibility frontend if switch is on" in {
        val builder = SummaryVatDetailsSectionBuilder(
          vatTradingDetails = Some(validVatTradingDetails),
          vatEligiblityChoice = Some(validEligibilityChoice),
          useEligibilityFrontend = true
        )
        builder.overThresholdDateRow mustBe SummaryRow("vatDetails.overThresholdDate", testDate.format(testMonthYearPresentationFormatter), Some(builder.getUrl(serviceName,"turnover-over-threshold")))
      }

    }

    "with startDateRow render" should {

      "a date with format 'd MMMM y' if it's a voluntary registration" in {
        val builder = SummaryVatDetailsSectionBuilder(
          vatTradingDetails = Some(tradingDetails(
            startDateSelection = StartDateView.SPECIFIC_DATE,
            startDate = Some(LocalDate.of(2017, 3, 21)))),
          vatEligiblityChoice = Some(validEligibilityChoice)
        )

        val expectedRow = SummaryRow(
          "vatDetails.startDate",
          "21 March 2017",
          Some(controllers.vatTradingDetails.vatChoice.routes.StartDateController.show())
        )

        builder.startDateRow mustBe expectedRow
      }

      "a Companies House incorporation date message, if it's a voluntary registration and the date is a default date" in {
        val builder = SummaryVatDetailsSectionBuilder(
        Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_VOLUNTARY).vatEligibilityChoice
        )

        val expectedRow = SummaryRow(
          "vatDetails.startDate",
          "pages.summary.vatDetails.mandatoryStartDate",
          Some(controllers.vatTradingDetails.vatChoice.routes.StartDateController.show()))

        builder.startDateRow mustBe expectedRow
      }

      "a Companies House incorporation date message, if it's a mandatory registration" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_OBLIGATORY).vatEligibilityChoice)
        builder.startDateRow mustBe SummaryRow("vatDetails.startDate", "pages.summary.vatDetails.mandatoryStartDate", None)
      }
    }
    "with tradingNameRow render" should {

      "a trading name if there's one" in {
        val builder = SummaryVatDetailsSectionBuilder(Some(tradingDetails()))
        builder.tradingNameRow mustBe SummaryRow(
          "vatDetails.tradingName",
          "ACME Ltd.",
          Some(controllers.vatTradingDetails.routes.TradingNameController.show()))
      }

      "a 'No' if there isn't a trading name" in {
        val builder = SummaryVatDetailsSectionBuilder()
        builder.tradingNameRow mustBe SummaryRow("vatDetails.tradingName", "app.common.no", Some(controllers.vatTradingDetails.routes.TradingNameController.show()))
      }
    }

    "with section generate" should {

      "a valid summary section" in {
        val builder = SummaryVatDetailsSectionBuilder(vatTradingDetails = Some(tradingDetails()))
        builder.section.id mustBe "vatDetails"
        builder.section.rows.length mustEqual 7
      }
    }

  }
}
