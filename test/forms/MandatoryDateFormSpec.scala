/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.data.Form
import testHelpers.VatRegSpec

class MandatoryDateFormSpec extends VatRegSpec {
  val incorpDate: LocalDate = LocalDate.of(2016, 1, 1)
  val oldIncorpDate: LocalDate = LocalDate.of(2010, 1, 1)
  val calculatedDate: LocalDate = LocalDate.of(2017, 12, 12)

  val form: Form[(DateSelection.Value, Option[LocalDate])] = MandatoryDateForm.form(incorpDate, calculatedDate)
  val oldIncorpForm: Form[(DateSelection.Value, Option[LocalDate])] = MandatoryDateForm.form(oldIncorpDate, calculatedDate)

  "Binding MandatoryDateForm" should {
    "Bind successfully for a calculated date selection" in {
      val data = Map(
        "startDateRadio" -> "calculated_date"
      )

      form.bind(data).get mustBe (calculated_date, None)
    }

    "Fail to bind successfully for no selection" in {
      val data = Map(
        "startDateRadio" -> "",
        "startDate" -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "startDateRadio"
      bound.errors.head.message mustBe "validation.startDate.choice.missing"
    }

    "Fail to bind successfully for an invalid selection" in {
      val data = Map(
        "startDateRadio" -> "invalidSelection",
        "startDate" -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "startDateRadio"
      bound.errors.head.message mustBe "validation.startDate.choice.missing"
    }

    "Bind successfully for a valid specific date selection" in {
      val data = Map(
        "startDateRadio" -> "specific_date",
        "startDate.day" -> "5",
        "startDate.month" -> "5",
        "startDate.year" -> "2017"
      )
      form.bind(data).get mustBe(specific_date, Some(LocalDate.of(2017, 5, 5)))
    }

    "Bind successfully if the specified date is on the incorporation date" in {
      val data = Map(
        "startDateRadio" -> "specific_date",
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2017"
      )
      form.bind(data).get mustBe(specific_date, Some(LocalDate.of(2017, 1, 1)))
    }

    "Bind successfully if the specified date is on the calculated date" in {
      val data = Map(
        "startDateRadio" -> "specific_date",
        "startDate.day" -> "12",
        "startDate.month" -> "12",
        "startDate.year" -> "2017"
      )
      form.bind(data).get mustBe(specific_date, Some(LocalDate.of(2017, 12, 12)))
    }

    "Fail to bind if the date specified is before the incorp date" in {
      val data = Map(
        "startDateRadio" -> "specific_date",
        "startDate.day" -> "5",
        "startDate.month" -> "5",
        "startDate.year" -> "2011"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "startDate"
      bound.errors.head.message mustBe "validation.startDate.range"
    }

    "Fail to bind if the date specified is after the calculated date" in {
      val data = Map(
        "startDateRadio" -> "specific_date",
        "startDate.day" -> "30",
        "startDate.month" -> "12",
        "startDate.year" -> "2017"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "startDate"
      bound.errors.head.message mustBe "validation.startDate.range"
    }

    "Fail to bind if the date specified is not within 4 years" in {
      val data = Map(
        "startDateRadio" -> "specific_date",
        "startDate.day" -> "30",
        "startDate.month" -> "12",
        "startDate.year" -> "2012"
      )
      val bound = oldIncorpForm.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "startDate"
      bound.errors.head.message mustBe "validation.startDateManIncorp.range.below4y"
    }

    "Fail to bind successfully if the date is empty" in {
      val data = Map(
        "startDateRadio" -> "specific_date",
        "startDate.day" -> "",
        "startDate.month" -> "",
        "startDate.year" -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "startDate"
      bound.errors.head.message mustBe "validation.startDate.missing"
    }

    "Fail to bind successfully if the date is invalid" in {
      val data = Map(
        "startDateRadio" -> "specific_date",
        "startDate.day" -> "50",
        "startDate.month" -> "76",
        "startDate.year" -> "1"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "startDate"
      bound.errors.head.message mustBe "validation.startDate.invalid"
    }
  }
}
