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

import forms.EmailAddressForm
import org.jsoup.Jsoup
import views.html.capture_email_address

class CaptureEmailAddressViewSpec extends VatRegViewSpec {

  val title = "What is your email address?"
  val heading = "What is your email address?"
  val namedHeading = "What is testFirstName’s email address?"
  val paragraph = "We use this to send you communications and updates about your VAT"
  val privacyInformation = "Full details of how we use your information are in the HMRC Privacy Notice (opens in new tab)."
  val buttonText = "Save and continue"

  "Capture Email Address Page" should {
    val form = EmailAddressForm.form
    val name = "testFirstName"
    val nonTransactorView = app.injector.instanceOf[capture_email_address].apply(testCall, form, None)
    val transactorView = app.injector.instanceOf[capture_email_address].apply(testCall, form, Some(name))

    val nonTransactorDoc = Jsoup.parse(nonTransactorView.body)
    val transactorDoc = Jsoup.parse(transactorView.body)

    "have the correct title" in {
      nonTransactorDoc.title must include(title)
    }

    "have the correct heading" in {
      nonTransactorDoc.select(Selectors.h1).text mustBe heading
    }

    "have the correct heading when the user is a transactor" in {
      transactorDoc.select(Selectors.h1).text mustBe namedHeading
    }

    "have the correct paragraph" in {
      nonTransactorDoc.getElementById("use-of-email").text mustBe paragraph
    }

    "have the correct privacy information" in {
      nonTransactorDoc.getElementById("privacy-information").text mustBe privacyInformation
    }

    "have the correct continue button" in {
      nonTransactorDoc.select(Selectors.button).text mustBe buttonText
    }

  }

}
