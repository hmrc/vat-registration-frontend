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

package models

import play.api.libs.json._

sealed trait NoUKBankAccount

case object BeingSetupOrNameChange extends NoUKBankAccount
case object OverseasAccount extends NoUKBankAccount
case object NameChange extends NoUKBankAccount
case object AccountNotInBusinessName extends NoUKBankAccount
case object DontWantToProvide extends NoUKBankAccount

object NoUKBankAccount {

  val beingSetup: String = "BeingSetup"
  val overseasAccount: String = "OverseasAccount"
  val nameChange: String = "NameChange"
  val accountNotInBusinessName: String = "AccountNotInBusinessName"
  val dontWantToProvide: String = "DontWantToProvide"

  implicit val reads: Reads[NoUKBankAccount] = Reads[NoUKBankAccount] {
    case JsString(`beingSetup`) => JsSuccess(BeingSetupOrNameChange)
    case JsString(`overseasAccount`) => JsSuccess(OverseasAccount)
    case JsString(`nameChange`) => JsSuccess(NameChange)
    case JsString(`accountNotInBusinessName`) => JsSuccess(AccountNotInBusinessName)
    case JsString(`dontWantToProvide`) => JsSuccess(DontWantToProvide)
    case _ => JsError("Could not parse reason for no UK bank account")
  }

  implicit val writes: Writes[NoUKBankAccount] = Writes[NoUKBankAccount] {
    case BeingSetupOrNameChange => JsString(beingSetup)
    case OverseasAccount => JsString(overseasAccount)
    case NameChange => JsString(nameChange)
    case AccountNotInBusinessName => JsString(accountNotInBusinessName)
    case DontWantToProvide => JsString(dontWantToProvide)
  }

  implicit val format: Format[NoUKBankAccount] = Format(reads, writes)

}
