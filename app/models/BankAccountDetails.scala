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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.JsonUtilities

case class BankAccount(isProvided: Boolean,
                       details: Option[BankAccountDetails],
                       reason: Option[NoUKBankAccount])

case class BankAccountDetails(name: String,
                              number: String,
                              sortCode: String)

object BankAccountDetails {
  implicit val accountReputationWrites: OWrites[BankAccountDetails] = new OWrites[BankAccountDetails] {
    override def writes(o: BankAccountDetails): JsObject = {
      Json.obj("account" -> Json.obj(
        "accountName" -> o.name,
        "accountNumber" -> o.number,
        "sortCode" -> o.sortCode.replace("-", "")
      ))
    }
  }

  val format: OFormat[BankAccountDetails] = Json.format[BankAccountDetails]

  def bankSeq(bankAccount: BankAccountDetails): Seq[String] = {
    Seq(
      bankAccount.name,
      bankAccount.number,
      bankAccount.sortCode
    )
  }
}

object BankAccount extends JsonUtilities {
  implicit val s4lKey: S4LKey[BankAccount]    = S4LKey[BankAccount]("bankAccount")

   val reads: Reads[BankAccount] = (
    (__ \ "isProvided").read[Boolean] and
      (__ \ "details").readNullable[BankAccountDetails](BankAccountDetails.format) and
        (__ \ "reason").readNullable[NoUKBankAccount]
    ) (apply _)

  val writes: Writes[BankAccount] = Writes[BankAccount] { bankAccount =>
    Json.obj(
      "isProvided" -> bankAccount.isProvided,
      "details" -> bankAccount.details.map(details =>
        Json.obj(
          "name" -> details.name,
          "sortCode" -> details.sortCode,
          "number" -> details.number
        )
      ),
      "reason" -> bankAccount.reason
    ) filterNullFields

  }

  implicit val format: Format[BankAccount] = Format[BankAccount](reads, writes)

}
