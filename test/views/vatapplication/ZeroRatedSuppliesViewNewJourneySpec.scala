/*
 * Copyright 2024 HM Revenue & Customs
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
import views.html.vatapplication.ZeroRatedSuppliesNewJourney

class ZeroRatedSuppliesViewNewJourneySpec extends VatRegViewSpec {

  val title = "What do you think the business’s zero rate (0%) VAT Taxable Turnover will be for the next 12 months?"
  val heading = "What do you think the business’s zero rate (0%) VAT Taxable Turnover will be for the next 12 months?"
  val text = "Try to make your estimate as accurate as possible. It may be used to calculate how much VAT you owe if you do not submit your return on time."
  val summary = "What are zero-rated taxable goods and services?"
  val details = "Zero-rated goods and services are those which are still taxable as VAT but you do not charge your customers any VAT. You must still record these sales in your VAT software and report them to HMRC when you do your VAT Return."
  val link = "Find out about VAT rates on different goods and services (opens in new tab)"
  val label = "Zero rated turnover estimate"
  val buttonText = "Save and continue"

  "Zero Rated Supplies Page" must {
    val form = ZeroRatedSuppliesForm.form(10000)
    val view = app.injector.instanceOf[ZeroRatedSuppliesNewJourney].apply(testCall, form)
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
