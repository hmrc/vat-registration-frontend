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

import forms.EnterBankAccountDetailsForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.enter_company_bank_account_details

class CompanyBankDetailsViewSpec extends VatRegViewSpec {

  val view: enter_company_bank_account_details = app.injector.instanceOf[enter_company_bank_account_details]

  val title = "What are the business’s bank or building society account details?"
  val heading = "What are the business’s bank or building society account details?"
  val p1 = "HMRC VAT will only use this account to send VAT repayments. We will not take money from it."
  val panelText = "You must tell us if your account details change."
  val accountName = "Account name"
  val accountNumber = "Account number"
  val accountNumberHint = "Must be between 6 and 8 digits long"
  val sortCode = "Sort code"
  val sortCodeHint = "Must be 6 digits long"
  val buttonText = "Save and continue"

  "Company Bank Details Page" should {
    lazy val doc: Document = Jsoup.parse(view(EnterBankAccountDetailsForm.form).body)

    "have the correct title" in {
      doc.title must include(title)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text mustBe heading
    }

    "have the correct p1" in {
      doc.select(Selectors.p(1)).text mustBe p1
    }

    "have the correct panel text" in {
      doc.select(Selectors.indent).text mustBe panelText
    }

    "have the correct Account Name label text" in {
      doc.select(Selectors.label).get(0).text mustBe accountName
    }

    "have the correct Account Number label text" in {
      doc.select(Selectors.label).get(1).text mustBe accountNumber
    }

    "have the correct Account Number Hint text" in {
      doc.select(Selectors.multipleHints(1)).get(1).text mustBe accountNumberHint
    }

    "have the correct Sort Code label text" in {
      doc.select(Selectors.label).get(2).text mustBe sortCode
    }

    "have the correct Sort Code Hint text" in {
      doc.select(Selectors.multipleHints(1)).get(2).text mustBe sortCodeHint
    }

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe buttonText
    }

  }

}
