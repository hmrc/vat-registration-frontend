/*
 * Copyright 2020 HM Revenue & Customs
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

package views

import org.jsoup.Jsoup
import play.api.data.Form
import play.api.data.Forms.{boolean, single}
import views.html.claim_refunds_view

class ClaimRefundsViewSpec extends VatRegViewSpec {

  val form = Form(single("value" -> boolean))
  val view = app.injector.instanceOf[claim_refunds_view]
  implicit val doc = Jsoup.parse(view(form).body)

  object ExpectedContent {
    val heading = "Do you expect the business to regularly claim VAT refunds from HMRC?"
    val title = "Do you expect the business to regularly claim VAT refunds from HMRC?"
    val para1 = "Most businesses do not claim VAT refunds. It is only possible when the VAT a business pays on " +
      "business-related purchases is more than the VAT it charges customers."
    val detailsSummary = "Show me an example"
    val detailsContent = "If a business sells mainly zero-rated items (the VAT on them is 0%), it may pay more VAT to " +
      "run its business than it can charge. For example, most books are zero-rated, so a bookshop may find itself in this situation."
    val label = "Select yes if you expect the business to regularly claim VAT refunds from HMRC"
    val continue = "Continue"
    val yes = "Yes"
    val no = "No"
  }

  "The charge expectancy (regularly claim refunds) page" must {
    "have a back link in new Setup" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have a progressive disclosure" in new ViewSetup {
      doc.details mustBe Some(Details(ExpectedContent.detailsSummary, ExpectedContent.detailsContent))
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(ExpectedContent.yes)
      doc.radio("false") mustBe Some(ExpectedContent.no)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
