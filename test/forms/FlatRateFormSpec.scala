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

import helpers.FormInspectors._
import models.FRSDateChoice
import testHelpers.VatRegSpec

class FlatRateFormSpec extends VatRegSpec {
  val minDate: LocalDate       = LocalDate.of(2018, 5, 29)
  val now: LocalDate           = LocalDate.of(2018, 5, 28)
  val testformNoVatStartDate   = FRSStartDateForm.form(minDate, None)
  val testformWithVatStartDate = FRSStartDateForm.form(minDate, Some(now.minusDays(1)))

  "FRSStartDateForm form" should {
    "be valid if date entered is equal to min date and user has not entered a vat start date " in {
      val data = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
        "frsStartDate.day" -> Seq(minDate.getDayOfMonth.toString),
        "frsStartDate.month" -> Seq(minDate.getMonthValue.toString),
        "frsStartDate.year" -> Seq(minDate.getYear.toString))
      testformNoVatStartDate.bindFromRequest(data) shouldContainValue ((FRSDateChoice.DifferentDate,Some(minDate)))
    }
    "be valid if date entered is after min date and user has not entered a vat start date " in {
      val laterDate = minDate.plusDays(1)
      val data = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
        "frsStartDate.day" -> Seq(laterDate.getDayOfMonth.toString),
        "frsStartDate.month" -> Seq(laterDate.getMonthValue.toString),
        "frsStartDate.year" -> Seq(laterDate.getYear.toString))
      testformNoVatStartDate.bindFromRequest(data) shouldContainValue ((FRSDateChoice.DifferentDate,Some(laterDate)))
    }
    "be invalid if date entered is before min date and user has not entered a vat start date" in {
      val earlyDate = LocalDate.of(2018, 5, 25)
      val data = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
        "frsStartDate.day" -> Seq(earlyDate.getDayOfMonth.toString),
        "frsStartDate.month" -> Seq(earlyDate.getMonthValue.toString),
        "frsStartDate.year" -> Seq(earlyDate.getYear.toString))
      testformNoVatStartDate.bindFromRequest(data) shouldHaveErrors Seq("frsStartDate" -> "validation.frs.startDate.range.below")
    }
    "be invalid if user does not provide complete date and user has not entered a vat start date" in {
      val data = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
        "frsStartDate.day" -> Seq("01"),
        "frsStartDate.month" -> Seq(""),
        "frsStartDate.year" -> Seq("2011"))
      testformNoVatStartDate.bindFromRequest(data) shouldHaveErrors Seq("frsStartDate" -> "validation.frs.startDate.missing")
    }
    "be invalid if date is in an invalid format and user has not entered a vat start date" in {
      val data = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
        "frsStartDate.day" -> Seq("01"),
        "frsStartDate.month" -> Seq("XXXXXXXX"),
        "frsStartDate.year" -> Seq("2011"))
      testformNoVatStartDate.bindFromRequest(data) shouldHaveErrors Seq("frsStartDate" -> "validation.frs.startDate.invalid")
    }
    "be valid if date is today as user has entered a vat start date" in {
      val data = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
        "frsStartDate.day" -> Seq(now.getDayOfMonth.toString),
        "frsStartDate.month" -> Seq(now.getMonthValue.toString),
        "frsStartDate.year" -> Seq(now.getYear.toString))
      testformWithVatStartDate.bindFromRequest(data) shouldContainValue ((FRSDateChoice.DifferentDate,Some(now)))
    }
    "be valid if date is 1 day after today as user has entered a vat start date" in {
      val oneDayInFuture = now.plusDays(1)
      val data = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
        "frsStartDate.day" -> Seq(oneDayInFuture.getDayOfMonth.toString),
        "frsStartDate.month" -> Seq(oneDayInFuture.getMonthValue.toString),
        "frsStartDate.year" -> Seq(oneDayInFuture.getYear.toString))
      testformWithVatStartDate.bindFromRequest(data) shouldContainValue ((FRSDateChoice.DifferentDate,Some(oneDayInFuture)))
    }
    "be valid if date entered is yesterday as vat start date is yesterday" in {
      val oneDayInPast = now.minusDays(1)
      val data = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
        "frsStartDate.day" -> Seq(oneDayInPast.getDayOfMonth.toString),
        "frsStartDate.month" -> Seq(oneDayInPast.getMonthValue.toString),
        "frsStartDate.year" -> Seq(oneDayInPast.getYear.toString))
      testformWithVatStartDate.bindFromRequest(data) shouldContainValue ((FRSDateChoice.DifferentDate,Some(oneDayInPast)))
    }
    "be invalid if date entered is two days in past as vat start date was yesterday" in {
      val twoDaysInPast = now.minusDays(2)
      val data = Map("frsStartDateRadio" -> Seq(FRSDateChoice.DifferentDate.toString),
        "frsStartDate.day" -> Seq(twoDaysInPast.getDayOfMonth.toString),
        "frsStartDate.month" -> Seq(twoDaysInPast.getMonthValue.toString),
        "frsStartDate.year" -> Seq(twoDaysInPast.getYear.toString))
      testformWithVatStartDate.bindFromRequest(data) shouldHaveErrors Seq("frsStartDate" -> "validation.frs.startDate.range.below.vatStartDate")
    }
  }
}
