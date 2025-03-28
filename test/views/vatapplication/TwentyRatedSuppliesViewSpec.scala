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

import forms.TwentyRatedSuppliesForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.vatapplication.TwentyRatedSupplies

class TwentyRatedSuppliesViewSpec extends VatRegViewSpec {

  val title = "What do you think the business’s standard rate (20%) VAT taxable turnover will be for the next 12 months?"
  val heading = "What do you think the business’s standard rate (20%) VAT taxable turnover will be for the next 12 months?"
  val text = "Try to make your estimate as accurate as possible. It may be used to calculate how much VAT you owe if you do not submit your return on time."
  val link = "Find out more about calculating your VAT taxable turnover (opens in new tab)"
  val buttonText = "Save and continue"

  val linkWelshText = "Dylech geisio gwneud"


  "Twenty Rated Supplies Page" must {
    val form = TwentyRatedSuppliesForm.form
    val view = app.injector.instanceOf[TwentyRatedSupplies].apply(testCall, form)
    implicit val doc: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(title)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text must include(heading)
    }

    "have the correct text" in {
      doc.select(Selectors.p).text must include(text)
    }

    "have the correct link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(link, "https://www.gov.uk/register-for-vat#calculate-your-turnover"))
    }

    "have the correct label" in {
      doc.select(Selectors.label).text must include(heading)
    }

    "have the correct button" in {
      doc.select(Selectors.button).text must include(buttonText)
    }

    "render the welsh language toggle" in new ViewSetup() {
      assertContainsLink(doc, "Cymraeg", "/hmrc-frontend/language/cy")
    }
  }
}
