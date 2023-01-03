/*
 * Copyright 2023 HM Revenue & Customs
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

class SellOrMoveNipFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val sellOrMoveNipForm: Form[(Boolean, Option[BigDecimal])] = SellOrMoveNipForm.form
  val testValidSellOrMoveNip: String = "14999.99"
  val validSellOrMoveNip: BigDecimal = 14999.99
  val validYesNo: String = "true"
  val testNonNumberSellOrMoveNip: String = "test"
  val testSellOrMoveNipWithComma: String = "14,999.99"
  val testSellOrMoveNipWithMoreDecimals: String = "14999.999"
  val testInvalidSellOrMoveNip: String = (999999999999999L + 1).toString
  val testNegativeSellOrMoveNip: String = "-1"

  val invalid_sell_or_move_nip_error_key: String = "validation.sellOrMoveNip.invalid"
  val missing_sell_or_move_nip_error_key: String = "validation.sellOrMoveNip.missing"
  val missing_yes_no_error_key: String = "nip.error.missing"
  val commasNotAllowed_sell_or_move_nip_error_key: String = "validation.sellOrMoveNip.commasNotAllowed"
  val moreThanTwoDecimalsNotAllowed_sell_or_move_nip_error_key = "validation.sellOrMoveNip.moreThanTwoDecimalsNotAllowed"
  val too_big_sell_or_move_nip_error_key: String = "validation.sellOrMoveNip.range.above"
  val negative_sell_or_move_nip_error_key: String = "validation.sellOrMoveNip.range.below"

  "The sellOrMoveNipForm" must {
    "validate that sellOrMoveNip is valid" in {
      val form = sellOrMoveNipForm.bind(Map(SellOrMoveNipForm.yesNo -> validYesNo, SellOrMoveNipForm.inputAmount -> testValidSellOrMoveNip)).value

      form mustBe Some((true, Some(validSellOrMoveNip)))
    }

    "validate that missing yesNo answer fails" in {
      val form = sellOrMoveNipForm.bind(Map(SellOrMoveNipForm.yesNo -> "", SellOrMoveNipForm.inputAmount -> ""))

      form.errors must contain(FormError(SellOrMoveNipForm.yesNo, missing_yes_no_error_key))
    }

    "validate that missing sellOrMoveNip fails" in {
      val form = sellOrMoveNipForm.bind(Map(SellOrMoveNipForm.yesNo -> validYesNo, SellOrMoveNipForm.inputAmount -> ""))

      form.errors must contain(FormError(SellOrMoveNipForm.inputAmount, missing_sell_or_move_nip_error_key))
    }

    "validate that non numeric sellOrMoveNip fails" in {
      val form = sellOrMoveNipForm.bind(Map(SellOrMoveNipForm.yesNo -> validYesNo, SellOrMoveNipForm.inputAmount -> testNonNumberSellOrMoveNip))

      form.errors.size mustBe 1
      form.errors.head.key mustBe SellOrMoveNipForm.inputAmount
      form.errors.head.message mustBe invalid_sell_or_move_nip_error_key
    }

    "validate that sellOrMoveNip with comma fails" in {
      val form = sellOrMoveNipForm.bind(Map(SellOrMoveNipForm.yesNo -> validYesNo, SellOrMoveNipForm.inputAmount -> testSellOrMoveNipWithComma))

      form.errors.size mustBe 1
      form.errors.head.key mustBe SellOrMoveNipForm.inputAmount
      form.errors.head.message mustBe commasNotAllowed_sell_or_move_nip_error_key
    }

    "validate that sellOrMoveNip with more than two decimals fails" in {
      val form = sellOrMoveNipForm.bind(Map(SellOrMoveNipForm.yesNo -> validYesNo, SellOrMoveNipForm.inputAmount -> testSellOrMoveNipWithMoreDecimals))

      form.errors.size mustBe 1
      form.errors.head.key mustBe SellOrMoveNipForm.inputAmount
      form.errors.head.message mustBe moreThanTwoDecimalsNotAllowed_sell_or_move_nip_error_key
    }

    "validate that when sellOrMoveNip is negative the form fails" in {
      val form = sellOrMoveNipForm.bind(Map(SellOrMoveNipForm.yesNo -> validYesNo, SellOrMoveNipForm.inputAmount -> testNegativeSellOrMoveNip))

      form.errors.size mustBe 1
      form.errors.head.key mustBe SellOrMoveNipForm.inputAmount
      form.errors.head.message mustBe invalid_sell_or_move_nip_error_key
    }
  }
}
