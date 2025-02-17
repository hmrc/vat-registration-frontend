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
import models.DateSelection._
import java.time.LocalDate
import play.api.data.Form
import play.api.i18n.{Lang, Messages, MessagesApi}
import testHelpers.VatRegSpec
import utils.workingdays._


import java.time.LocalDate

class VoluntaryDateFormIncorpSpec extends VatRegSpec {
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

  val incorpDate: LocalDate = LocalDate.of(2021, 1, 1)

  implicit val bhs: BankHolidaySet = BankHolidaySet("england-and-wales", List(
    BankHoliday(title = "Good Friday",            date = LocalDate.of(2022, 4, 15)),
    BankHoliday(title = "Easter Monday",          date = LocalDate.of(2022, 4, 18)),
    BankHoliday(title = "Early May bank holiday", date = LocalDate.of(2022, 5, 2)),
    BankHoliday(title = "Spring bank holiday",    date = LocalDate.of(2022, 6, 2)),
    BankHoliday(title = "Platinum Jubilee bank holiday",    date = LocalDate.of(2022, 6, 3)),
    BankHoliday(title = "Summer bank holiday",    date = LocalDate.of(2022, 8, 29)),
    BankHoliday(title = "Christmas Day",          date = LocalDate.of(2022, 12, 26)),
    BankHoliday(title = "Boxing Day",             date = LocalDate.of(2022, 12, 27)),
    BankHoliday(title = "New Year's Day",         date = LocalDate.of(2023, 1, 2))
  ))

  val form: Form[(DateSelection.Value, Option[LocalDate])] = VoluntaryDateFormIncorp.form(incorpDate)

  "Binding MandatoryDateForm" should {
    "Bind successfully for an incorp date selection" in {
      val data = Map(
        "value"  -> "company_registration_date",
        "startDate"       -> ""
      )
      form.bind(data).get mustBe ((company_registration_date, None))
    }

    "Bind successfully for a business start date selection" in {
      val data = Map(
        "value"  -> "business_start_date",
        "startDate"       -> ""
      )
      form.bind(data).get mustBe ((business_start_date, None))
    }

    "Bind successfully for a valid specific date selection" in {
      val data = Map(
        "value"  -> "specific_date",
        "startDate.day"   -> "5",
        "startDate.month" -> "1",
        "startDate.year"  -> "2022"
      )
      form.bind(data).get mustBe((specific_date, Some(LocalDate.of(2022, 1, 5))))
    }

    "Fail to bind successfully for no selection" in {
      val data = Map(
        "value"  -> "",
        "startDate"       -> ""
      )
      val bound = form.bind(data)
      bound.errors.size         mustBe 1
      bound.errors.head.key     mustBe "value"
      bound.errors.head.message mustBe "validation.startDate.choice.missing"
    }

    "Fail to bind successfully for an invalid selection" in {
      val data = Map(
        "value"  -> "invalidSelection",
        "startDate"       -> ""
      )
      val bound = form.bind(data)
      bound.errors.size         mustBe 1
      bound.errors.head.key     mustBe "value"
      bound.errors.head.message mustBe "validation.startDate.choice.missing"
    }
  }
}
