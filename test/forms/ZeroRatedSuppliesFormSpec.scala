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

import models.TurnoverEstimates
import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}

class ZeroRatedSuppliesFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val turnoverEstimates: TurnoverEstimates = TurnoverEstimates(15000)
  val zeroRatedSuppliesForm: Form[BigDecimal] = ZeroRatedSuppliesForm.form(turnoverEstimates)
  val testValidZeroRatedSupplies: String = "14,999.99"
  val validZeroRatedSupplies: BigDecimal = 14999.99
  val testNonNumberZeroRatedSupplies: String = "test"
  val testInvalidZeroRatedSupplies: String = "16000"
  val testNegativeZeroRatedSupplies: String = "-1"

  val numeric_error_key: String = "validation.numeric"
  val missing_zero_rated_supplies_error_key: String = "validation.zeroRatedSupplies.missing"
  val too_big_zero_rated_supplies_error_key: String = "validation.zeroRatedSupplies.range.above"
  val negative_zero_rated_supplies_error_key: String = "validation.zeroRatedSupplies.range.below"

  "The zeroRatedSuppliesForm" should {
    "validate that zeroRatedSupplies is valid" in {
      val form = zeroRatedSuppliesForm.bind(Map(ZeroRatedSuppliesForm.zeroRatedSuppliesKey -> testValidZeroRatedSupplies)).value

      form shouldBe Some(validZeroRatedSupplies)
    }

    "validate that missing zeroRatedSupplies fails" in {
      val form = zeroRatedSuppliesForm.bind(Map(ZeroRatedSuppliesForm.zeroRatedSuppliesKey -> ""))

      form.errors should contain(FormError(ZeroRatedSuppliesForm.zeroRatedSuppliesKey, missing_zero_rated_supplies_error_key))
    }

    "validate that non numeric zeroRatedSupplies fails" in {
      val form = zeroRatedSuppliesForm.bind(Map(ZeroRatedSuppliesForm.zeroRatedSuppliesKey -> testNonNumberZeroRatedSupplies))

      form.errors should contain(FormError(ZeroRatedSuppliesForm.zeroRatedSuppliesKey, numeric_error_key))
    }

    "validate that when zeroRatedSupplies > turnoverEstimates the form fails" in {
      val form = zeroRatedSuppliesForm.bind(Map(ZeroRatedSuppliesForm.zeroRatedSuppliesKey -> testInvalidZeroRatedSupplies))

      form.errors should contain(FormError(ZeroRatedSuppliesForm.zeroRatedSuppliesKey, too_big_zero_rated_supplies_error_key))
    }

    "validate that when zeroRatedSupplies is negative the form fails" in {
      val form = zeroRatedSuppliesForm.bind(Map(ZeroRatedSuppliesForm.zeroRatedSuppliesKey -> testNegativeZeroRatedSupplies))

      form.errors should contain(FormError(ZeroRatedSuppliesForm.zeroRatedSuppliesKey, negative_zero_rated_supplies_error_key))
    }
  }
}
