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

import models.api.returns._
import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

object PaymentFrequencyForm {

  val paymentFrequency: String = "value"
  val quarterly = "quarterly"
  val monthly = "monthly"
  val paymentFrequencyNotProvidedKey = "aas.paymentFrequency.notProvided"

  def apply(): Form[PaymentFrequency] = Form(
    single(
      paymentFrequency -> of(formatter)
    )
  )

  def formatter: Formatter[PaymentFrequency] = new Formatter[PaymentFrequency] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], PaymentFrequency] = {
      data.get(key) match {
        case Some(`quarterly`) => Right(QuarterlyPayment)
        case Some(`monthly`) => Right(MonthlyPayment)
        case _ => Left(Seq(FormError(key, paymentFrequencyNotProvidedKey)))
      }
    }

    override def unbind(key: String, value: PaymentFrequency): Map[String, String] = {
      Map(
        key -> {
          value match {
            case QuarterlyPayment => quarterly
            case MonthlyPayment => monthly
          }
        }
      )
    }
  }

}
