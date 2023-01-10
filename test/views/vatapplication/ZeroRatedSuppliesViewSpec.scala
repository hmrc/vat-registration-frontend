/*
 * Copyright 2023 HM Revenue & Customs
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

import forms.ZeroRatedSuppliesForm
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.vatapplication.zero_rated_supplies

class ZeroRatedSuppliesViewSpec extends VatRegViewSpec {

  val title = "What will the value of the business’s zero-rated taxable goods be over the next 12 months?"
  val heading = "What will the value of the business’s zero-rated taxable goods be over the next 12 months?"
  val text = "Enter a value for all of the taxable goods which are zero-rated."
  val summary = "What are zero-rated taxable goods?"
  val details = "Zero-rated are goods which are still taxable as VAT but you do not charge your customers any VAT. You must still record these sales in your VAT software and report them to HMRC when you do your VAT Return."
  val link = "Find out about VAT rates on different goods and services (opens in new tab)"
  val label = "Zero rated turnover estimate"
  val buttonText = "Save and continue"

  "Zero Rated Supplies Page" must {
    val form = ZeroRatedSuppliesForm.form(10000)
    val view = app.injector.instanceOf[zero_rated_supplies].apply(testCall, form)
    implicit val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(title)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text must include(heading)
    }

    "have the correct text" in {
      doc.select(Selectors.p).text must include(text)
    }

    "have the correct details" in {
      doc.select(Selectors.detailsSummary).text must include(summary)
      doc.select(Selectors.detailsContent).text must include(details)
    }

    "have the correct link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(link, "https://www.gov.uk/guidance/rates-of-vat-on-different-goods-and-services"))
    }

    "have the correct label" in {
      doc.select(Selectors.label).text must include(heading)
    }

    "have the correct button" in {
      doc.select(Selectors.button).text must include(buttonText)
    }
  }
}
