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

package views

import forms.EstimateTotalSalesForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import views.html.estimate_total_sales

class EstimateTotalSalesViewSpec extends VatRegViewSpec {

  val header = "Estimate the business’s total sales, including VAT, for the next 3 months"
  val title = s"$header - Register for VAT - GOV.UK"
  val text = "Give figures that are as realistic as possible."
  val label = "Enter the estimate"
  val buttonText = "Save and continue"

  val form: Form[Long] = EstimateTotalSalesForm.form
  val view: Html = app.injector.instanceOf[estimate_total_sales].apply(form)
  implicit val doc: Document = Jsoup.parse(view.body)

  "Zero Rated Supplies Page" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe title
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(header)
    }

    "have the correct text" in new ViewSetup {
      doc.para(1) mustBe Some(text)
    }

    "have the correct label" in new ViewSetup {
      doc.textBox("totalSalesEstimate") mustBe Some(label)
    }

    "have the correct button" in new ViewSetup {
      doc.submitButton mustBe Some(buttonText)
    }
  }
}
