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

import featureswitch.core.config.{FeatureSwitching, SaveAndContinueLater}
import forms.NoUKBankAccountForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.no_uk_bank_account

class NoUKBankAccountPageSpec extends VatRegViewSpec with FeatureSwitching {

  val view: no_uk_bank_account = app.injector.instanceOf[no_uk_bank_account]
  implicit val doc: Document = Jsoup.parse(view(NoUKBankAccountForm.form).body)

  object ExpectedContent {

    val title: String = "Why is the bank account not set up? - Register for VAT - GOV.UK"
    val heading: String = "Why is the bank account not set up?"
    val button1: String = "It is being set up but is taking a while"
    val button2: String = "The business has an overseas bank account"
    val button3: String = "The name is being changed (for example, from sole trader to limited company)"
    val error: String = "Tell us why the bank account is not set up"
    val continue: String = "Continue"
    val continueLater: String = "Save and come back later"
  }

  "No UK Bank Account Page" should {

    enable(SaveAndContinueLater)

    val view: no_uk_bank_account = app.injector.instanceOf[no_uk_bank_account]
    implicit val doc: Document = Jsoup.parse(view(NoUKBankAccountForm.form).body)

    disable(SaveAndContinueLater)

    "have the correct title" in new ViewSetup() {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct heading" in new ViewSetup() {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct button1" in new ViewSetup() {
      doc.radio("beingSetup") mustBe Some(ExpectedContent.button1)
    }

    "have the correct button2" in new ViewSetup() {
      doc.radio("overseasAccount") mustBe Some(ExpectedContent.button2)
    }

    "have the correct button3" in new ViewSetup() {
      doc.radio("nameChange") mustBe Some(ExpectedContent.button3)
    }

    "have the correct continue button" in new ViewSetup() {
      doc.select(Selectors.button).get(0).text mustBe ExpectedContent.continue
    }

    "have a save and continue button when the FS is enabled" in {
      doc.select(Selectors.saveProgressButton).text mustBe ExpectedContent.continueLater
    }
  }

}
