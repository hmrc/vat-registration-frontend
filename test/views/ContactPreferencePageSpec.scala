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

import forms.ContactPreferenceForm
import org.jsoup.Jsoup
import views.html.contact_preference

class ContactPreferencePageSpec extends VatRegViewSpec {


  val title = "How should we contact the business about VAT?"
  val heading = "How should we contact the business about VAT?"
  val paragraph = "We can send you an email when you have a new message about VAT."
  val paragraph2 = "We may still need to send you letters if this is the only service available or if the law requires us to do so."
  val email = "email"
  val letter = "letter"
  val buttonText = "Continue"

  "Contact Preference Page" should {
    lazy val form = ContactPreferenceForm()
    lazy val view = contact_preference(form, testCall)
    lazy val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(title)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text mustBe heading
    }

    "have the correct paragraph" in {
      doc.getElementById("paragraph").text mustBe paragraph
    }

    "have the correct paragraph2" in {
      doc.getElementById("paragraph2").text mustBe paragraph2
    }

    "have the correct continue button" in {
      doc.getElementById("continue").text mustBe buttonText
    }

  }

}