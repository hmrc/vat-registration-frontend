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

import helpers.VatRegSpec
import models.api.{VatChoice, VatTradingDetails}
import models.view.SummaryRow
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class SummaryVatDetailsSectionBuilderSpec extends VatRegSpec {

  "The section builder composing a vat details section" should {

    "with taxableTurnoverRow render" should {

      "a 'No' if it's a voluntary registration" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(necessity = VatChoice.NECESSITY_VOLUNTARY), VatTradingDetails())
        builder.taxableTurnoverRow mustBe SummaryRow("vatDetails.taxableTurnover", "app.common.no", Some(controllers.userJourney.vatChoice.routes.TaxableTurnoverController.show()))
      }

      "a 'Yes' if it's a mandatory registration" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(necessity = VatChoice.NECESSITY_OBLIGATORY), VatTradingDetails())
        builder.taxableTurnoverRow mustBe SummaryRow("vatDetails.taxableTurnover", "app.common.yes", Some(controllers.userJourney.vatChoice.routes.TaxableTurnoverController.show()))
      }
    }

    "with necessityRow render" should {

      "a 'Yes' if it's a voluntary registration" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(necessity = VatChoice.NECESSITY_VOLUNTARY), VatTradingDetails())
        builder.necessityRow mustBe SummaryRow("vatDetails.necessity", "app.common.yes", Some(controllers.userJourney.vatChoice.routes.VoluntaryRegistrationController.show()))
      }

      "a 'No' if it's a mandatory registration" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(necessity = VatChoice.NECESSITY_OBLIGATORY), VatTradingDetails())
        builder.necessityRow mustBe SummaryRow("vatDetails.necessity", "app.common.no", None)
      }
    }

    "with startDateRow render" should {

      "a date with format 'd MMMM y' if it's a voluntary registration" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(necessity = VatChoice.NECESSITY_VOLUNTARY), VatTradingDetails())
        builder.startDateRow mustBe SummaryRow("vatDetails.startDate", DateTime.now().toString("d MMMM y"), Some(controllers.userJourney.vatChoice.routes.StartDateController.show()))
      }

      "a Companies House incorporation date message, if it's a voluntary registration and the date is a default date" in {
        val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        val startDate = DateTime.parse("31/12/1969", formatter)
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(startDate = startDate, necessity = VatChoice.NECESSITY_VOLUNTARY), VatTradingDetails())
        builder.startDateRow mustBe SummaryRow("vatDetails.startDate", "pages.summary.vatDetails.mandatoryStartDate", Some(controllers.userJourney.vatChoice.routes.StartDateController.show()))
      }

      "a Companies House incorporation date message, if it's a mandatory registration" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(necessity = VatChoice.NECESSITY_OBLIGATORY), VatTradingDetails())
        builder.startDateRow mustBe SummaryRow("vatDetails.startDate", "pages.summary.vatDetails.mandatoryStartDate", None)
      }
    }

    "with tradingNameRow render" should {

      "a trading name if there's one" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(), VatTradingDetails(tradingName = "ACME Ltd."))
        builder.tradingNameRow mustBe SummaryRow("vatDetails.tradingName", "ACME Ltd.", Some(controllers.userJourney.vatTradingDetails.routes.TradingNameController.show()))
      }

      "a 'No' if there isn't a trading name" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(), VatTradingDetails())
        builder.tradingNameRow mustBe SummaryRow("vatDetails.tradingName", "app.common.no", Some(controllers.userJourney.vatTradingDetails.routes.TradingNameController.show()))
      }
    }

    "with section generate" should {

      "a valid summary section" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(), VatTradingDetails())
        builder.section.id mustBe "vatDetails"
        builder.section.rows.length mustEqual 4
      }
    }

  }
}
