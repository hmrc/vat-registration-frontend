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

package views.bankdetails

import featuretoggle.FeatureSwitch.UseNewBarsVerify
import featuretoggle.FeatureToggleSupport._
import forms.EnterBankAccountDetailsForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.bankdetails.EnterBankAccountDetails

class EnterBankDetailsViewSpec extends VatRegViewSpec {

  val view: EnterBankAccountDetails = app.injector.instanceOf[EnterBankAccountDetails]

  val title = "What are the business’s account details?"
  val heading = "What are the business’s account details?"
  val p1 = "HMRC VAT will only use this information to send VAT repayments. Money will not be taken from the account you supply."
  val panelText = "You must tell us if your account details change."
  val accountName = "Name on the account"
  val accountNumber = "Account number"
  val accountNumberHint = "Must be between 6 and 8 digits long"
  val sortCode = "Sort code"
  val sortCodeHint = "Must be 6 digits long"
  val rollNumber = "Building society roll number (if you have one)"
  val rollNumberHint = "You can find it on your card, statement or passbook"
  val buttonText = "Save and continue"

  implicit lazy val doc: Document = Jsoup.parse(view(EnterBankAccountDetailsForm.form).body)

  "Company Bank Details Page common elements" should {
    disable(UseNewBarsVerify)

    "have the correct title" in new ViewSetup {
      doc.title must include(title)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct p1" in new ViewSetup {
      doc.para(1) mustBe Some(p1)
    }

    "have the correct panel text" in new ViewSetup {
      doc.panelIndent(1) mustBe Some(panelText)
    }

    "have the correct Account Name label text" in {
      doc.select(Selectors.label).get(0).text mustBe accountName
    }

    "have the correct Account Number label text" in {
      doc.select(Selectors.label).get(1).text mustBe accountNumber
    }

    "have the correct Account Number Hint text" in new ViewSetup {
      doc.hintWithMultiple(2) mustBe Some(accountNumberHint)
    }

    "have the correct Sort Code label text" in {
      doc.select(Selectors.label).get(2).text mustBe sortCode
    }

    "have the correct Sort Code Hint text" in new ViewSetup {
      doc.hintWithMultiple(3) mustBe Some(sortCodeHint)
    }

    "have Roll Number label text" in new ViewSetup {
      doc.select(Selectors.label).get(3).text mustBe rollNumber
    }

    "have the correct Roll Number Hint text" in new ViewSetup {
      doc.hintWithMultiple(4) mustBe Some(rollNumberHint)
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(buttonText)
    }
  }
}
