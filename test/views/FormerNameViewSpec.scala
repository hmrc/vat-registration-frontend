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

import forms.FormerNameForm
import models.view.FormerNameView
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.former_name

class FormerNameViewSpec extends VatRegViewSpec {

  lazy val view: former_name = app.injector.instanceOf[former_name]
  lazy val form: Form[FormerNameView] = FormerNameForm.form
  implicit val doc: Document = Jsoup.parse(view(form).body)

  val heading = "Have you ever changed your name?"
  val title = s"$heading - Register for VAT - GOV.UK"
  val para = "This could be if you got married or changed your name by deed poll."
  val label = "Enter your former name"
  val hint = "First name and last name"
  val yes = "Yes"
  val no = "No"
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

    "have correct text" in new ViewSetup {
      doc.para(1) mustBe Some(para)
    }

    "have a textbox label" in new ViewSetup {
      doc.textBox("formerName") mustBe Some(label)
    }

    "have a hint" in new ViewSetup {
      doc.hintText mustBe Some(hint)
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(yes)
      doc.radio("false") mustBe Some(no)
    }

    "have a save and continue button" in new ViewSetup {
      doc.submitButton mustBe Some(continue)
    }
  }
}
