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

package views.vatapplication

import forms.vatapplication.MandatoryDateForm
import models.DateSelection
import models.DateSelection.{calculated_date, specific_date}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import utils.MessageDateFormat
import views.VatRegViewSpec
import views.html.vatapplication.MandatoryStartDateIncorpView

import java.time.LocalDate

class MandatoryStartDateIncorpViewSpec extends VatRegViewSpec {

  val view: MandatoryStartDateIncorpView = app.injector.instanceOf[MandatoryStartDateIncorpView]
  val calculatedDate: LocalDate = LocalDate.now()
  val incorpDate: LocalDate = LocalDate.now().minusYears(2)
  val formattedDate: String = MessageDateFormat.format(calculatedDate)
  val form: Form[(DateSelection.Value, Option[LocalDate])] = MandatoryDateForm.form(incorpDate, calculatedDate)

  implicit val doc: Document = Jsoup.parse(view(form, formattedDate).body)

  object ExpectedMessages {
    val heading = "When would you like your VAT registration date to start?"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para = s"You must start your VAT registration date from ($formattedDate)"
    val radio1: String = formattedDate
    val radio2 = "A different date"
    val hint = "This must be on or after the date of incorporation or within the last 4 years."
  }

  "the Mandatory Start Date page" must {
    "have a backlink" in new ViewSetup {
      doc.hasBackLink mustBe true
    }
    "have the correct page title" in new ViewSetup {
      doc.title must include(ExpectedMessages.heading)
    }
    "have the correct page heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have the correct para" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedMessages.para)
    }
    "have the correct radios" in new ViewSetup {
      doc.radio(calculated_date.toString) mustBe Some(ExpectedMessages.radio1)
      doc.radio(specific_date.toString) mustBe Some(ExpectedMessages.radio2)
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
