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

import forms.OverseasBankAccountForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.overseas_bank_account

class OverseasBankAccountViewSpec extends VatRegViewSpec {

  val view: overseas_bank_account = app.injector.instanceOf[overseas_bank_account]

  val title = "Bank or building society account details"
  val heading = "Bank or building society account details"
  val accountName = "Name on the account"
  val bic = "BIC or SWIFT code"
  val bicHint = "Must be between 8 and 11 characters long. You can ask your bank or check your bank statement"
  val iban = "IBAN"
  val ibanHint = "You can ask your bank or check your bank statement"
  val buttonText = "Save and continue"

  "Overseas Bank Details Page" should {
    lazy val doc: Document = Jsoup.parse(view(OverseasBankAccountForm.form).body)

    "have the correct title" in {
      doc.title must include(title)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text mustBe heading
    }

    "have the correct Account Name label text" in {
      doc.select(Selectors.label).get(0).text mustBe accountName
    }

    "have the correct BIC label text" in {
      doc.select(Selectors.label).get(1).text mustBe bic
    }

    "have the correct BIC hint text" in {
      doc.select(Selectors.multipleHints(1)).get(1).text mustBe bicHint
    }

    "have the correct IBAN label text" in {
      doc.select(Selectors.label).get(2).text mustBe iban
    }

    "have the correct IBAN hint text" in {
      doc.select(Selectors.multipleHints(1)).get(2).text mustBe ibanHint
    }

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe buttonText
    }

  }

}
