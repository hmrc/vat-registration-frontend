/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.HasCompanyBankAccountForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.has_company_bank_account

class HasCompanyBankAccountViewSpec extends VatRegViewSpec {

  implicit val doc: Document = Jsoup.parse(view(HasCompanyBankAccountForm.form).body)
  lazy val view: has_company_bank_account = app.injector.instanceOf[has_company_bank_account]

  val heading = "Does the business have a bank or building society account?"
  val title = s"$heading - Register for VAT - GOV.UK"
  val para = "The account does not have to be a dedicated business account but must be separate from a personal account. We will use this account for VAT repayments."
  val yes = "Yes"
  val no = "No"
  val continue = "Save and continue"

  "Has Bank Account Page" should {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe title
    }

    "have correct text" in new ViewSetup {
      doc.para(1) mustBe Some(para)
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(yes)
      doc.radio("false") mustBe Some(no)
    }

    "have a save and continue button" in new ViewSetup {
      doc.submitButton mustBe Some(continue)
    }
  }

}
