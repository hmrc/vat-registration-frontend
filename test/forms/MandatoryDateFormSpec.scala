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

import models.DateSelection
import models.DateSelection._
import play.api.data.Form
import testHelpers.VatRegSpec

import java.time.LocalDate

class MandatoryDateFormSpec extends VatRegSpec {
  val incorpDate: LocalDate = LocalDate.now.minusYears(3)
  val oldIncorpDate: LocalDate = LocalDate.of(2010, 1, 1)
  val calculatedDate: LocalDate = LocalDate.now().minusYears(2).withMonth(12).withDayOfMonth(12)

  val form: Form[(DateSelection.Value, Option[LocalDate])] = MandatoryDateForm.form(incorpDate, calculatedDate)
  val oldIncorpForm: Form[(DateSelection.Value, Option[LocalDate])] = MandatoryDateForm.form(oldIncorpDate, calculatedDate)

  "Binding MandatoryDateForm" should {
    "Bind successfully for a calculated date selection" in {
      val data = Map(
        "value" -> DateSelection.calculated_date.toString
      )

      form.bind(data).get mustBe(calculated_date, None)
    }

    "Fail to bind successfully for no selection" in {
      val data = Map(
        "value" -> "",
        "date" -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "value"
      bound.errors.head.message mustBe "validation.startDate.choice.missing"
    }

    "Fail to bind successfully for an invalid selection" in {
      val data = Map(
        "value" -> "invalidSelection",
        "date" -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "value"
      bound.errors.head.message mustBe "validation.startDate.choice.missing"
    }

    "Bind successfully for a valid specific date selection" in {
      val testYear = LocalDate.now().minusYears(2).getYear

      val data = Map(
        "value" -> DateSelection.specific_date.toString,
        "date.day" -> "5",
        "date.month" -> "5",
        "date.year" -> testYear.toString
      )

      form.bind(data).get mustBe(specific_date, Some(LocalDate.of(testYear, 5, 5)))
    }

    "Bind successfully if the specified date is on the incorporation date" in {
      val data = Map(
        "value" -> DateSelection.specific_date.toString,
        "date.day" -> s"${incorpDate.getDayOfMonth}",
        "date.month" -> s"${incorpDate.getMonthValue}",
        "date.year" -> s"${incorpDate.getYear}"
      )
      form.bind(data).get mustBe(specific_date, Some(incorpDate))
    }

    "Bind successfully if the specified date is on the calculated date" in {
      val data = Map(
        "value" -> DateSelection.specific_date.toString,
        "date.day" -> calculatedDate.getDayOfMonth.toString,
        "date.month" -> calculatedDate.getMonthValue.toString,
        "date.year" -> calculatedDate.getYear.toString
      )
      form.bind(data).get mustBe(specific_date, Some(calculatedDate))
    }

    "Fail to bind if the date specified is before the incorp date" in {
      val data = Map(
        "value" -> DateSelection.specific_date.toString,
        "date.day" -> "5",
        "date.month" -> "5",
        "date.year" -> "2011"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "date"
      bound.errors.head.message mustBe "validation.startDate.range"
    }

    "Fail to bind if the date specified is after the calculated date" in {
      val data = Map(
        "value" -> DateSelection.specific_date.toString,
        "date.day" -> "30",
        "date.month" -> "12",
        "date.year" -> "2017"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "date"
      bound.errors.head.message mustBe "validation.startDate.range"
    }

    "Fail to bind if the date specified is not within 4 years" in {
      val data = Map(
        "value" -> DateSelection.specific_date.toString,
        "date.day" -> "30",
        "date.month" -> "12",
        "date.year" -> "2012"
      )
      val bound = oldIncorpForm.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "date"
      bound.errors.head.message mustBe "validation.startDateManIncorp.range.below4y"
    }

    "Fail to bind successfully if the date is empty" in {
      val data = Map(
        "value" -> DateSelection.specific_date.toString,
        "date.day" -> "",
        "date.month" -> "",
        "date.year" -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "date"
      bound.errors.head.message mustBe "validation.startDate.missing"
    }

    "Fail to bind successfully if the date is invalid" in {
      val data = Map(
        "value" -> DateSelection.specific_date.toString,
        "date.day" -> "50",
        "date.month" -> "76",
        "date.year" -> "1"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "date"
      bound.errors.head.message mustBe "validation.startDate.invalid"
    }
  }
}
