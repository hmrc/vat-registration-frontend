/*
 * Copyright 2025 HM Revenue & Customs
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

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}

class FiveRatedTurnoverFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val fiveRatedTurnoverForm: Form[BigDecimal] = FiveRatedTurnoverForm.form
  val testValidFiveRatedTurnover: String = "14999"
  val validFiveRatedTurnover: BigDecimal = 14999
  val testNonNumberFiveRatedTurnover: String = "test"
  val testFiveRatedTurnoverWithComma: String = "14,999"
  val testFiveRatedTurnoverWithDecimals: String = "14999.999"
  val testInvalidFiveRatedTurnover: String = (999999999999999L + 1).toString
  val testAlphabetsFiveRatedTurnover: String = "ABCD123abc"
  val testNegativeFiveRatedTurnover: String = "-1"

  val invalid_five_rated_turnover_error_key: String = "validation.fiveRatedTurnover.invalid"
  val missing_five_rated_turnover_error_key: String = "validation.fiveRatedTurnover.missing"
  val penceNotAllowed_five_rated_turnover_error_key: String = "validation.fiveRatedTurnover.penceNotAllowed"
  val too_big_five_rated_turnover_error_key: String = "validation.fiveRatedTurnover.range.above"
  val negative_five_rated_turnover_error_key: String = "validation.fiveRatedTurnover.range.below"

  "The fiveRatedTurnoverForm" must {
    "validate that fiveRatedTurnover is valid" in {
      val form = fiveRatedTurnoverForm.bind(Map(FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey -> testValidFiveRatedTurnover)).value

      form mustBe Some(validFiveRatedTurnover)
    }

    "validate that missing fiveRatedTurnover fails" in {
      val form = fiveRatedTurnoverForm.bind(Map(FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey -> ""))

      form.errors must contain(FormError(FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey, missing_five_rated_turnover_error_key))
    }

    "validate that non numeric fiveRatedTurnover fails" in {
      val form = fiveRatedTurnoverForm.bind(Map(FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey -> testNonNumberFiveRatedTurnover))

      form.errors.size mustBe 1
      form.errors.head.key mustBe FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey
      form.errors.head.message mustBe invalid_five_rated_turnover_error_key
    }

    "validate that alphabets in fiveRatedTurnover fails" in {
      val form = fiveRatedTurnoverForm.bind(Map(FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey -> testAlphabetsFiveRatedTurnover))

      form.errors.size mustBe 1
      form.errors.head.key mustBe FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey
      form.errors.head.message mustBe invalid_five_rated_turnover_error_key
    }

    "validate that fiveRatedTurnover with comma fails" in {
      val form =  fiveRatedTurnoverForm.bind(Map(FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey -> testFiveRatedTurnoverWithComma))

      form.errors.size mustBe 1
      form.errors.head.key mustBe FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey
      form.errors.head.message mustBe invalid_five_rated_turnover_error_key
    }

    "validate that fiveRatedTurnover with decimals fails" in {
      val form =  fiveRatedTurnoverForm.bind(Map(FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey -> testFiveRatedTurnoverWithDecimals))

      form.errors.size mustBe 1
      form.errors.head.key mustBe FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey
      form.errors.head.message mustBe penceNotAllowed_five_rated_turnover_error_key
    }

    "validate that when fiveRatedTurnover > 999999999999999 the form fails" in {
      val form =  fiveRatedTurnoverForm.bind(Map(FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey -> testInvalidFiveRatedTurnover))

      form.errors must contain(FormError(FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey, too_big_five_rated_turnover_error_key, List(999999999999999L)))
    }

    "validate that when fiveRatedTurnover is negative the form fails" in {
      val form =  fiveRatedTurnoverForm.bind(Map(FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey -> testNegativeFiveRatedTurnover))

      form.errors.size mustBe 1
      form.errors.head.key mustBe FiveRatedTurnoverForm.fiveRatedTurnoverEstimateKey
      form.errors.head.message mustBe invalid_five_rated_turnover_error_key
    }
  }
}
