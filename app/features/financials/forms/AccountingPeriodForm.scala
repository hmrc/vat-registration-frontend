/*
 * Copyright 2018 HM Revenue & Customs
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

import features.financials.models.Stagger
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}

object AccountingPeriodForm {

  val accountingPeriodInvalidKey = "validation.accounting.period.missing"
  val ACCOUNTING_PERIOD: String = "accountingPeriodRadio"

  implicit def formatter: Formatter[Stagger.Value] = new Formatter[Stagger.Value] {

    override val format = Some(("format.string", Nil))

    // default play binding is to data.getOrElse(key, "false")
    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key,"")).right.flatMap {
        case e if e == Stagger.jan.toString => Right(Stagger.jan)
        case e if e == Stagger.feb.toString => Right(Stagger.feb)
        case e if e == Stagger.mar.toString => Right(Stagger.mar)
        case _ => Left(Seq(FormError(key, accountingPeriodInvalidKey, Nil)))
      }
    }

    def unbind(key: String, value: Stagger.Value) = Map(key -> value.toString)
  }

  val form = Form(
    single(ACCOUNTING_PERIOD -> Forms.of[Stagger.Value])
  )
}
