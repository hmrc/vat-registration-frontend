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

class ReducedRateSuppliesFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val reducedRateSuppliesForm: Form[BigDecimal] = ReducedRateSuppliesForm.form
  val testValidReduceRatedSupplies: String = "14999"
  val validReducedRateSupplies: BigDecimal = 14999
  val testNonNumberReducedRateSupplies: String = "test"
  val testReducedRateSuppliesWithComma: String = "14,999"
  val testReducedRateSuppliesWithDecimals: String = "14999.999"
  val testInvalidReducedRateSupplies: String = (999999999999999L + 1).toString
  val testAlphabetsReducedRateSupplies: String = "ABCD123abc"
  val testNegativeReducedRateSupplies: String = "-1"

  val invalid_reduced_rate_supplies_error_key: String = "validation.reducedRateSupplies.invalid"
  val missing_reduced_rate_supplies_error_key: String = "validation.reducedRateSupplies.missing"
  val penceNotAllowed_reduced_rate_supplies_error_key: String = "validation.reducedRateSupplies.penceNotAllowed"
  val too_big_reduced_rate_supplies_error_key: String = "validation.reducedRateSupplies.range.above"
  val negative_reduced_rate_supplies_error_key: String = "validation.reducedRateSupplies.range.below"

  "The reducedRateSuppliesForm" must {
    "validate that reducedRateSupplies is valid" in {
      val form = reducedRateSuppliesForm.bind(Map(ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey -> testValidReduceRatedSupplies)).value

      form mustBe Some(validReducedRateSupplies)
    }

    "validate that missing reducedRatedSupplies fails" in {
      val form = reducedRateSuppliesForm.bind(Map(ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey -> ""))

      form.errors must contain(FormError(ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey, missing_reduced_rate_supplies_error_key))
    }

    "validate that non numeric reducedRatedSupplies fails" in {
      val form = reducedRateSuppliesForm.bind(Map(ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey -> testNonNumberReducedRateSupplies))

      form.errors.size mustBe 1
      form.errors.head.key mustBe ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey
      form.errors.head.message mustBe invalid_reduced_rate_supplies_error_key
    }

    "validate that alphabets in reducedRatedSupplies fails" in {
      val form = reducedRateSuppliesForm.bind(Map(ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey -> testAlphabetsReducedRateSupplies))

      form.errors.size mustBe 1
      form.errors.head.key mustBe ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey
      form.errors.head.message mustBe invalid_reduced_rate_supplies_error_key
    }

    "validate that reducedRatedSupplies with comma fails" in {
      val form =  reducedRateSuppliesForm.bind(Map(ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey -> testInvalidReducedRateSupplies))

      form.errors.size mustBe 1
      form.errors.head.key mustBe ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey
      form.errors.head.message mustBe too_big_reduced_rate_supplies_error_key
    }

    "validate that reducedRatedSupplies with decimals fails" in {
      val form =  reducedRateSuppliesForm.bind(Map(ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey -> testReducedRateSuppliesWithDecimals))

      form.errors.size mustBe 1
      form.errors.head.key mustBe ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey
      form.errors.head.message mustBe penceNotAllowed_reduced_rate_supplies_error_key
    }

    "validate that when reducedRatedSupplies > 999999999999999 the form fails" in {
      val form =  reducedRateSuppliesForm.bind(Map(ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey -> testInvalidReducedRateSupplies))

      form.errors must contain(FormError(ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey, too_big_reduced_rate_supplies_error_key, List(999999999999999L)))
    }

    "validate that when reducedRatedSupplies is negative the form fails" in {
      val form =  reducedRateSuppliesForm.bind(Map(ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey -> testInvalidReducedRateSupplies))

      form.errors.size mustBe 1
      form.errors.head.key mustBe ReducedRateSuppliesForm.reducedRateSuppliesEstimateKey
      form.errors.head.message mustBe too_big_reduced_rate_supplies_error_key
    }
  }
}
