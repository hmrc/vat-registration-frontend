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

import forms.NoUKBankAccountForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.bankdetails.NoUkBankAccount

class NoUKBankAccountViewSpec extends VatRegViewSpec {

  val view: NoUkBankAccount = app.injector.instanceOf[NoUkBankAccount]
  implicit val doc: Document = Jsoup.parse(view(NoUKBankAccountForm.form).body)

  object ExpectedContent {

    val title: String = "Why are you unable to provide bank account details for the business? - Register for VAT - GOV.UK"
    val heading: String = "Why are you unable to provide bank account details for the business?"
    val button1: String = "It is being set up but is taking a while"
    val newButton1: String = "The bank account is still being set up (for example, the name is being changed from a sole trader to a limited company)"
    val button2: String = "The business has an overseas bank account"
    val button3: String = "The name is being changed (for example, from sole trader to limited company)"
    val newButton3: String = "The bank account is not in the business name"
    val newButton4: String = "I do not want to provide the business bank account details"
    val error: String = "Select why you cannot provide bank account details for the business"
    val continue: String = "Save and continue"
    val continueLater: String = "Save and come back later"
  }

  "No UK Bank Account Page" should {
    "have the correct title" in new ViewSetup() {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct heading" in new ViewSetup() {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct button1" in new ViewSetup() {
      doc.radio("beingSetup") mustBe Some(ExpectedContent.newButton1)
    }

    "have the correct button2" in new ViewSetup() {
      doc.radio("overseasAccount") mustBe Some(ExpectedContent.button2)
    }

    "have the correct button3" in new ViewSetup() {
      doc.radio("accountNotInBusinessName") mustBe Some(ExpectedContent.newButton3)
    }

    "have the correct button4" in new ViewSetup() {
      doc.radio("dontWantToProvide") mustBe Some(ExpectedContent.newButton4)
    }

    "have the correct continue button" in new ViewSetup() {
      doc.select(Selectors.button).get(0).text mustBe ExpectedContent.continue
    }

    "have a save and continue button" in {
      doc.select(Selectors.saveProgressButton).text mustBe ExpectedContent.continueLater
    }
  }

}
