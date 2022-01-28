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

import forms.ApplyForEoriForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.apply_for_eori

class ApplyForEoriViewSpec extends VatRegViewSpec {

  val view: apply_for_eori = app.injector.instanceOf[apply_for_eori]
  implicit val doc: Document = Jsoup.parse(view(ApplyForEoriForm.form).body)

  object ExpectedContent {
    val p1 = "The business may need an Economic Operators Registration and Identification number (EORI number) if it moves goods:"
    val bullet1 = "between the Isle of Man and any other country (including the EU)"
    val bullet2 = "between Great Britain and Northern Ireland"
    val bullet3 = "between Great Britain and the Channel Islands"
    val bullet4 = "between Northern Ireland and countries outside the EU"
    val p2 = "The business does not need an EORI number if it only moves goods on the island of Ireland or between an EU country and Northern Ireland."
    val linkText = "Find out more about EORI (opens in new tab)"
    val title = "Do you need an EORI number? - Register for VAT - GOV.UK"
    val subheading = "Company details"
    val heading = "Do you need an EORI number?"
    val yes = "Yes"
    val no = "No"
    val continue = "Save and continue"
  }

  "the Apply For Eori page" must {
    "have the correct paragraphs" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.p1)
      doc.para(2) mustBe Some(ExpectedContent.p2)
    }

    "have the correct bullets" in new ViewSetup {
      doc.unorderedList(1) mustBe
        List(
          ExpectedContent.bullet1,
          ExpectedContent.bullet2,
          ExpectedContent.bullet3,
          ExpectedContent.bullet4
        )
    }

    "have the correct link text" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.linkText, "https://www.gov.uk/eori"))
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct subheading" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedContent.subheading)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
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
