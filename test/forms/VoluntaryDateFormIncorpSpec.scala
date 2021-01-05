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

package forms

import java.time.LocalDate

import models.DateSelection
import models.DateSelection._
import org.joda.time.{LocalDate => JodaLocalDate}
import play.api.data.Form
import testHelpers.VatRegSpec
import uk.gov.hmrc.time.workingdays.{BankHoliday, BankHolidaySet}

class VoluntaryDateFormIncorpSpec extends VatRegSpec {
  val incorpDate: LocalDate = LocalDate.of(2018, 1, 1)

  implicit val bhs: BankHolidaySet = BankHolidaySet("england-and-wales", List(
    BankHoliday(title = "Good Friday",            date = new JodaLocalDate(2017, 4, 14)),
    BankHoliday(title = "Easter Monday",          date = new JodaLocalDate(2017, 4, 17)),
    BankHoliday(title = "Early May bank holiday", date = new JodaLocalDate(2017, 5, 1)),
    BankHoliday(title = "Spring bank holiday",    date = new JodaLocalDate(2017, 5, 29)),
    BankHoliday(title = "Summer bank holiday",    date = new JodaLocalDate(2017, 8, 28)),
    BankHoliday(title = "Christmas Day",          date = new JodaLocalDate(2017, 12, 25)),
    BankHoliday(title = "Boxing Day",             date = new JodaLocalDate(2017, 12, 26)),
    BankHoliday(title = "New Year's Day",         date = new JodaLocalDate(2018, 1, 1))
  ))

  val form: Form[(DateSelection.Value, Option[LocalDate])] = VoluntaryDateFormIncorp.form(incorpDate)

  "Binding MandatoryDateForm" should {
    "Bind successfully for an incorp date selection" in {
      val data = Map(
        "startDateRadio"  -> "company_registration_date",
        "startDate"       -> ""
      )
      form.bind(data).get mustBe (company_registration_date, None)
    }

    "Bind successfully for a business start date selection" in {
      val data = Map(
        "startDateRadio"  -> "business_start_date",
        "startDate"       -> ""
      )
      form.bind(data).get mustBe (business_start_date, None)
    }

    "Bind successfully for a valid specific date selection" in {
      val data = Map(
        "startDateRadio"  -> "specific_date",
        "startDate.day"   -> "5",
        "startDate.month" -> "1",
        "startDate.year"  -> "2018"
      )
      form.bind(data).get mustBe(specific_date, Some(LocalDate.of(2018, 1, 5)))
    }

    "Fail to bind successfully for no selection" in {
      val data = Map(
        "startDateRadio"  -> "",
        "startDate"       -> ""
      )
      val bound = form.bind(data)
      bound.errors.size         mustBe 1
      bound.errors.head.key     mustBe "startDateRadio"
      bound.errors.head.message mustBe "validation.startDate.choice.missing"
    }

    "Fail to bind successfully for an invalid selection" in {
      val data = Map(
        "startDateRadio"  -> "invalidSelection",
        "startDate"       -> ""
      )
      val bound = form.bind(data)
      bound.errors.size         mustBe 1
      bound.errors.head.key     mustBe "startDateRadio"
      bound.errors.head.message mustBe "validation.startDate.choice.missing"
    }
  }
}
