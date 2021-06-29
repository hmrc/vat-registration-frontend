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


import forms.FRSStartDateForm
import models.FRSDateChoice
import org.jsoup.Jsoup
import views.html.frs_start_date

import java.time.LocalDate

class FrsStartDateViewSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[frs_start_date]
  val startDate = LocalDate.of(2021, 6, 30)
  val endDate = LocalDate.of(2021, 8, 30)

  implicit val doc = Jsoup.parse(view(
    FRSStartDateForm.form(minDate = startDate, maxDate = endDate),
    startDate.toString
  ).body)

  "the FRS start date page" must {
    "have a h1 heading" in new ViewSetup {
      doc.heading mustBe Some("When does the business want to join the Flat Rate Scheme?")
    }
    "have a page title that matches the h1 heading" in new ViewSetup {
      doc.title must include ("When does the business want to join the Flat Rate Scheme?")
    }
    "have options to choose the Vat Start Date or a different date" in new ViewSetup {
      doc.radio(FRSDateChoice.VATDate.toString) mustBe Some("The same date that it’s registered for VAT")
      doc.radio(FRSDateChoice.DifferentDate.toString) mustBe Some("On a different date")
    }
    "have a field to enter a different date" in new ViewSetup {
      doc.textBox("frsStartDate.day").isDefined mustBe true
      doc.textBox("frsStartDate.month").isDefined mustBe true
      doc.textBox("frsStartDate.year").isDefined mustBe true
    }
    "have a submit button" in new ViewSetup {
      doc.submitButton mustBe Some("Save and continue")
    }
  }

}
