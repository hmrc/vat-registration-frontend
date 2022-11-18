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

package views.vatapplication

import forms.TurnoverEstimateForm
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.vatapplication.TurnoverEstimate

class TurnoverEstimateViewSpec extends VatRegViewSpec {

  val heading = "What do you think the VAT-taxable turnover will be for the next 12 months?"
  val title = s"$heading - Register for VAT - GOV.UK"
  val para1Text = "Tell us the estimated value."
  val para2Text = "Include the sale of all goods and services that are not exempt from VAT. You must include goods and services that have a 0% VAT rate."
  val link = "Find out more about which goods and services are exempt from VAT (opens in new tab)"
  val buttonText = "Save and continue"

  "Turnver Estimate Page" must {
    val form = TurnoverEstimateForm.form
    val view = app.injector.instanceOf[TurnoverEstimate].apply(form)
    implicit val doc = Jsoup.parse(view.body)

    "have the correct title" in new ViewSetup {
      doc.title() mustBe title
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct first para" in new ViewSetup {
      doc.para(1) mustBe Some(para1Text)
    }

    "have the correct second para" in new ViewSetup {
      doc.para(2) mustBe Some(para2Text)
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
