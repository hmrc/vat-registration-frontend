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

package models.api.returns

import play.api.libs.json._

sealed trait PaymentMethod
case object StandingOrder extends PaymentMethod
case object BankGIRO extends PaymentMethod
case object BACS extends PaymentMethod
case object CHAPS extends PaymentMethod

object PaymentMethod {

  val standingOrder: String = "01"
  val bankGIRO: String = "02"
  val objectBACS: String = "03"
  val objectCHAPS: String = "04"

  val reads: Reads[PaymentMethod] = Reads[PaymentMethod] {
    case JsString(`standingOrder`) => JsSuccess(StandingOrder)
    case JsString(`bankGIRO`) => JsSuccess(BankGIRO)
    case JsString(`objectBACS`) => JsSuccess(BACS)
    case JsString(`objectCHAPS`) => JsSuccess(CHAPS)
    case _ => JsError("Could not parse payment method")
  }

  val writes: Writes[PaymentMethod] = Writes {
    case StandingOrder => JsString(standingOrder)
    case BankGIRO => JsString(bankGIRO)
    case BACS => JsString(objectBACS)
    case CHAPS => JsString(objectCHAPS)
  }

  implicit val format: Format[PaymentMethod] = Format(reads, writes)

}
