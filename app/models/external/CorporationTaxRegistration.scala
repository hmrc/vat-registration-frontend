/*
 * Copyright 2019 HM Revenue & Customs
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

package models.external

import play.api.libs.functional.syntax._
import play.api.libs.json._


case class AccountingDetails(status: String, activeDate: Option[String])

object AccountingDetails {
  //TODO currently this class is not used, so no code is exercising the JSON format

  implicit val format =
    ((__ \ "accountingDateStatus").format[String] and
      (__ \ "startDateOfBusiness").formatNullable[String]
      ) (AccountingDetails.apply, unlift(AccountingDetails.unapply))
}


case class CorporationTaxRegistration(accountingDetails: Option[AccountingDetails] = None)

object CorporationTaxRegistration {

  implicit val format = Json.format[CorporationTaxRegistration]

}
