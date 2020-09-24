/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.EmailPasscodeForm
import org.jsoup.Jsoup
import views.html.capture_email_passcode

class CaptureEmailPasscodeViewSpec extends VatRegViewSpec {

  val testEmail = "test@test.com"
  val title = "Enter code to confirm your email address"
  val heading = "Enter code to confirm your email address"
  val paragraph = s"We have sent a code to: $testEmail."
  val insetText = "If you use a browser to access your email, you may need to open a new window or tab to see the code."
  val label = "Confirmation code"
  val hint = "For example, DNCLRK"
  val buttonText = "Continue"

  "Capture Email Passcode Page" should {

    val view = app.injector.instanceOf[capture_email_passcode].apply(testEmail, testCall, EmailPasscodeForm.form)

    val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(title) // TODO review titles as they seem to be missing a message key 'site.govuk'
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe heading
    }

    "have the correct paragraph" in {
      doc.getElementById("sent-email").text() mustBe paragraph
    }

    "have the correct inset text" in {
      doc.select(Selectors.indent).text() mustBe insetText
    }

    "have the correct label" in {
      doc.select(Selectors.label).text() mustBe label
    }

    "have the correct hint" in {
      doc.select(Selectors.hint).text() mustBe hint
    }

    "have the correct button" in {
      doc.select(Selectors.button).text() mustBe buttonText
    }

  }

}
