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

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}

class TurnoverEstimateFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val turnoverEstimateForm: Form[BigDecimal] = TurnoverEstimateForm.form
  val testValidTurnoverEstimate: String = "14,999.99"
  val validTurnoverEstimate: BigDecimal = 14999.99
  val testNonNumberTurnoverEstimate: String = "test"
  val testInvalidTurnoverEstimate: String = (999999999999999L + 1).toString
  val testNegativeTurnoverEstimate: String = "-1"

  val numeric_error_key: String = "validation.numeric"
  val missing_zero_rated_supplies_error_key: String = "validation.turnoverEstimate.missing"
  val too_big_zero_rated_supplies_error_key: String = "validation.turnoverEstimate.range.above"
  val negative_zero_rated_supplies_error_key: String = "validation.turnoverEstimate.range.below"

  "The turnoverEstimateForm" must {
    "validate that turnoverEstimate is valid" in {
      val form = turnoverEstimateForm.bind(Map(TurnoverEstimateForm.turnoverEstimateKey -> testValidTurnoverEstimate)).value

      form mustBe Some(validTurnoverEstimate)
    }

    "validate that missing turnoverEstimate fails" in {
      val form = turnoverEstimateForm.bind(Map(TurnoverEstimateForm.turnoverEstimateKey -> ""))

      form.errors must contain(FormError(TurnoverEstimateForm.turnoverEstimateKey, missing_zero_rated_supplies_error_key))
    }

    "validate that non numeric turnoverEstimate fails" in {
      val form = turnoverEstimateForm.bind(Map(TurnoverEstimateForm.turnoverEstimateKey -> testNonNumberTurnoverEstimate))

      form.errors must contain(FormError(TurnoverEstimateForm.turnoverEstimateKey, numeric_error_key))
    }

    "validate that when turnoverEstimate > 999999999999999 the form fails" in {
      val form = turnoverEstimateForm.bind(Map(TurnoverEstimateForm.turnoverEstimateKey -> testInvalidTurnoverEstimate))

      form.errors must contain(FormError(TurnoverEstimateForm.turnoverEstimateKey, too_big_zero_rated_supplies_error_key, List(999999999999999L)))
    }

    "validate that when turnoverEstimate is negative the form fails" in {
      val form = turnoverEstimateForm.bind(Map(TurnoverEstimateForm.turnoverEstimateKey -> testNegativeTurnoverEstimate))

      form.errors must contain(FormError(TurnoverEstimateForm.turnoverEstimateKey, negative_zero_rated_supplies_error_key, List(0)))
    }
  }
}