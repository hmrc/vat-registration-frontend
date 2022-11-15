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

import forms.TelephoneNumberForm
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.applicant.capture_telephone_number

class CaptureTelephoneNumberViewSpec extends VatRegViewSpec {


  val heading = "What is your telephone number?"
  val title = s"$heading - Register for VAT - GOV.UK"
  val namedHeading = "What is testFirstName’s telephone number?"
  val paragraph = "We may need to contact you about the application."
  val buttonText = "Save and continue"
  val name = "testFirstName"

  "Capture Telephone Number Page" should {
    lazy val form = TelephoneNumberForm.form
    lazy val view = app.injector.instanceOf[capture_telephone_number].apply(testCall, form, None)
    implicit lazy val doc = Jsoup.parse(view.body)
    lazy val transactorView = app.injector.instanceOf[capture_telephone_number].apply(testCall, form, Some(name))
    lazy val transactorDoc = Jsoup.parse(transactorView.body)

    "have the correct title" in new ViewSetup {
      doc.title mustBe title
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct heading when the user is a transactor" in new ViewSetup()(transactorDoc) {
      doc.heading mustBe Some(namedHeading)
    }

    "have the correct label" in {
      doc.select(Selectors.label).text mustBe heading
    }

    "have the correct label when the user is a transactor" in {
      transactorDoc.select(Selectors.label).text mustBe namedHeading
    }

    "have the correct paragraph" in {
      doc.getElementById("telephone-number-collection-reason").text mustBe paragraph
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(buttonText)
    }

  }

}
