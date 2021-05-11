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
import forms.TelephoneNumberForm
import org.jsoup.Jsoup
import views.html.capture_telephone_number

class CaptureTelephoneNumberPageSpec extends VatRegViewSpec with FeatureSwitching {

  val title = "What is your telephone number?"
  val heading = "What is your telephone number?"
  val paragraph = "We may need to contact you about the application."
  val buttonText = "Save and continue"

  disable(SaveAndContinueLater)

  "Capture Telephone Number Page" should {
    lazy val form = TelephoneNumberForm.form
    lazy val view = app.injector.instanceOf[capture_telephone_number].apply(testCall, form)
    lazy val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(title) // TODO review titles as they seem to be missing a message key 'site.govuk'
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text mustBe heading
    }

    "have the correct paragraph" in {
      doc.getElementById("telephone-number-collection-reason").text mustBe paragraph
    }

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe buttonText
    }

  }

}
