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

import helpers.FormInspectors._
import models.FRSDateChoice
import testHelpers.VatRegSpec

class FlatRateFormSpec extends VatRegSpec {
  val minDate: LocalDate = LocalDate.of(2018, 5, 1)
  val now: LocalDate = LocalDate.of(2018, 5, 28)
  val maxDate: LocalDate = now.plusMonths(3)
  val testform = FRSStartDateForm.form(minDate, maxDate)

  "FRSStartDateForm form" should {
    "be valid" when {
      "the date entered is equal to min date" in {
        val data = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
          "frsStartDate.day" -> Seq(minDate.getDayOfMonth.toString),
          "frsStartDate.month" -> Seq(minDate.getMonthValue.toString),
          "frsStartDate.year" -> Seq(minDate.getYear.toString))
        testform.bindFromRequest(data) shouldContainValue ((FRSDateChoice.DifferentDate,Some(minDate)))
      }
      "the date entered is after min date" in {
        val laterDate = minDate.plusDays(1)
        val data = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
          "frsStartDate.day" -> Seq(laterDate.getDayOfMonth.toString),
          "frsStartDate.month" -> Seq(laterDate.getMonthValue.toString),
          "frsStartDate.year" -> Seq(laterDate.getYear.toString))
        testform.bindFromRequest(data) shouldContainValue ((FRSDateChoice.DifferentDate,Some(laterDate)))
      }
      "the date given is equal to the max date" in {
        val data = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
          "frsStartDate.day" -> Seq(maxDate.getDayOfMonth.toString),
          "frsStartDate.month" -> Seq(maxDate.getMonthValue.toString),
          "frsStartDate.year" -> Seq(maxDate.getYear.toString))
        testform.bindFromRequest(data) shouldContainValue ((FRSDateChoice.DifferentDate, Some(maxDate)))
      }
    }
    "be invalid" when {
      "the date is before the min date" in {
        val data = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
          "frsStartDate.day" -> Seq(minDate.minusDays(1).getDayOfMonth.toString),
          "frsStartDate.month" -> Seq(minDate.minusMonths(1).getMonthValue.toString),
          "frsStartDate.year" -> Seq(minDate.getYear.toString))
        testform.bindFromRequest(data) shouldHaveErrors Seq("frsStartDate" -> "validation.frs.startDate.range.below.vatStartDate")
      }
      "the date exceeds the max date" in {
        val data = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
          "frsStartDate.day" -> Seq(maxDate.plusDays(1).getDayOfMonth.toString),
          "frsStartDate.month" -> Seq(maxDate.getMonthValue.toString),
          "frsStartDate.year" -> Seq(maxDate.getYear.toString))
        testform.bindFromRequest(data) shouldHaveErrors Seq("frsStartDate" -> "validation.frs.startDate.range.after.maxDate")
      }
    }
  }
}
