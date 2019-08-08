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
import play.api.libs.json.Reads._
import play.api.libs.json._

case class CoHoCompanyProfile(status: String, transactionId: String)

object CoHoCompanyProfile {
  implicit val reader: Reads[CoHoCompanyProfile] = (
    (__ \ "status").read[String] and
    (__ \ "confirmationReferences" \ "transaction-id").read[String]
    )(CoHoCompanyProfile.apply _)

  implicit val writer: Writes[CoHoCompanyProfile] = (
    (__ \ "status").write[String] and
    (__ \ "confirmationReferences" \ "transaction-id").write[String]
    )(unlift(CoHoCompanyProfile.unapply))

}
