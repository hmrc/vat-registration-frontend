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

package forms.vatapplication

import models.DateSelection
import models.DateSelection.{business_start_date, company_registration_date, specific_date}
import play.api.data.Form
import play.api.i18n.{Lang, Messages, MessagesApi}
import testHelpers.VatRegSpec
import utils.workingdays._
import java.time.LocalDate

class VoluntaryDateFormSpec extends VatRegSpec {
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

  val now: LocalDate = LocalDate.of(2018, 1, 2)

  val validDate: LocalDate = now.plusDays(10)
  val lowerLimitDate: LocalDate = now.plusDays(2)
  val upperLimitDate: LocalDate = now.plusMonths(3)

  implicit val bhs: BankHolidaySet = {

    BankHolidaySet("england-and-wales", List(
      BankHoliday(title = "Good Friday",            date = LocalDate.of(2017, 4, 14)),
      BankHoliday(title = "Easter Monday",          date = LocalDate.of(2017, 4, 17)),
      BankHoliday(title = "Early May bank holiday", date = LocalDate.of(2017, 5, 1)),
      BankHoliday(title = "Spring bank holiday",    date = LocalDate.of(2017, 5, 29)),
      BankHoliday(title = "Summer bank holiday",    date = LocalDate.of(2017, 8, 28)),
      BankHoliday(title = "Christmas Day",          date = LocalDate.of(2017, 12, 25)),
      BankHoliday(title = "Boxing Day",             date = LocalDate.of(2017, 12, 26)),
      BankHoliday(title = "New Year's Day",         date = LocalDate.of(2018, 1, 1))
    ))
  }

  val form: Form[(DateSelection.Value, Option[LocalDate])] = VoluntaryDateForm.form(now.plusDays(2), now.plusMonths(3))

  "Binding VoluntaryDateForm" should {
    "Bind successfully for an incorp date selection" in {
      val data = Map(
        "value" -> "company_registration_date",
        "startDate" -> ""
      )
      form.bind(data).get mustBe ((company_registration_date, None))
    }

    "Bind successfully for a business start date selection" in {
      val data = Map(
        "value" -> "business_start_date",
        "startDate" -> ""
      )
      form.bind(data).get mustBe ((business_start_date, None))
    }

    "Bind successfully with a date input" in {
      val data = Map(
        "value"  -> "specific_date",
        "startDate.day"   -> s"${validDate.getDayOfMonth}",
        "startDate.month" -> s"${validDate.getMonthValue}",
        "startDate.year"  -> s"${validDate.getYear}"
      )
      form.bind(data).get mustBe ((specific_date, Some(validDate)))
    }

    "Bind successfully with a date 2 days from now" in {
      val data = Map(
        "value" -> "specific_date",
        "startDate.day" -> s"${lowerLimitDate.getDayOfMonth}",
        "startDate.month" -> s"${lowerLimitDate.getMonthValue}",
        "startDate.year" -> s"${lowerLimitDate.getYear}"
      )
      form.bind(data).get mustBe ((specific_date, Some(lowerLimitDate)))
    }

    "Bind successfully with a date 3 months from now" in {
      val data = Map(
        "value" -> "specific_date",
        "startDate.day" -> s"${upperLimitDate.getDayOfMonth}",
        "startDate.month" -> s"${upperLimitDate.getMonthValue}",
        "startDate.year" -> s"${upperLimitDate.getYear}"
      )
      form.bind(data).get mustBe ((specific_date, Some(upperLimitDate)))
    }

    "Fail to bind successfully for no input" in {
      val data = Map(
        "value" -> "specific_date",
        "startDate.day" -> "",
        "startDate.month" -> "",
        "startDate.year" -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "startDate"
      bound.errors.head.message mustBe "validation.startDate.missing"
    }

    "Fail to bind successfully for an invalid input" in {
      val data = Map(
        "value" -> "specific_date",
        "startDate.day" -> "INVALID",
        "startDate.month" -> "INVALID",
        "startDate.year" -> "INVALID"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "startDate"
      bound.errors.head.message mustBe "validation.startDate.invalid"
    }

    "Fail to bind for an invalid selection" in {
      val data = Map(
        "value" -> "invalid_selection",
        "startDate.day" -> s"${validDate.getDayOfMonth}",
        "startDate.month" -> s"${validDate.getMonthValue}",
        "startDate.year" -> s"${validDate.getYear}"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "value"
      bound.errors.head.message mustBe "validation.startDate.choice.missing"
    }
  }
}