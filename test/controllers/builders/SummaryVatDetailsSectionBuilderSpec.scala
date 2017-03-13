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

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.VatChoice
import models.view.SummaryRow
import org.joda.time.DateTime

import org.joda.time.format.DateTimeFormat

class SummaryVatDetailsSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  "The section builder composing a VatDetails section" should {

    "with taxableTurnoverRow render" should {

      "a 'No' if it's a voluntary registration" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(necessity = VatChoice.NECESSITY_VOLUNTARY))
        builder.taxableTurnoverRow mustBe SummaryRow("vatDetails.taxableTurnover", "app.common.no", Some(controllers.userJourney.routes.TaxableTurnoverController.show()))
      }

      "a 'Yes' if it's a mandatory registration" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(necessity = VatChoice.NECESSITY_OBLIGATORY))
        builder.taxableTurnoverRow mustBe SummaryRow("vatDetails.taxableTurnover", "app.common.yes", Some(controllers.userJourney.routes.TaxableTurnoverController.show()))
      }
    }

    "with necessityRow render" should {

      "a 'Yes' if it's a voluntary registration" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(necessity = VatChoice.NECESSITY_VOLUNTARY))
        builder.necessityRow mustBe SummaryRow("vatDetails.necessity", "app.common.yes", Some(controllers.userJourney.routes.VoluntaryRegistrationController.show()))
      }

      "a 'No' if it's a mandatory registration" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(necessity = VatChoice.NECESSITY_OBLIGATORY))
        builder.necessityRow mustBe SummaryRow("vatDetails.necessity", "app.common.no", None)
      }
    }

    "with startDateRow render" should {

      "a date with format 'd MMMM y' if it's a voluntary registration" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(necessity = VatChoice.NECESSITY_VOLUNTARY))
        builder.startDateRow mustBe SummaryRow("vatDetails.startDate", DateTime.now().toString("d MMMM y"), Some(controllers.userJourney.routes.StartDateController.show()))
      }

      "a Companies House incorporation date message, if it's a voluntary registration and the date is a default date" in {
        val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        val startDate = DateTime.parse("31/12/1969", formatter)
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(startDate = startDate, necessity = VatChoice.NECESSITY_VOLUNTARY))
        builder.startDateRow mustBe SummaryRow("vatDetails.startDate", "pages.summary.vatDetails.mandatoryStartDate", Some(controllers.userJourney.routes.StartDateController.show()))
      }

      "a Companies House incorporation date message, if it's a mandatory registration" in {
        val builder = SummaryVatDetailsSectionBuilder(VatChoice(necessity = VatChoice.NECESSITY_OBLIGATORY))
        builder.startDateRow mustBe SummaryRow("vatDetails.startDate", "pages.summary.vatDetails.mandatoryStartDate", None)
      }
    }

  }
}
