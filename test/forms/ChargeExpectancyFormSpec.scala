/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.ChargeExpectancyForm._
import testHelpers.VatRegSpec

class ChargeExpectancyFormSpec extends VatRegSpec {

  val form = ChargeExpectancyForm.form

  "Binding ChargeExpectancyForm" should {
    "Bind successfully for a yes selection" in {
      val data = Map(
        EXPECT_CHARGE_MORE_VAT -> "true"
      )
      form.bind(data).get mustBe true
    }

    "Bind successfully for a no selection" in {
      val data = Map(
        EXPECT_CHARGE_MORE_VAT -> "false"
      )
      form.bind(data).get mustBe false
    }

    "Fail to bind successfully for an invalid selection" in {
      val data = Map(
        EXPECT_CHARGE_MORE_VAT -> "invalidSelection"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe EXPECT_CHARGE_MORE_VAT
      bound.errors.head.message mustBe errorMsg
    }

    "Fail to bind successfully if empty" in {
      val data = Map(
        EXPECT_CHARGE_MORE_VAT -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe EXPECT_CHARGE_MORE_VAT
      bound.errors.head.message mustBe errorMsg
    }
  }
}
