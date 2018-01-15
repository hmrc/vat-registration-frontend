/*
 * Copyright 2018 HM Revenue & Customs
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

import features.returns.{Returns, Start}
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.VatEligibilityChoice
import models.view.SummaryRow

class SummaryVatDetailsSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  val serviceName = "vat-registration-eligibility-frontend"
  def returnsWithStartDate(startDate : Option[LocalDate] = Some(LocalDate.now())) =
    Some(Returns(None, None, None, Some(Start(startDate))))

  "The section builder composing a vat details section" should {

    "with taxableTurnoverRow render" should {

      "a 'No' if it's a voluntary registration" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
            validServiceEligibility(VatEligibilityChoice.NECESSITY_VOLUNTARY).vatEligibilityChoice,
            returnsBlock = returnsWithStartDate(),
            useEligibilityFrontend = false)
        builder.taxableTurnoverRow mustBe
          SummaryRow("vatDetails.taxableTurnover", "app.common.no", Some(controllers.vatTradingDetails.vatChoice.routes.TaxableTurnoverController.show()))
      }

      "a 'Yes' if it's a mandatory" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
            validServiceEligibility(VatEligibilityChoice.NECESSITY_OBLIGATORY).vatEligibilityChoice,
            returnsBlock = returnsWithStartDate(),
            useEligibilityFrontend = false)
        builder.taxableTurnoverRow mustBe
          SummaryRow("vatDetails.taxableTurnover", "app.common.yes", Some(controllers.vatTradingDetails.vatChoice.routes.TaxableTurnoverController.show()))
      }

      "a 'No' if it's a voluntary registration and point to eligibility frontend if switch is on" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_VOLUNTARY).vatEligibilityChoice,
          returnsBlock = returnsWithStartDate())
        builder.taxableTurnoverRow mustBe SummaryRow("vatDetails.taxableTurnover", "app.common.no", Some(builder.getUrl(serviceName,"sales-over-threshold")))
      }

      "a 'Yes' if it's a mandatory registration and point to eligibility frontend if switch is on" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_OBLIGATORY).vatEligibilityChoice,
          returnsBlock = returnsWithStartDate())
        builder.taxableTurnoverRow mustBe SummaryRow("vatDetails.taxableTurnover", "app.common.yes", Some(builder.getUrl(serviceName,"sales-over-threshold")))
      }
    }

    "with necessityRow render" should {

      "a 'Yes' if it's a voluntary registration" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_VOLUNTARY).vatEligibilityChoice,
          useEligibilityFrontend = false,
          returnsBlock = returnsWithStartDate())
        builder.necessityRow mustBe
          SummaryRow("vatDetails.necessity", "app.common.yes", Some(controllers.vatTradingDetails.vatChoice.routes.VoluntaryRegistrationController.show()))
      }

      "a 'No' if it's a mandatory registration" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_OBLIGATORY).vatEligibilityChoice,
          useEligibilityFrontend = false,
          returnsBlock = returnsWithStartDate())
        builder.necessityRow mustBe SummaryRow("vatDetails.necessity", "app.common.no", None)
      }

      "a 'Yes' if it's a voluntary registration registration and point to eligibility frontend if switch is on" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_VOLUNTARY).vatEligibilityChoice,
          returnsBlock = returnsWithStartDate())
        builder.necessityRow mustBe SummaryRow("vatDetails.necessity", "app.common.yes", Some(builder.getUrl(serviceName,"register-voluntary")))
      }
    }

    "with overThresholdDate render" should {

      "a month and year displayed if a date is entered" in {
        val builder = SummaryVatDetailsSectionBuilder(
          vatTradingDetails = Some(validVatTradingDetails),
          vatEligiblityChoice = Some(validEligibilityChoice),
          useEligibilityFrontend = false,
          returnsBlock = returnsWithStartDate()
        )
        builder.overThresholdDateRow mustBe
          SummaryRow("vatDetails.overThresholdDate", testDate.format(testMonthYearPresentationFormatter), Some(controllers.vatTradingDetails.vatChoice.routes.OverThresholdController.show()))
      }

      "a month and year displayed if a date is entered and point to eligibility frontend if switch is on" in {
        val builder = SummaryVatDetailsSectionBuilder(
          vatTradingDetails = Some(validVatTradingDetails),
          vatEligiblityChoice = Some(validEligibilityChoice),
          returnsBlock = returnsWithStartDate()
        )
        builder.overThresholdDateRow mustBe SummaryRow("vatDetails.overThresholdDate", testDate.format(testMonthYearPresentationFormatter), Some(builder.getUrl(serviceName,"turnover-over-threshold")))
      }

    }

    "with expectedOverThresholdSelectionRow render" should {

      "a 'No' if the expectedOverThresholdSelection is false" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(expectedThreshold = validExpectedOverFalse).vatEligibilityChoice,
          returnsBlock = returnsWithStartDate())

        builder.expectedOverThresholdSelectionRow mustBe
          SummaryRow("vatDetails.expectedOverThresholdSelection",
            "app.common.no",
            Some(builder.getUrl(serviceName,"thought-over-threshold")))
      }

      "a 'Yes' if the expectedOverThresholdSelection is true" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(expectedThreshold = validExpectedOverTrue).vatEligibilityChoice,
          returnsBlock = returnsWithStartDate())

        builder.expectedOverThresholdSelectionRow mustBe
          SummaryRow("vatDetails.expectedOverThresholdSelection",
            "app.common.yes",
            Some(builder.getUrl(serviceName,"thought-over-threshold")))
      }
    }

    "with expectedOverThresholdDateRow render" should {

      "a 'date' if the expectedOverThresholdDate is present" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(expectedThreshold = validExpectedOverTrue).vatEligibilityChoice,
          returnsBlock = returnsWithStartDate())

        builder.expectedOverThresholdDateRow mustBe
          SummaryRow("vatDetails.expectedOverThresholdDate",
            testDate.format(testPresentationFormatter),
            Some(builder.getUrl(serviceName,"thought-over-threshold")))
      }

      "date row with no 'date' if the expectedOverThresholdDate is present" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(expectedThreshold = validExpectedOverTrueNoDate).vatEligibilityChoice,
          returnsBlock = returnsWithStartDate())

        builder.expectedOverThresholdDateRow mustBe
          SummaryRow("vatDetails.expectedOverThresholdDate",
            "",
            Some(builder.getUrl(serviceName,"thought-over-threshold")))
      }
    }

    "with startDateRow render" should {

      "a date with format 'd MMMM y' if it's a voluntary registration where they are not incorped" in {
        val builder = SummaryVatDetailsSectionBuilder(
          vatEligiblityChoice = Some(validEligibilityChoice),
          returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21)))
        )

        val expectedRow = SummaryRow(
          "vatDetails.startDate",
          "21 March 2017",
          Some(features.returns.routes.ReturnsController.voluntaryStartPage())
        )

        builder.startDateRow mustBe expectedRow
      }

      "a Companies House incorporation date message, if it's a voluntary registration and the date is a default date and they are not incorped" in {
        val builder = SummaryVatDetailsSectionBuilder(
        Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_VOLUNTARY).vatEligibilityChoice,
          returnsBlock = returnsWithStartDate(None)
        )

        val expectedRow = SummaryRow(
          "vatDetails.startDate",
          "pages.summary.vatDetails.mandatoryStartDate",
          Some(features.returns.routes.ReturnsController.voluntaryStartPage())
        )

        builder.startDateRow mustBe expectedRow
      }

      "a Companies House incorporation date message, if it's a mandatory registration and they are not incorped" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(tradingDetails()),
          validServiceEligibility(VatEligibilityChoice.NECESSITY_OBLIGATORY).vatEligibilityChoice,
          returnsBlock = returnsWithStartDate(None)
        )
        builder.startDateRow mustBe SummaryRow("vatDetails.startDate", "pages.summary.vatDetails.mandatoryStartDate", None)
      }

      "a date with format 'd MMMM y' if it's a voluntary registration where they are incorped" in {
        val builder = SummaryVatDetailsSectionBuilder(
          vatEligiblityChoice = Some(validEligibilityChoice),
          incorpDate = Some(LocalDate.of(2017, 1, 21)),
          returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21)))
        )

        val expectedRow = SummaryRow(
          "vatDetails.startDate",
          "21 March 2017",
          Some(features.returns.routes.ReturnsController.voluntaryStartPage())
        )

        builder.startDateRow mustBe expectedRow
      }

      "a date with format 'd MMMM y' if it's a mandatory registration where they are incorped and picked an earlier date" in {
        val builder = SummaryVatDetailsSectionBuilder(
          vatEligiblityChoice = validServiceEligibility(VatEligibilityChoice.NECESSITY_OBLIGATORY).vatEligibilityChoice,
          incorpDate = Some(LocalDate.of(2017, 3, 21)),
          returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 4, 26)))
        )

        val expectedRow = SummaryRow(
          "vatDetails.startDate",
          "26 April 2017",
          Some(features.returns.routes.ReturnsController.mandatoryStartPage())
        )

        builder.startDateRow mustBe expectedRow
      }

      "a date with format 'd MMMM y' if it's a mandatory registration where they are incorped and picked the latest date possible" in {
        val builder = SummaryVatDetailsSectionBuilder(
          vatEligiblityChoice = validServiceEligibility(VatEligibilityChoice.NECESSITY_OBLIGATORY).vatEligibilityChoice,
          incorpDate = Some(LocalDate.of(2017, 3, 21)),
          returnsBlock = returnsWithStartDate(None)
        )

        builder.startDateRow mustBe SummaryRow(
          "vatDetails.startDate",
          "pages.summary.vatDetails.mandatoryStartDate",
          Some(features.returns.routes.ReturnsController.mandatoryStartPage())
        )
      }
    }
    "with tradingNameRow render" should {

      "a trading name if there's one" in {
        val builder = SummaryVatDetailsSectionBuilder(
          vatTradingDetails = Some(validVatTradingDetails),
          returnsBlock = returnsWithStartDate()
        )
        builder.tradingNameRow mustBe SummaryRow(
          "vatDetails.tradingName",
          "ACME INC",
          Some(controllers.vatTradingDetails.routes.TradingNameController.show()))
      }

      "a 'No' if there isn't a trading name" in {
        val builder = SummaryVatDetailsSectionBuilder(
          returnsBlock = Some(Returns(None, None, None, None))
        )
        builder.tradingNameRow mustBe SummaryRow("vatDetails.tradingName", "app.common.no", Some(controllers.vatTradingDetails.routes.TradingNameController.show()))
      }
    }

    "with section generate" should {

      "a valid summary section" in {
        val builder = SummaryVatDetailsSectionBuilder(vatTradingDetails = Some(tradingDetails()), returnsBlock = returnsWithStartDate())
        builder.section.id mustBe "vatDetails"
        builder.section.rows.length mustEqual 9
      }
    }

  }
}
