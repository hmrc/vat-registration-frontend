/*
 * Copyright 2018 HM Revenue & Customs
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

package features.returns.forms

import java.time.LocalDate

import features.returns.models.DateSelection._
import MandatoryDateForm._
import features.returns.models.DateSelection
import helpers.VatRegSpec
import play.api.data.Form

class MandatoryDateFormSpec extends VatRegSpec {

  val incorpDate: LocalDate = LocalDate.of(2016, 1, 1)
  val oldIncorpDate: LocalDate = LocalDate.of(2010, 1, 1)
  val calculatedDate: LocalDate = LocalDate.of(2017, 12, 12)

  val form: Form[(DateSelection.Value, Option[LocalDate])] = MandatoryDateForm.form(incorpDate, calculatedDate)
  val oldIncorpForm: Form[(DateSelection.Value, Option[LocalDate])] = MandatoryDateForm.form(oldIncorpDate, calculatedDate)

  "Binding MandatoryDateForm" should {
    "Bind successfully for a calculated date selection" in {
      val data = Map(
        MANDATORY_SELECTION -> "calculated_date",
        MANDATORY_DATE -> ""
      )
      form.bind(data).get mustBe (calculated_date, None)
    }

    "Fail to bind successfully for no selection" in {
      val data = Map(
        MANDATORY_SELECTION -> "",
        MANDATORY_DATE -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe MANDATORY_SELECTION
      bound.errors.head.message mustBe mandatorySelectionInvalidKey
    }

    "Fail to bind successfully for an invalid selection" in {
      val data = Map(
        MANDATORY_SELECTION -> "invalidSelection",
        MANDATORY_DATE -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe MANDATORY_SELECTION
      bound.errors.head.message mustBe mandatorySelectionInvalidKey
    }

    "Bind successfully for a valid specific date selection" in {
      val data = Map(
        MANDATORY_SELECTION -> "specific_date",
        s"$MANDATORY_DATE.day" -> "5",
        s"$MANDATORY_DATE.month" -> "5",
        s"$MANDATORY_DATE.year" -> "2017"
      )
      form.bind(data).get mustBe(specific_date, Some(LocalDate.of(2017, 5, 5)))
    }

    "Bind successfully if the specified date is on the incorporation date" in {
      val data = Map(
        MANDATORY_SELECTION -> "specific_date",
        s"$MANDATORY_DATE.day" -> "1",
        s"$MANDATORY_DATE.month" -> "1",
        s"$MANDATORY_DATE.year" -> "2016"
      )
      form.bind(data).get mustBe(specific_date, Some(LocalDate.of(2016, 1, 1)))
    }

    "Bind successfully if the specified date is on the calculated date" in {
      val data = Map(
        MANDATORY_SELECTION -> "specific_date",
        s"$MANDATORY_DATE.day" -> "12",
        s"$MANDATORY_DATE.month" -> "12",
        s"$MANDATORY_DATE.year" -> "2017"
      )
      form.bind(data).get mustBe(specific_date, Some(LocalDate.of(2017, 12, 12)))
    }

    "Fail to bind if the date specified is before the incorp date" in {
      val data = Map(
        MANDATORY_SELECTION -> "specific_date",
        s"$MANDATORY_DATE.day" -> "5",
        s"$MANDATORY_DATE.month" -> "5",
        s"$MANDATORY_DATE.year" -> "2011"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe MANDATORY_DATE
      bound.errors.head.message mustBe dateBelowIncorp
    }

    "Fail to bind if the date specified is after the calculated date" in {
      val data = Map(
        MANDATORY_SELECTION -> "specific_date",
        s"$MANDATORY_DATE.day" -> "30",
        s"$MANDATORY_DATE.month" -> "12",
        s"$MANDATORY_DATE.year" -> "2017"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe MANDATORY_DATE
      bound.errors.head.message mustBe dateAboveCalculated
    }

    "Fail to bind if the date specified is not within 4 years" in {
      val data = Map(
        MANDATORY_SELECTION -> "specific_date",
        s"$MANDATORY_DATE.day" -> "30",
        s"$MANDATORY_DATE.month" -> "12",
        s"$MANDATORY_DATE.year" -> "2012"
      )
      val bound = oldIncorpForm.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe MANDATORY_DATE
      bound.errors.head.message mustBe dateWithinFourYears
    }

    "Fail to bind successfully if the date is empty" in {
      val data = Map(
        MANDATORY_SELECTION -> "specific_date",
        s"$MANDATORY_DATE.day" -> "",
        s"$MANDATORY_DATE.month" -> "",
        s"$MANDATORY_DATE.year" -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe MANDATORY_DATE
      bound.errors.head.message mustBe dateEmptyKey
    }

    "Fail to bind successfully if the date is invalid" in {
      val data = Map(
        MANDATORY_SELECTION -> "specific_date",
        s"$MANDATORY_DATE.day" -> "50",
        s"$MANDATORY_DATE.month" -> "76",
        s"$MANDATORY_DATE.year" -> "1"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe MANDATORY_DATE
      bound.errors.head.message mustBe dateInvalidKey
    }
  }
}
