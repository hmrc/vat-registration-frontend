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

package views.returns

import forms.VoluntaryStartDateNoChoiceForm
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.returns.VoluntaryStartDateNoChoice

class VoluntaryStartDateNoChoiceViewSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[VoluntaryStartDateNoChoice]
  val form = app.injector.instanceOf[VoluntaryStartDateNoChoiceForm]
  val exampleDate = "example date"

  implicit val doc = Jsoup.parse(view(form(), exampleDate).body)

  object ExpectedMessages {
    val heading = "What do you want the business’s VAT start date to be"
    val para = "You can choose the date the business becomes VAT registered when you register voluntarily. This could be up to 4 years ago or within the next 3 months"
    val panel = "Once the business is successfully registered for VAT you cannot change its start date"
    val hint = s"For example, $exampleDate"
  }

  "the Voluntart Start Date (no date choice) page" must {
    "have the correct page title" in new ViewSetup {
      doc.title must include(ExpectedMessages.heading)
    }
    "have the correct page heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have the correct first paragraph" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedMessages.para)
    }
    "have the correct panel indent text" in new ViewSetup {
      doc.panelIndent(1) mustBe Some(ExpectedMessages.panel)
    }
    "have the correct date input" in new ViewSetup {
      doc.dateInput(1) mustBe Some(DateField(
        legend = ExpectedMessages.heading,
        hint = Some(ExpectedMessages.hint)
      ))
    }
    "have a submit button" in new ViewSetup {
      doc.submitButton.isDefined mustBe true
    }
  }

}
