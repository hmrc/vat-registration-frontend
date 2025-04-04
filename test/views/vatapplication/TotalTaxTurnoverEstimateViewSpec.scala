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

import forms.TotalTaxTurnoverEstimateForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.vatapplication.TotalTaxTurnoverEstimate

class TotalTaxTurnoverEstimateViewSpec extends VatRegViewSpec {

  val heading = "Total estimated VAT taxable turnover"
  val title = s"$heading - Register for VAT - GOV.UK"
  val para1Text = "We have calculated your total estimated VAT taxable turnover based on the information you have provided."
  val optionText = "No, I want to change my answer"
  val buttonText = "Save and continue"

  "Total Tax Turnver Estimate Page" must {
    val view = app.injector.instanceOf[TotalTaxTurnoverEstimate]
    implicit val doc: Document = Jsoup.parse(view(new TotalTaxTurnoverEstimateForm().apply(),
                                                          Some("£100"), Some("£200"), Some("£300"), Some("£600")).body)
    "have the correct title" in new ViewSetup {
      doc.title() mustBe title
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct first para" in new ViewSetup {
      doc.para(1) mustBe Some(para1Text)
    }

    "have the correct option" in new ViewSetup {
      doc.select(Selectors.label).text must include(optionText)
    }

    "have the correct button" in new ViewSetup {
      doc.submitButton mustBe Some(buttonText)
    }
  }
}
