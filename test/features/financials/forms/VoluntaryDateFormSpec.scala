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

package features.financials.forms

import java.time.LocalDate

import features.financials.models.DateSelection.{business_start_date, company_registration_date, specific_date}
import forms.VoluntaryDateForm
import forms.VoluntaryDateForm._
import forms.VoluntaryDateFormIncorp.{VOLUNTARY_DATE, VOLUNTARY_SELECTION}
import helpers.VatRegSpec

class VoluntaryDateFormSpec extends VatRegSpec {

  val form = VoluntaryDateForm.form

  val now: LocalDate = LocalDate.now()

  val validDate: LocalDate = now.plusDays(10)
  val lowerLimitDate: LocalDate = now.plusDays(2)
  val upperLimitDate: LocalDate = now.plusMonths(3)

  "Binding VoluntaryDateForm" should {
    "Bind successfully for an incorp date selection" in {
      val data = Map(
        VOLUNTARY_SELECTION -> "company_registration_date",
        VOLUNTARY_DATE -> ""
      )
      form.bind(data).get mustBe (company_registration_date, None)
    }

    "Bind successfully for a business start date selection" in {
      val data = Map(
        VOLUNTARY_SELECTION -> "business_start_date",
        VOLUNTARY_DATE -> ""
      )
      form.bind(data).get mustBe (business_start_date, None)
    }

    "Bind successfully with a date input" in {
      val data = Map(
        VOLUNTARY_SELECTION -> "specific_date",
        s"$VOLUNTARY_DATE.day" -> s"${validDate.getDayOfMonth}",
        s"$VOLUNTARY_DATE.month" -> s"${validDate.getMonthValue}",
        s"$VOLUNTARY_DATE.year" -> s"${validDate.getYear}"
      )
      form.bind(data).get mustBe (specific_date, Some(validDate))
    }

    "Bind successfully with a date 2 days from now" in {
      val data = Map(
        VOLUNTARY_SELECTION -> "specific_date",
        s"$VOLUNTARY_DATE.day" -> s"${lowerLimitDate.getDayOfMonth}",
        s"$VOLUNTARY_DATE.month" -> s"${lowerLimitDate.getMonthValue}",
        s"$VOLUNTARY_DATE.year" -> s"${lowerLimitDate.getYear}"
      )
      form.bind(data).get mustBe (specific_date, Some(lowerLimitDate))
    }

    "Bind successfully with a date 3 months from now" in {
      val data = Map(
        VOLUNTARY_SELECTION -> "specific_date",
        s"$VOLUNTARY_DATE.day" -> s"${upperLimitDate.getDayOfMonth}",
        s"$VOLUNTARY_DATE.month" -> s"${upperLimitDate.getMonthValue}",
        s"$VOLUNTARY_DATE.year" -> s"${upperLimitDate.getYear}"
      )
      form.bind(data).get mustBe (specific_date, Some(upperLimitDate))
    }

    "Fail to bind successfully for no input" in {
      val data = Map(
        VOLUNTARY_SELECTION -> "specific_date",
        s"$VOLUNTARY_DATE.day" -> "",
        s"$VOLUNTARY_DATE.month" -> "",
        s"$VOLUNTARY_DATE.year" -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe VOLUNTARY_DATE
      bound.errors.head.message mustBe dateEmptyKey
    }

    "Fail to bind successfully for an invalid input" in {
      val data = Map(
        VOLUNTARY_SELECTION -> "specific_date",
        s"$VOLUNTARY_DATE.day" -> "INVALID",
        s"$VOLUNTARY_DATE.month" -> "INVALID",
        s"$VOLUNTARY_DATE.year" -> "INVALID"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe VOLUNTARY_DATE
      bound.errors.head.message mustBe dateInvalidKey
    }
  }
}