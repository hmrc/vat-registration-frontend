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

class TwentyRatedSuppliesFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val turnoverEstimate = 15000
  val twentyRatedSuppliesForm: Form[BigDecimal] = TwentyRatedSuppliesForm.form
  val testValidTwentyRatedSupplies: String = "15000"
  val validTwentyRatedSupplies: BigDecimal = 15000
  val testNonNumberTwentyRatedSupplies: String = "test"
  val testTwentyRatedSuppliesWithComma: String = "15,000"
  val testTwentyRatedSuppliesWithDecimals: String = "14999.999"
  val testInvalidTwentyRatedSupplies: String = "16000"
  val testAlphabetsTwentyRatedSupplies: String = "AABBCC99aaa"
  val testNegativeTwentyRatedSupplies: String = "-1"

  val invalid_twenty_rated_supplies_error_key: String = "validation.twentyRatedSupplies.invalid"
  val missing_twenty_rated_supplies_error_key: String = "validation.twentyRatedSupplies.missing"
  val penceNotAllowed_twenty_rated_supplies_error_key: String = "validation.twentyRatedSupplies.penceNotAllowed"

  "The twentyRatedSuppliesForm" must {
    "validate that twentyRatedSupplies is valid" in {
      val form = twentyRatedSuppliesForm.bind(Map(TwentyRatedSuppliesForm.twentyRatedSuppliesKey -> testValidTwentyRatedSupplies)).value

      form mustBe Some(validTwentyRatedSupplies)
    }

    "validate that missing twentyRatedSupplies fails" in {
      val form = twentyRatedSuppliesForm.bind(Map(TwentyRatedSuppliesForm.twentyRatedSuppliesKey -> ""))

      form.errors must contain(FormError(TwentyRatedSuppliesForm.twentyRatedSuppliesKey, missing_twenty_rated_supplies_error_key))
    }

    "validate that alphabets in twentyRatedSupplies fails" in {
      val form = twentyRatedSuppliesForm.bind(Map(TwentyRatedSuppliesForm.twentyRatedSuppliesKey -> testAlphabetsTwentyRatedSupplies))

      form.errors.size mustBe 1
      form.errors.head.key mustBe TwentyRatedSuppliesForm.twentyRatedSuppliesKey
      form.errors.head.message mustBe invalid_twenty_rated_supplies_error_key
    }

    "validate that non numeric twentyRatedSupplies fails" in {
      val form = twentyRatedSuppliesForm.bind(Map(TwentyRatedSuppliesForm.twentyRatedSuppliesKey -> testNonNumberTwentyRatedSupplies))

      form.errors.size mustBe 1
      form.errors.head.key mustBe TwentyRatedSuppliesForm.twentyRatedSuppliesKey
      form.errors.head.message mustBe invalid_twenty_rated_supplies_error_key
    }

    "validate twentyRatedSupplies with comma fails" in {
      val form = twentyRatedSuppliesForm.bind(Map(TwentyRatedSuppliesForm.twentyRatedSuppliesKey -> testTwentyRatedSuppliesWithComma))

      form.errors.size mustBe 1
      form.errors.head.key mustBe TwentyRatedSuppliesForm.twentyRatedSuppliesKey
      form.errors.head.message mustBe invalid_twenty_rated_supplies_error_key
    }

    "validate twentyRatedSupplies with decimals fails" in {
      val form = twentyRatedSuppliesForm.bind(Map(TwentyRatedSuppliesForm.twentyRatedSuppliesKey -> testTwentyRatedSuppliesWithDecimals))

      form.errors.size mustBe 1
      form.errors.head.key mustBe TwentyRatedSuppliesForm.twentyRatedSuppliesKey
      form.errors.head.message mustBe penceNotAllowed_twenty_rated_supplies_error_key
    }

    "validate that when twentyRatedSupplies is negative the form fails" in {
      val form = twentyRatedSuppliesForm.bind(Map(TwentyRatedSuppliesForm.twentyRatedSuppliesKey -> testNegativeTwentyRatedSupplies))

      form.errors.size mustBe 1
      form.errors.head.key mustBe TwentyRatedSuppliesForm.twentyRatedSuppliesKey
      form.errors.head.message mustBe invalid_twenty_rated_supplies_error_key
    }
  }
}
