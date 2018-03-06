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

package features.bankAccountDetails.models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class BankAccount(isProvided: Boolean,
                       details: Option[BankAccountDetails])

case class BankAccountDetails(name: String,
                              sortCode: String,
                              number: String)

object BankAccountDetails {
  implicit val accountReputationWrites: OWrites[BankAccountDetails] = new OWrites[BankAccountDetails]{
    override def writes(o: BankAccountDetails): JsObject = {
      Json.obj("account" -> Json.obj(
        "accountName" -> o.name,
        "accountNumber" -> o.number,
        "sortCode" -> o.sortCode.replace("-", "")
      ))
    }
  }
}

object BankAccount {
  implicit val format: OFormat[BankAccount] = (
    (__ \ "isProvided").format[Boolean] and
    (__ \ "details").formatNullable((
      (__ \ "name").format[String] and
      (__ \ "sortCode").format[String] and
      (__ \ "number").format[String]
    )(BankAccountDetails.apply, unlift(BankAccountDetails.unapply)))
  )(apply, unlift(unapply))
}
