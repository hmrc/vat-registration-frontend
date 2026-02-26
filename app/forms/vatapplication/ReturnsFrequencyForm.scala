/*
 * Copyright 2026 HM Revenue & Customs
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

package forms.vatapplication

import models.api.vatapplication._
import play.api.data.Forms.single
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}

object ReturnsFrequencyForm {

  private val returnFrequencyEmptyKey = "validation.vat.return.frequency.missing"
  private val RETURN_FREQUENCY = "value"

  val monthlyKey = "monthly"
  val quarterlyKey = "quarterly"
  val annualKey = "annual"

  implicit def formatter: Formatter[ReturnsFrequency] = new Formatter[ReturnsFrequency] {
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], ReturnsFrequency] = {
      data.get(key) match {
        case Some(`monthlyKey`) => Right(Monthly)
        case Some(`quarterlyKey`) => Right(Quarterly)
        case Some(`annualKey`) => Right(Annual)
        case _ => Left(Seq(FormError(key, returnFrequencyEmptyKey, Nil)))
      }
    }

    def unbind(key: String, value: ReturnsFrequency) =
      Map(key -> {
        value match {
          case Monthly => monthlyKey
          case Quarterly => quarterlyKey
          case Annual => annualKey
        }
      })
  }

  val form: Form[ReturnsFrequency] = Form(
    single(RETURN_FREQUENCY -> Forms.of[ReturnsFrequency])
  )
}