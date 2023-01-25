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

package views.business

import forms.ContactPreferenceForm
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.business.ContactPreferenceView

class ContactPreferenceViewSpec extends VatRegViewSpec {

  val title = "How should we contact the business about VAT?"
  val heading = "How should we contact the business about VAT?"
  val paragraph = "We can send an email when there is a new message about VAT."
  val paragraph2 = "We may still need to send the business letters if this is the only service available or if the law requires us to do so."
  val email = "email"
  val letter = "letter"
  val buttonText = "Save and continue"
  val viewInstance = app.injector.instanceOf[ContactPreferenceView]

  "Contact Preference Page" should {
    lazy val form = ContactPreferenceForm()
    lazy val view = viewInstance(form, testCall)
    implicit lazy val doc = Jsoup.parse(view.body)

    "have the correct title" in new ViewSetup {
      doc.title must include(title)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct paragraph" in new ViewSetup {
      doc.para(1) mustBe Some(paragraph)
    }

    "have the correct paragraph2" in new ViewSetup {
      doc.para(2) mustBe Some(paragraph2)
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(buttonText)
    }

  }

}