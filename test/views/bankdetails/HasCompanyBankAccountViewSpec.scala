/*
 * Copyright 2023 HM Revenue & Customs
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

import forms.HasCompanyBankAccountForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.bankdetails.HasCompanyBankAccountView

class HasCompanyBankAccountViewSpec extends VatRegViewSpec {

  implicit val doc: Document = Jsoup.parse(view(HasCompanyBankAccountForm.form).body)
  lazy val view: HasCompanyBankAccountView = app.injector.instanceOf[HasCompanyBankAccountView]

  val heading = "Are you able to provide bank or building society account details for the business?"
  val title = s"$heading - Register for VAT - GOV.UK"
  val para = "If we owe the business money, we can repay this directly into your bank by BACS. This is faster and more secure than HMRC cheques."
  val para2 = "The account does not have to be a dedicated business account but it must be:"
  val bullet1 = "separate from a personal account"
  val bullet2 = "in the name of the registered person or company"
  val bullet3 = "in the UK"
  val bullet4 = "able to receive BACS payments"
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
      doc.para(2) mustBe Some(para2)
    }

    "have correct bullets" in new ViewSetup {
      doc.unorderedList(1) mustBe
        List(
          bullet1,
          bullet2,
          bullet3,
          bullet4
        )
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
