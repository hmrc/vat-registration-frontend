/*
 * Copyright 2026 HM Revenue & Customs
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

package views.bankdetails

import forms.ChooseAccountTypeForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.bankdetails.ChooseAccountTypeView

class ChooseAccountTypeViewSpec extends VatRegViewSpec {

  lazy val view: ChooseAccountTypeView = app.injector.instanceOf[ChooseAccountTypeView]
  implicit val doc: Document           = Jsoup.parse(view(ChooseAccountTypeForm.form).body)

  val heading  = "What kind of bank or building society account will you use for VAT repayments?"
  val title    = s"$heading - Register for VAT - GOV.UK"
  val business = "Business account"
  val personal = "Personal account"
  val continue = "Save and continue"

  "ChooseAccountTypeView Page" should {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe title
    }

    "have a Business account radio option" in new ViewSetup {
      doc.radio("business") mustBe Some(business)
    }

    "have a Personal account radio option" in new ViewSetup {
      doc.radio("personal") mustBe Some(personal)
    }

    "have a save and continue button" in new ViewSetup {
      doc.submitButton mustBe Some(continue)
    }

    "display an error summary with the correct message when no option is selected" in new ViewSetup {
      val errorDoc: Document = Jsoup.parse(
        view(ChooseAccountTypeForm.form.withError("value", "validation.chooseAccountType.missing")).body
      )
      errorDoc.hasErrorSummary mustBe true
      errorDoc.errorSummaryLinks.head.text mustBe "Select business or personal account"
    }
  }
}
