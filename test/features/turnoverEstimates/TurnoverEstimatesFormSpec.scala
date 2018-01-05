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

package features.turnoverEstimates

import forms.EstimateVatTurnoverForm
import forms.EstimateVatTurnoverForm._
import org.scalatestplus.play.PlaySpec

class TurnoverEstimatesFormSpec extends PlaySpec {

  "EstimateVatTurnoverForm" should {

    val form = EstimateVatTurnoverForm.form

    val overMaxLong = "9223372036854775808"
    val overMinLong = "-9223372036854775809"

    "successfully bind valid data to the form and return a Long" in {
      val formData = Map(
        TURNOVER_ESTIMATE -> "100"
      )

      val boundForm = form.bind(formData)
      boundForm.get mustBe 100L
    }

    "successfully bind a turnover estimate with leading zeroes to the form and return a Long without the leading zeroes" in {
      val formData = Map(
        TURNOVER_ESTIMATE -> "000100"
      )

      val boundForm = form.bind(formData)
      boundForm.get mustBe 100L
    }

    "successfully bind a turnover estimate with spaces either side of the number and return a long" in {
      val formData = Map(
        TURNOVER_ESTIMATE -> " 100 "
      )

      val boundForm = form.bind(formData)
      boundForm.get mustBe 100L
    }

    "fail to bind data to the form" when {

      "a turnover estimate is not provided" in {
        val formData = Map(
          TURNOVER_ESTIMATE -> ""
        )

        val boundForm = form.bind(formData)
        boundForm.errors.size mustBe 1
        boundForm.errors.head.key mustBe TURNOVER_ESTIMATE
        boundForm.errors.head.message mustBe estimateVatTurnoverMissing
      }

      "the provided turnover estimate is more than the maximum number of a Long" in {
        val formData = Map(
          TURNOVER_ESTIMATE -> overMaxLong
        )

        val boundForm = form.bind(formData)
        boundForm.errors.size mustBe 1
        boundForm.errors.head.key mustBe TURNOVER_ESTIMATE
        boundForm.errors.head.message mustBe estimateVatTurnoverTooHigh
      }

      "the provided turnover estimate is less than the minimum number of a Long" in {
        val formData = Map(
          TURNOVER_ESTIMATE -> overMinLong
        )

        val boundForm = form.bind(formData)
        boundForm.errors.size mustBe 1
        boundForm.errors.head.key mustBe TURNOVER_ESTIMATE
        boundForm.errors.head.message mustBe estimateVatTurnoverTooLow
      }

      "the provided turnover estimate is not a number" in {
        val formData = Map(
          TURNOVER_ESTIMATE -> "abc"
        )

        val boundForm = form.bind(formData)
        boundForm.errors.size mustBe 1
        boundForm.errors.head.key mustBe TURNOVER_ESTIMATE
        boundForm.errors.head.message mustBe estimateVatTurnoverInvalid
      }

      "the provided turnover estimate has a space in between the number" in {
        val formData = Map(
          TURNOVER_ESTIMATE -> "100 00"
        )

        val boundForm = form.bind(formData)
        boundForm.errors.size mustBe 1
        boundForm.errors.head.key mustBe TURNOVER_ESTIMATE
        boundForm.errors.head.message mustBe estimateVatTurnoverInvalid
      }
    }
  }
}
