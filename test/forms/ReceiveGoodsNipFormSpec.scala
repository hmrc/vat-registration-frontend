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

class ReceiveGoodsNipFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val receiveGoodsNipForm: Form[(Boolean, Option[BigDecimal])] = ReceiveGoodsNipForm.form
  val testValidReceiveGoodsNip: String = "14999.99"
  val validReceiveGoodsNip: BigDecimal = 14999.99
  val validYesNo: String = "true"
  val testNonNumberReceiveGoodsNip: String = "test"
  val testReceiveGoodsNipWithComma: String = "14,999.99"
  val testReceiveGoodsNipWithMoreDecimals: String = "14999.999"
  val testInvalidReceiveGoodsNip: String = (999999999999999L + 1).toString
  val testNegativeReceiveGoodsNip: String = "-1"

  val invalid_receive_goods_nip_nip_error_key: String = "validation.northernIrelandReceiveGoods.invalid"
  val missing_receive_goods_nip_nip_error_key: String = "validation.northernIrelandReceiveGoods.missing"
  val missing_yes_no_error_key: String = "nip.receiveGoods.missing"
  val commasNotAllowed_receive_goods_nip_error_key: String = "validation.northernIrelandReceiveGoods.commasNotAllowed"
  val moreThanTwoDecimalsNotAllowed_receive_goods_nip_error_key = "validation.northernIrelandReceiveGoods.moreThanTwoDecimalsNotAllowed"
  val too_big_receive_goods_nip_nip_error_key: String = "validation.northernIrelandReceiveGoods.range.above"
  val negative_receive_goods_nip_nip_error_key: String = "validation.northernIrelandReceiveGoods.range.below"

  "The receiveGoodsNipForm" must {
    "validate that receiveGoodsNip is valid" in {
      val form = receiveGoodsNipForm.bind(Map(ReceiveGoodsNipForm.yesNo -> validYesNo, ReceiveGoodsNipForm.inputAmount -> testValidReceiveGoodsNip)).value

      form mustBe Some((true, Some(validReceiveGoodsNip)))
    }

    "validate that missing yesNo answer fails" in {
      val form = receiveGoodsNipForm.bind(Map(ReceiveGoodsNipForm.yesNo -> "", ReceiveGoodsNipForm.inputAmount -> ""))

      form.errors must contain(FormError(ReceiveGoodsNipForm.yesNo, missing_yes_no_error_key))
    }

    "validate that missing receiveGoodsNip fails" in {
      val form = receiveGoodsNipForm.bind(Map(ReceiveGoodsNipForm.yesNo -> validYesNo, ReceiveGoodsNipForm.inputAmount -> ""))

      form.errors must contain(FormError(ReceiveGoodsNipForm.inputAmount, missing_receive_goods_nip_nip_error_key))
    }

    "validate that non numeric receiveGoodsNip fails" in {
      val form = receiveGoodsNipForm.bind(Map(ReceiveGoodsNipForm.yesNo -> validYesNo, ReceiveGoodsNipForm.inputAmount -> testNonNumberReceiveGoodsNip))

      form.errors.size mustBe 1
      form.errors.head.key mustBe ReceiveGoodsNipForm.inputAmount
      form.errors.head.message mustBe invalid_receive_goods_nip_nip_error_key
    }

    "validate that receiveGoodsNip with comma fails" in {
      val form = receiveGoodsNipForm.bind(Map(ReceiveGoodsNipForm.yesNo -> validYesNo, ReceiveGoodsNipForm.inputAmount -> testReceiveGoodsNipWithComma))

      form.errors.size mustBe 1
      form.errors.head.key mustBe ReceiveGoodsNipForm.inputAmount
      form.errors.head.message mustBe commasNotAllowed_receive_goods_nip_error_key
    }

    "validate that receiveGoodsNip with more than two decimals fails" in {
      val form = receiveGoodsNipForm.bind(Map(ReceiveGoodsNipForm.yesNo -> validYesNo, ReceiveGoodsNipForm.inputAmount -> testReceiveGoodsNipWithMoreDecimals))

      form.errors.size mustBe 1
      form.errors.head.key mustBe ReceiveGoodsNipForm.inputAmount
      form.errors.head.message mustBe moreThanTwoDecimalsNotAllowed_receive_goods_nip_error_key
    }

    "validate that when receiveGoodsNip is negative the form fails" in {
      val form = receiveGoodsNipForm.bind(Map(ReceiveGoodsNipForm.yesNo -> validYesNo, ReceiveGoodsNipForm.inputAmount -> testNegativeReceiveGoodsNip))

      form.errors.size mustBe 1
      form.errors.head.key mustBe ReceiveGoodsNipForm.inputAmount
      form.errors.head.message mustBe invalid_receive_goods_nip_nip_error_key
    }
  }
}
