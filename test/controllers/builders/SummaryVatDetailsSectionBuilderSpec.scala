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
    "with startDateRow render" should {
      "a date with format 'd MMMM y' if it's a voluntary registration where they are not incorped" in {
        val builder = SummaryVatDetailsSectionBuilder(
          threshold = optVoluntaryRegistration,
          returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21)))
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
          returnsBlock = returnsWithStartDate(None)
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
          optMandatoryRegistrationThirtyDays,
          returnsBlock = returnsWithStartDate(None)
        )
        builder.startDateRow mustBe SummaryRow("vatDetails.startDate", "pages.summary.vatDetails.mandatoryStartDate", None)
      }

      "a date with format 'd MMMM y' if it's a voluntary registration where they are incorped" in {
        val builder = SummaryVatDetailsSectionBuilder(
          threshold = optVoluntaryRegistration,
          incorpDate = Some(LocalDate.of(2017, 1, 21)),
          returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 3, 21)))
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
          threshold = optMandatoryRegistrationThirtyDays,
          incorpDate = Some(LocalDate.of(2017, 3, 21)),
          returnsBlock = returnsWithStartDate(Some(LocalDate.of(2017, 4, 26)))
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
          threshold = optMandatoryRegistrationThirtyDays,
          incorpDate = Some(LocalDate.of(2017, 3, 21)),
          returnsBlock = returnsWithStartDate(None)
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
          returnsBlock = returnsWithStartDate()
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
          returnsBlock = Some(Returns(None, None, None, None))
        )

        builder.tradingNameRow mustBe SummaryRow("vatDetails.tradingName", "app.common.no", Some(controllers.routes.TradingDetailsController.tradingNamePage()))
      }

      "an error should be thrown if no threshold is present" in {
        def builder = SummaryVatDetailsSectionBuilder(
          threshold = None,
          returnsBlock = Some(Returns(None, None, None, None))
        )

        intercept[IllegalStateException](builder)
      }
    }

    "with section generate" should {
      "a valid summary section" in {
        val builder = SummaryVatDetailsSectionBuilder(tradingDetails = Some(generateTradingDetails()), threshold = optVoluntaryRegistration, returnsBlock = returnsWithStartDate())
        builder.section.id mustBe "vatDetails"
        builder.section.rows.length mustEqual 2
      }
    }
  }
}
