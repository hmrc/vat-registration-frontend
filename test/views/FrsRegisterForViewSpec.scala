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

import forms.genericForms.YesOrNoFormFactory
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.frs_register_for

class FrsRegisterForViewSpec extends VatRegViewSpec {

  val form = YesOrNoFormFactory.form()("frs.registerFor")
  val view = app.injector.instanceOf[frs_register_for]
  implicit val doc: Document = Jsoup.parse(view(form).body)

  val heading = "To join the Flat Rate Scheme the business must use the 16.5% flat rate"
  val para = "This is because the business doesn’t spend enough (including VAT) on relevant goods."
  val indentText = "Businesses in their first year of VAT registration can take an extra 1% off their flat rate."
  val subheading = "Do you want the business to join the Flat Rate Scheme with this rate?"
  val continue = "Save and continue"
  val yes = "Yes"
  val no = "No"

  "The FRS Your flat rate page" must {

    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.select(Selectors.h1).text() mustBe heading
    }

    "have the correct paragraph" in new ViewSetup {
      doc.select(Selectors.p(1)).text() mustBe para
    }

    "have the correct indent text" in new ViewSetup {
      doc.select(Selectors.indent).first().text() mustBe indentText
    }

    "have the correct subheading" in new ViewSetup {
      doc.select(Selectors.h2(1)).text() mustBe subheading
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(yes)
      doc.radio("false") mustBe Some(no)
    }

    "have a continue button" in new ViewSetup {
      doc.submitButton mustBe Some(continue)
    }
  }

}