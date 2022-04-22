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

package views.applicant

import featureswitch.core.config.{FeatureSwitching, SaveAndContinueLater}
import forms.TelephoneNumberForm
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.applicant.capture_telephone_number

class CaptureTelephoneNumberViewSpec extends VatRegViewSpec with FeatureSwitching {

  val title = "What is your telephone number?"
  val heading = "What is your telephone number?"
  val namedHeading = "What is testFirstName’s telephone number?"
  val paragraph = "We may need to contact you about the application."
  val buttonText = "Save and continue"
  val name = "testFirstName"

  disable(SaveAndContinueLater)

  "Capture Telephone Number Page" should {
    lazy val form = TelephoneNumberForm.form
    lazy val view = app.injector.instanceOf[capture_telephone_number].apply(testCall, form, None)
    lazy val doc = Jsoup.parse(view.body)
    lazy val transactorView = app.injector.instanceOf[capture_telephone_number].apply(testCall, form, Some(name))
    lazy val transactorDoc = Jsoup.parse(transactorView.body)

    "have the correct title" in {
      doc.title must include(title) // TODO review titles as they seem to be missing a message key 'site.govuk'
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text mustBe heading
    }

    "have the correct heading when the user is a transactor" in {
      transactorDoc.select(Selectors.h1).text mustBe namedHeading
    }

    "have the correct paragraph" in {
      doc.getElementById("telephone-number-collection-reason").text mustBe paragraph
    }

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe buttonText
    }

  }

}