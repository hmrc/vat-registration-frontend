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

package models

import java.time.LocalDate

import common.enums.VatRegStatus
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class CurrentProfile(companyName: String,
                          registrationId: String,
                          transactionId: String,
                          vatRegistrationStatus: VatRegStatus.Value,
                          incorporationDate: Option[LocalDate],
                          ivPassed: Option[Boolean] = None,
                          incorpRejected: Option[Boolean] = None)

object CurrentProfile {
  val reads: Reads[CurrentProfile] = (
    (__ \ "companyName").read[String] and
    (__ \ "registrationID").read[String] and
    (__ \ "transactionID").read[String] and
    (__ \ "vatRegistrationStatus").read[VatRegStatus.Value] and
    (__ \ "incorporationDate").readNullable[LocalDate] and
    (__ \ "ivPassed").readNullable[Boolean] and
    (__ \ "incorpRejected").readNullable[Boolean]
  )(CurrentProfile.apply _)

  val writes: Writes[CurrentProfile] = (
    (__ \ "companyName").write[String] and
    (__ \ "registrationID").write[String] and
    (__ \ "transactionID").write[String] and
    (__ \ "vatRegistrationStatus").write[VatRegStatus.Value] and
    (__ \ "incorporationDate").writeNullable[LocalDate] and
    (__ \ "ivPassed").writeNullable[Boolean] and
    (__ \ "incorpRejected").writeNullable[Boolean]
  )(unlift(CurrentProfile.unapply))

  implicit val format: Format[CurrentProfile] = Format(reads, writes)
}
