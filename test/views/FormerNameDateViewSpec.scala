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

import config.FrontendAppConfig
import forms.FormerNameDateForm
import models.view.FormerNameDateView
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import testHelpers.VatRegSpec
import views.html.former_name_date

import java.time.LocalDate

class FormerNameDateViewSpec extends VatRegViewSpec {

  lazy val view: former_name_date = app.injector.instanceOf[former_name_date]
  val testApplicantDob: LocalDate = LocalDate.of(2020, 1, 1)
  val testName = "testName"
  lazy val form: Form[FormerNameDateView] = FormerNameDateForm.form(testApplicantDob)
  implicit val doc: Document = Jsoup.parse(view(form, testName).body)

  val heading = "When did you change your name?"
  val title = s"$heading - Register for VAT - GOV.UK"
  val para = "This could be if you got married or changed your name by deed poll."
  val hint = "For example, 31 3 2006."
  val continue = "Save and continue"

  "Former Name Page" should {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
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
