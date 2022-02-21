/*
 * Copyright 2022 HM Revenue & Customs
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

import services.mocks.TimeServiceMock
import testHelpers.VatRegSpec

class VoluntaryStartDateNoChoiceFormSpec extends VatRegSpec with TimeServiceMock {

  val form = new VoluntaryStartDateNoChoiceForm(mockTimeService)

  "The Voluntart Start Date form (no date selection)" when {
    "the date field is left blank" must {
      "return the blank field message" in {
        mockToday(testDate)

        val res = form().bind(Map(
          "startDate.day" -> "",
          "startDate.month" -> "",
          "startDate.year" -> ""
        ))

        res.errors.map(_.message).headOption mustBe Some("validation.startDate.missing")
      }
    }
    "the date is within the allowed range" must {
      "bind successfully on the earliest possible day" in {
        mockToday(testDate)

        val minDate = testDate.minusYears(4)

        val res = form().bind(Map(
          "startDate.day" -> minDate.getDayOfMonth.toString,
          "startDate.month" -> minDate.getMonthValue.toString,
          "startDate.year" -> minDate.getYear.toString
        ))

        res.errors.isEmpty mustBe true
      }
      "bind successfully on the latest possible day" in {
        mockToday(testDate)

        val maxDate = testDate.plusMonths(3)

        val res = form().bind(Map(
          "startDate.day" -> maxDate.getDayOfMonth.toString,
          "startDate.month" -> maxDate.getMonthValue.toString,
          "startDate.year" -> maxDate.getYear.toString
        ))

        res.errors.isEmpty mustBe true
      }
    }
    "the date is before the minimum allowed date" must {
      "return the minimum date error" in {
        mockToday(testDate)

        val minDate = testDate.minusYears(4)

        val res = form().bind(Map(
          "startDate.day" -> minDate.minusDays(1).getDayOfMonth.toString,
          "startDate.month" -> minDate.getMonthValue.toString,
          "startDate.year" -> minDate.getYear.toString
        ))

        res.errors.map(_.message).headOption mustBe Some("validation.startDate.range")
      }
    }
    "the date is after the minimum" must {
      "return the maximum date error" in {
        mockToday(testDate)

        val maxDate = testDate.plusMonths(3)

        val res = form().bind(Map(
          "startDate.day" -> maxDate.plusDays(1).getDayOfMonth.toString,
          "startDate.month" -> maxDate.getMonthValue.toString,
          "startDate.year" -> maxDate.getYear.toString
        ))

        res.errors.map(_.message).headOption mustBe Some("validation.startDate.range")
      }
    }
    "the date is invalid" when {
      "the day is missing" must {
        "return the missing date error message" in {
          mockToday(testDate)

          val res = form().bind(Map(
            "startDate.day" -> "",
            "startDate.month" -> "2",
            "startDate.year" -> "2022"
          ))

          res.errors.map(_.message).headOption mustBe Some("validation.startDate.missing")
        }
      }
      "the month is missing" must {
        "return the missing date error message" in {
          mockToday(testDate)

          val res = form().bind(Map(
            "startDate.day" -> "1",
            "startDate.month" -> "",
            "startDate.year" -> "2022"
          ))

          res.errors.map(_.message).headOption mustBe Some("validation.startDate.missing")
        }
      }
      "the year is missing" must {
        "return the missing date error message" in {
          mockToday(testDate)

          val res = form().bind(Map(
            "startDate.day" -> "1",
            "startDate.month" -> "2",
            "startDate.year" -> ""
          ))

          res.errors.map(_.message).headOption mustBe Some("validation.startDate.missing")
        }
      }
    }
  }


}
