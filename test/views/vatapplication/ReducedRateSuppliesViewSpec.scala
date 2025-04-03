/*
 * Copyright 2025 HM Revenue & Customs
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

package views.vatapplication

import forms.ReducedRateSuppliesForm
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.vatapplication.ReducedRateSupplies

class ReducedRateSuppliesViewSpec extends VatRegViewSpec {

  val heading = "What do you think the business’s reduced rate (5%) VAT taxable turnover will be for the next 12 months?"
  val title = s"$heading - Register for VAT - GOV.UK"
  val paraText = "Try to make your estimate as accurate as possible. It may be used to calculate how much VAT you owe if you do not submit your return on time."
  val link = "Find out more about VAT rates on different goods and services (opens in new tab)"
  val buttonText = "Save and continue"

  "ReducedRated Supplies Page" must {
    val form = ReducedRateSuppliesForm.form
    val view = app.injector.instanceOf[ReducedRateSupplies].apply(form)
    implicit val doc = Jsoup.parse(view.body)

    "have the correct title" in new ViewSetup {
      doc.title() mustBe title
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct para" in new ViewSetup {
      doc.para(1) mustBe Some(paraText)
    }

    "have the correct link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(link, "https://www.gov.uk/guidance/rates-of-vat-on-different-goods-and-services"))
    }

    "have the correct label" in new ViewSetup {
      doc.select(Selectors.label).text must include(heading)
    }

    "have the correct button" in new ViewSetup {
      doc.submitButton mustBe Some(buttonText)
    }
  }
}
