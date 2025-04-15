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

class ZeroRatedSuppliesNewJourneyFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val zeroRatedSuppliesForm: Form[BigDecimal] = ZeroRatedSuppliesNewJourneyForm.form()
  val testValidZeroRatedSupplies: String = "14999"
  val validZeroRatedSupplies: BigDecimal = 14999
  val testNonNumberZeroRatedSupplies: String = "test"
  val testZeroRatedSuppliesWithDecimals: String = "14999.999"
  val testInvalidZeroRatedSupplies: String = "9999999999999999"
  val testNegativeZeroRatedSupplies: String = "-1"

  val invalid_zero_rated_supplies_error_key: String = "validation.zeroRatedSupplies.newJourney.invalid"
  val missing_zero_rated_supplies_error_key: String = "validation.zeroRatedSupplies.newJourney.missing"
  val decimalsNotAllowed_zero_rated_supplies_error_key: String = "validation.zeroRatedSupplies.newJourney.decimalsNotAllowed"
  val too_big_zero_rated_supplies_error_key: String = "validation.zeroRatedSupplies.newJourney.range.above"
  val negative_zero_rated_supplies_error_key: String = "validation.zeroRatedSupplies.newJourney.range.below"

  "The zeroRatedSuppliesForm" must {
    "validate that zeroRatedSupplies is valid" in {
      val form = zeroRatedSuppliesForm.bind(Map(ZeroRatedSuppliesNewJourneyForm.zeroRatedSuppliesKey -> testValidZeroRatedSupplies)).value

      form mustBe Some(validZeroRatedSupplies)
    }

    "validate that missing zeroRatedSupplies fails" in {
      val form = zeroRatedSuppliesForm.bind(Map(ZeroRatedSuppliesNewJourneyForm.zeroRatedSuppliesKey -> ""))

      form.errors must contain(FormError(ZeroRatedSuppliesNewJourneyForm.zeroRatedSuppliesKey, missing_zero_rated_supplies_error_key))
    }

    "validate that non numeric zeroRatedSupplies fails" in {
      val form = zeroRatedSuppliesForm.bind(Map(ZeroRatedSuppliesNewJourneyForm.zeroRatedSuppliesKey -> testNonNumberZeroRatedSupplies))

      form.errors.size mustBe 1
      form.errors.head.key mustBe ZeroRatedSuppliesNewJourneyForm.zeroRatedSuppliesKey
      form.errors.head.message mustBe invalid_zero_rated_supplies_error_key
    }

    "validate zeroRatedSupplies with decimals fails" in {
      val form = zeroRatedSuppliesForm.bind(Map(ZeroRatedSuppliesNewJourneyForm.zeroRatedSuppliesKey -> testZeroRatedSuppliesWithDecimals))

      form.errors.size mustBe 1
      form.errors.head.key mustBe ZeroRatedSuppliesNewJourneyForm.zeroRatedSuppliesKey
      form.errors.head.message mustBe decimalsNotAllowed_zero_rated_supplies_error_key
    }

    "validate that when zeroRatedSupplies > 999 trillions the form fails" in {
      val form = zeroRatedSuppliesForm.bind(Map(ZeroRatedSuppliesNewJourneyForm.zeroRatedSuppliesKey -> testInvalidZeroRatedSupplies))

      form.errors must contain(FormError(ZeroRatedSuppliesNewJourneyForm.zeroRatedSuppliesKey, too_big_zero_rated_supplies_error_key, List(BigDecimal("999999999999999"))))
    }

    "validate that when zeroRatedSupplies is negative the form fails" in {
      val form = zeroRatedSuppliesForm.bind(Map(ZeroRatedSuppliesNewJourneyForm.zeroRatedSuppliesKey -> testNegativeZeroRatedSupplies))

      form.errors.size mustBe 1
      form.errors.head.key mustBe ZeroRatedSuppliesNewJourneyForm.zeroRatedSuppliesKey
      form.errors.head.message mustBe invalid_zero_rated_supplies_error_key
    }
  }
}
