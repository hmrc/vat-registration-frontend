/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{BACS, CHAPS, Giro, StandingOrder}
import play.api.data.FormError
import play.api.libs.json.Json
import testHelpers.VatRegSpec

class AASPaymentMethodFormSpec extends VatRegSpec {

  val form = AASPaymentMethodForm()

  "the AASPaymentMethod form" when {
    "binding a value" should {
      "return the correct payment method for all valid values" in {
        val validValues = Map(
          "bacs" -> BACS,
          "giro" -> Giro,
          "chaps" -> CHAPS,
          "standing-order" -> StandingOrder
        )

        validValues map {
          case (value, expected) =>
            val res = form.bind(Json.obj(AASPaymentMethodForm.paymentMethod -> value))
            res.get mustBe expected
        }
      }
      "return an error for an invalid value" in {
        val invalidValues = Seq(
          "",
          "credit-card"
        )

        invalidValues map { value =>
          val res = form.bind(Json.obj(AASPaymentMethodForm.paymentMethod -> value))
          res.errors.head mustBe FormError(AASPaymentMethodForm.paymentMethod, Seq("aas.paymentMethod.error.required"), Seq())
        }
      }
    }

    "populating with a valid payment method" should {
      "return the correct thing for every valid value" in {
        val validValues = Seq(
          BACS,
          Giro,
          CHAPS,
          StandingOrder
        )

        validValues map { value =>
          val res = form.fill(value)
          res.get mustBe value
        }
      }
    }
  }

}