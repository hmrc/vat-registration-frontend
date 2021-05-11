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

import forms.EmailAddressForm
import org.jsoup.Jsoup
import views.html.capture_email_address

class CaptureEmailAddressViewSpec extends VatRegViewSpec {

  val title = "What is your email address?"
  val heading = "What is your email address?"
  val paragraph = "We use this to send you communications and updates about your VAT"
  val privacyInformation = "Full details of how we use your information are in the HMRC Privacy Notice (opens in new tab)."
  val buttonText = "Save and continue"

  "Capture Email Address Page" should {
    val form = EmailAddressForm.form
    val view = app.injector.instanceOf[capture_email_address].apply(testCall, form)

    val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(title)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text mustBe heading
    }

    "have the correct paragraph" in {
      doc.getElementById("use-of-email").text mustBe paragraph
    }

    "have the correct privacy information" in {
      doc.getElementById("privacy-information").text mustBe privacyInformation
    }

    "have the correct continue button" in {
      doc.select(Selectors.button).text mustBe buttonText
    }

  }

}
