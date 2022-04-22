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

package views.otherbusinessinvolvements

import forms.OtherBusinessInvolvementForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.otherbusinessinvolvements.OtherBusinessInvolvement

class OtherBusinessInvolvementViewSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[OtherBusinessInvolvement]
  implicit val doc: Document = Jsoup.parse(view(OtherBusinessInvolvementForm.form).body)

  object ExpectedContent {
    val heading = "Have you or any of the partners or directors in this business been involved in any other businesses in the last 2 years?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val p1 = "Select ‘yes’ if any of the partners or directors in the business have acted as a sole trader, partner or director in any other business in the United Kingdom or Isle of Man in the last 2 years."

    val yes = "Yes"
    val no = "No"
    val continue = "Save and continue"
  }

  "the Other Business Involvement page" must {
    "have the correct paragraph" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.p1)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
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