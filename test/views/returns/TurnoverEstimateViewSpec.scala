/*
 * Copyright 2022 HM Revenue & Customs
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

package views.returns

import forms.TurnoverEstimateForm
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.returns.TurnoverEstimate

class TurnoverEstimateViewSpec extends VatRegViewSpec {

  val header = "What do you think the business’s VAT-taxable turnover will be for the next 12 months?"
  val title = s"$header - Register for VAT - GOV.UK"
  val text = "Include the sale of all goods and services that are not exempt from VAT. You must include goods and services that have a 0% VAT rate."
  val link = "Find out more about which goods and services are exempt from VAT (opens in new tab)"
  val label = "Turnover estimate"
  val buttonText = "Save and continue"

  "Turnver Estimate Page" must {
    val form = TurnoverEstimateForm.form
    val view = app.injector.instanceOf[TurnoverEstimate].apply(form)
    implicit val doc = Jsoup.parse(view.body)

    "have the correct title" in new ViewSetup {
      doc.title() mustBe title
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(header)
    }

    "have the correct text" in new ViewSetup {
      doc.para(1) mustBe Some(text)
    }

    "have the correct link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(link, "https://www.gov.uk/guidance/rates-of-vat-on-different-goods-and-services"))
    }

    "have the correct label" in new ViewSetup {
      doc.select(Selectors.label).text must include(label)
    }

    "have the correct button" in new ViewSetup {
      doc.submitButton mustBe Some(buttonText)
    }
  }
}
