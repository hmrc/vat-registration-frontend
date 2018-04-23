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

import features.returns.models.{Returns, Start}
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.view.SummaryRow


class SummaryVatDetailsSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  val serviceName = "vat-registration-eligibility-frontend"
  def returnsWithStartDate(startDate : Option[LocalDate] = Some(LocalDate.now())) =
    Some(Returns(None, None, None, Some(Start(startDate))))

  "The section builder composing a vat details section" should {

    "with taxableTurnoverRow render" should {

      "a 'No' if it's a voluntary registration and point to eligibility frontend if switch is on" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(generateTradingDetails()),
          optVoluntaryRegistration,
          returnsBlock = returnsWithStartDate(),
          taxableThreshold = currentThreshold)
        builder.overThresholdThirtySelectionRow mustBe SummaryRow("vatDetails.overThresholdThirtySelection", "app.common.no", Some(builder.getUrl(serviceName,"make-more-taxable-sales")), List(taxableThreshold.threshold))
      }

      "a 'Yes' if it's a mandatory registration and point to eligibility frontend if switch is on" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(generateTradingDetails()),
          optMandatoryRegistration,
          returnsBlock = returnsWithStartDate(),
          taxableThreshold = currentThreshold)
        builder.overThresholdThirtySelectionRow mustBe SummaryRow("vatDetails.overThresholdThirtySelection", "app.common.yes", Some(builder.getUrl(serviceName,"make-more-taxable-sales")), List(taxableThreshold.threshold))
      }
    }

    "with necessityRow render" should {

      "a 'No' if it's a mandatory registration" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(generateTradingDetails()),
          optMandatoryRegistration,
          returnsBlock = returnsWithStartDate(),
          taxableThreshold = currentThreshold)
        builder.necessityRow mustBe SummaryRow("vatDetails.necessity", "app.common.no", None)
      }

      "a 'Yes' if it's a voluntary registration registration and point to eligibility frontend if switch is on" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(generateTradingDetails()),
          optVoluntaryRegistration,
          returnsBlock = returnsWithStartDate(),
          taxableThreshold = currentThreshold)
        builder.necessityRow mustBe SummaryRow("vatDetails.necessity", "app.common.yes", Some(builder.getUrl(serviceName,"register-voluntary")))
      }
    }

    "with overThresholdDate render" should {

      "a month and year displayed if a date is entered and point to eligibility frontend if switch is on" in {
        val builder = SummaryVatDetailsSectionBuilder(
          tradingDetails = Some(generateTradingDetails()),
          threshold = optMandatoryRegistrationTwelve,
          returnsBlock = returnsWithStartDate(),
          taxableThreshold = currentThreshold
        )
        builder.overThresholdDateRow mustBe SummaryRow("vatDetails.overThresholdDate", testDate.format(testMonthYearPresentationFormatter), Some(builder.getUrl(serviceName,"gone-over-threshold")),List(taxableThreshold.threshold))
      }

    }

    "with pastOverThresholdSelectionRow render" should {

      "a 'No' if the pastOverThresholdSelectionRow is false" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(generateTradingDetails()),
          optMandatoryRegistration,
          returnsBlock = returnsWithStartDate(),
          taxableThreshold = currentThreshold)

        builder.pastOverThresholdSelectionRow mustBe
          SummaryRow("vatDetails.expectationOverThresholdSelection",
            "app.common.no",
            Some(builder.getUrl(serviceName,"gone-over-threshold-period")))
      }

      "a 'Yes' if the pastOverThresholdSelectionRow is true" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(generateTradingDetails()),
          generateOptionalThreshold(expectedOverThreshold = validExpectedOverTrue),
          returnsBlock = returnsWithStartDate(),
          taxableThreshold = currentThreshold)

        builder.pastOverThresholdSelectionRow mustBe
          SummaryRow("vatDetails.expectationOverThresholdSelection",
            "app.common.yes",
            Some(builder.getUrl(serviceName,"gone-over-threshold-period")))
      }
    }

    "with pastOverThresholdDateRow render" should {

      "a 'date' if the pastOverThresholdDateRow is present" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(generateTradingDetails()),
          generateOptionalThreshold(expectedOverThreshold = validExpectedOverTrue),
          returnsBlock = returnsWithStartDate(),
          taxableThreshold = currentThreshold)

        builder.pastOverThresholdDateRow mustBe
          SummaryRow("vatDetails.expectationOverThresholdDate",
            testDate.format(testPresentationFormatter),
            Some(builder.getUrl(serviceName,"gone-over-threshold-period")))
      }
    }

    "with startDateRow render" should {

      "a date with format 'd MMMM y' if it's a voluntary registration where they are not incorped" in {
        val builder = SummaryVatDetailsSectionBuilder(
          threshold = optVoluntaryRegistration,
          returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21))),
          taxableThreshold = currentThreshold
        )

        val expectedRow = SummaryRow(
          "vatDetails.startDate",
          "21 March 2017",
          Some(features.returns.controllers.routes.ReturnsController.voluntaryStartPage())
        )

        builder.startDateRow mustBe expectedRow
      }

      "a Companies House incorporation date message, if it's a voluntary registration and the date is a default date and they are not incorped" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(generateTradingDetails()),
          optVoluntaryRegistration,
          returnsBlock = returnsWithStartDate(None),
          taxableThreshold = currentThreshold
        )

        val expectedRow = SummaryRow(
          "vatDetails.startDate",
          "pages.summary.vatDetails.mandatoryStartDate",
          Some(features.returns.controllers.routes.ReturnsController.voluntaryStartPage())
        )

        builder.startDateRow mustBe expectedRow
      }

      "a Companies House incorporation date message, if it's a mandatory registration and they are not incorped" in {
        val builder = SummaryVatDetailsSectionBuilder(
          Some(generateTradingDetails()),
          optMandatoryRegistration,
          returnsBlock = returnsWithStartDate(None),
          taxableThreshold = currentThreshold
        )
        builder.startDateRow mustBe SummaryRow("vatDetails.startDate", "pages.summary.vatDetails.mandatoryStartDate", None)
      }

      "a date with format 'd MMMM y' if it's a voluntary registration where they are incorped" in {
        val builder = SummaryVatDetailsSectionBuilder(
          threshold = optVoluntaryRegistration,
          incorpDate = Some(LocalDate.of(2017, 1, 21)),
          returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21))),
          taxableThreshold = currentThreshold
        )

        val expectedRow = SummaryRow(
          "vatDetails.startDate",
          "21 March 2017",
          Some(features.returns.controllers.routes.ReturnsController.voluntaryStartPage())
        )

        builder.startDateRow mustBe expectedRow
      }

      "a date with format 'd MMMM y' if it's a mandatory registration where they are incorped and picked an earlier date" in {
        val builder = SummaryVatDetailsSectionBuilder(
          threshold = optMandatoryRegistration,
          incorpDate = Some(LocalDate.of(2017, 3, 21)),
          returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 4, 26))),
          taxableThreshold = currentThreshold
        )

        val expectedRow = SummaryRow(
          "vatDetails.startDate",
          "26 April 2017",
          Some(features.returns.controllers.routes.ReturnsController.mandatoryStartPage())
        )

        builder.startDateRow mustBe expectedRow
      }

      "a date with format 'd MMMM y' if it's a mandatory registration where they are incorped and picked the latest date possible" in {
        val builder = SummaryVatDetailsSectionBuilder(
          threshold = optMandatoryRegistration,
          incorpDate = Some(LocalDate.of(2017, 3, 21)),
          returnsBlock = returnsWithStartDate(None),
          taxableThreshold = currentThreshold
        )

        builder.startDateRow mustBe SummaryRow(
          "vatDetails.startDate",
          "pages.summary.vatDetails.mandatoryStartDate",
          Some(features.returns.controllers.routes.ReturnsController.mandatoryStartPage())
        )
      }
    }
    "with tradingNameRow render" should {

      "a trading name if there's one" in {
        val builder = SummaryVatDetailsSectionBuilder(
          tradingDetails = Some(generateTradingDetails()),
          threshold = optVoluntaryRegistration,
          returnsBlock = returnsWithStartDate(),
          taxableThreshold = currentThreshold
        )
        builder.tradingNameRow mustBe SummaryRow(
          "vatDetails.tradingName",
          "ACME Ltd.",
          Some(controllers.routes.TradingDetailsController.tradingNamePage())
        )
      }

      "a 'No' if there isn't a trading name" in {
        val builder = SummaryVatDetailsSectionBuilder(
          threshold = optVoluntaryRegistration,
          returnsBlock = Some(Returns(None, None, None, None)),
          taxableThreshold = currentThreshold
        )

        builder.tradingNameRow mustBe SummaryRow("vatDetails.tradingName", "app.common.no", Some(controllers.routes.TradingDetailsController.tradingNamePage()))
      }

      "an error should be thrown if no threshold is present" in {
        def builder = SummaryVatDetailsSectionBuilder(
          threshold = None,
          returnsBlock = Some(Returns(None, None, None, None)),
          taxableThreshold = currentThreshold
        )

        intercept[IllegalStateException](builder)
      }
    }

    "with section generate" should {

      "a valid summary section" in {
        val builder = SummaryVatDetailsSectionBuilder(tradingDetails = Some(generateTradingDetails()), threshold = optVoluntaryRegistration, returnsBlock = returnsWithStartDate(), taxableThreshold = currentThreshold)
        builder.section.id mustBe "vatDetails"
        builder.section.rows.length mustEqual 9
      }
    }

  }
}
