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

import forms.FormerNameCaptureForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.FormerNameCapture

class FormerNameCaptureViewSpec extends VatRegViewSpec {

  val name = "testFirstName"
  lazy val view: FormerNameCapture = app.injector.instanceOf[FormerNameCapture]
  implicit val nonTransactorDoc: Document = Jsoup.parse(view(FormerNameCaptureForm.form, None).body)
  val transactorDoc: Document = Jsoup.parse(view(FormerNameCaptureForm.form, Some(name)).body)

  val heading = "What was your previous name?"
  val namedHeading = "What was testFirstName’s previous name?"
  val title = s"$heading - Register for VAT - GOV.UK"
  val firstLabel = "First name"
  val lastLabel = "Last name"
  val continue = "Save and continue"

  "Former Name Page" should {
    "have a back link" in new ViewSetup {
      nonTransactorDoc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      nonTransactorDoc.heading mustBe Some(heading)
    }

    "have the correct heading when the user is a transactor" in {
      transactorDoc.select(Selectors.h1).text mustBe namedHeading
    }

    "have the correct page title" in new ViewSetup {
      nonTransactorDoc.title mustBe title
    }

    "have a first name label" in new ViewSetup {
      nonTransactorDoc.textBox("formerFirstName") mustBe Some(firstLabel)
    }

    "have a last name label" in new ViewSetup {
      nonTransactorDoc.textBox("formerLastName") mustBe Some(lastLabel)
    }

    "have a save and continue button" in new ViewSetup {
      nonTransactorDoc.submitButton mustBe Some(continue)
    }
  }
}
