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

class StandardRateSuppliesFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val turnoverEstimate = 15000
  val standardRateSuppliesForm: Form[BigDecimal] = StandardRateSuppliesForm.form
  val testValidStandardRateSupplies: String = "15000"
  val validStandardRateSupplies: BigDecimal = 15000
  val testNonNumberStandardRateSupplies: String = "test"
  val testStanrdardRateSuppliesWithComma: String = "15,000"
  val testStandardRateSuppliesWithDecimals: String = "14999.999"
  val testInvalidStandardRateSupplies: String = "16000"
  val testAlphabetsStandardRateSupplies: String = "AABBCC99aaa"
  val testNegativeStandardRateSupplies: String = "-1"

  val invalid_standard_rate_supplies_error_key: String = "validation.standardRateSupplies.invalid"
  val missing_standard_rate_supplies_error_key: String = "validation.standardRateSupplies.missing"
  val penceNotAllowed_standard_rate_supplies_error_key: String = "validation.standardRateSupplies.penceNotAllowed"

  "The standardRateSuppliesForm" must {
    "validate that standardRateSupplies is valid" in {
      val form = standardRateSuppliesForm.bind(Map(StandardRateSuppliesForm.standardRateSuppliesKey -> testValidStandardRateSupplies)).value

      form mustBe Some(validStandardRateSupplies)
    }

    "validate that missing standardRateSupplies fails" in {
      val form = standardRateSuppliesForm.bind(Map(StandardRateSuppliesForm.standardRateSuppliesKey -> ""))

      form.errors must contain(FormError(StandardRateSuppliesForm.standardRateSuppliesKey, missing_standard_rate_supplies_error_key))
    }

    "validate that alphabets in standardRateSupplies fails" in {
      val form = standardRateSuppliesForm.bind(Map(StandardRateSuppliesForm.standardRateSuppliesKey -> testAlphabetsStandardRateSupplies))

      form.errors.size mustBe 1
      form.errors.head.key mustBe StandardRateSuppliesForm.standardRateSuppliesKey
      form.errors.head.message mustBe invalid_standard_rate_supplies_error_key
    }

    "validate that non numeric standardRateSupplies fails" in {
      val form = standardRateSuppliesForm.bind(Map(StandardRateSuppliesForm.standardRateSuppliesKey -> testNonNumberStandardRateSupplies))

      form.errors.size mustBe 1
      form.errors.head.key mustBe StandardRateSuppliesForm.standardRateSuppliesKey
      form.errors.head.message mustBe invalid_standard_rate_supplies_error_key
    }

    "validate standardRateSupplies with comma fails" in {
      val form = standardRateSuppliesForm.bind(Map(StandardRateSuppliesForm.standardRateSuppliesKey -> testStanrdardRateSuppliesWithComma))

      form.errors.size mustBe 1
      form.errors.head.key mustBe StandardRateSuppliesForm.standardRateSuppliesKey
      form.errors.head.message mustBe invalid_standard_rate_supplies_error_key
    }

    "validate standardRateSupplies with decimals fails" in {
      val form = standardRateSuppliesForm.bind(Map(StandardRateSuppliesForm.standardRateSuppliesKey -> testStandardRateSuppliesWithDecimals))

      form.errors.size mustBe 1
      form.errors.head.key mustBe StandardRateSuppliesForm.standardRateSuppliesKey
      form.errors.head.message mustBe penceNotAllowed_standard_rate_supplies_error_key
    }

    "validate that when standardRateSupplies is negative the form fails" in {
      val form = standardRateSuppliesForm.bind(Map(StandardRateSuppliesForm.standardRateSuppliesKey -> testNegativeStandardRateSupplies))

      form.errors.size mustBe 1
      form.errors.head.key mustBe StandardRateSuppliesForm.standardRateSuppliesKey
      form.errors.head.message mustBe invalid_standard_rate_supplies_error_key
    }
  }
}
