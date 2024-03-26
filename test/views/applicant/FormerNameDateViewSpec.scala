/*
 * Copyright 2024 HM Revenue & Customs
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

import forms.FormerNameDateForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.VatRegViewSpec
import views.html.applicant.FormerNameDate

import java.time.LocalDate

class FormerNameDateViewSpec extends VatRegViewSpec {

  val name = "testFirstName"
  lazy val view: FormerNameDate = app.injector.instanceOf[FormerNameDate]
  val testApplicantDob: LocalDate = LocalDate.of(2020, 1, 1)
  val testName = "testName"
  lazy val form: Form[LocalDate] = FormerNameDateForm.form(testApplicantDob)
  implicit val doc: Document = Jsoup.parse(view(form, testName, None).body)
  val transactorDoc: Document = Jsoup.parse(view(form, testName, Some(name)).body)

  val heading = "When did you change your name?"
  val namedHeading = "When did testFirstName change their name?"
  val title = s"$heading - Register for VAT - GOV.UK"
  val para = "This could be if you got married or changed your name by deed poll."
  val hint = "For example, 27 3 2007"
  val continue = "Save and continue"

  "Former Name Page" should {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct heading when the user is a transactor" in {
      transactorDoc.select(Selectors.h1).text mustBe namedHeading
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe title
    }

    "have a hint" in new ViewSetup {
      doc.hintText mustBe Some(hint)
    }

    "have a save and continue button" in new ViewSetup {
      doc.submitButton mustBe Some(continue)
    }
  }
}
