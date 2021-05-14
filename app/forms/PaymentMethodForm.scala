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

import models.api.returns.{PaymentMethod, BACS, CHAPS, BankGIRO, StandingOrder}
import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

object PaymentMethodForm {
  val paymentMethod: String = "value"
  val bacs = "bacs"
  val giro = "giro"
  val chaps = "chaps"
  val standingOrder = "standing-order"
  val paymentMethodNotProvidedKey = "aas.paymentMethod.error.required"

  def apply(): Form[PaymentMethod] = Form(
    single(
      paymentMethod -> of(formatter)
    )
  )

  def formatter: Formatter[PaymentMethod] = new Formatter[PaymentMethod] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], PaymentMethod] = {
      data.get(key) match {
        case Some(`bacs`) => Right(BACS)
        case Some(`giro`) => Right(BankGIRO)
        case Some(`chaps`) => Right(CHAPS)
        case Some(`standingOrder`) => Right(StandingOrder)
        case _ => Left(Seq(FormError(key, paymentMethodNotProvidedKey)))
      }
    }

    override def unbind(key: String, value: PaymentMethod): Map[String, String] = {
      Map(
        key -> {
          value match {
            case BACS => bacs
            case BankGIRO => giro
            case CHAPS => chaps
            case StandingOrder => standingOrder
          }
        }
      )
    }
  }

}