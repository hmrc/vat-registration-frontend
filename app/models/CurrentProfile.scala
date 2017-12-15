/*
 * Copyright 2017 HM Revenue & Customs
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
import play.api.libs.json._
import play.api.libs.functional.syntax._
import common.enums.VatRegStatus

case class CurrentProfile(companyName: String,
                          registrationId: String,
                          transactionId: String,
                          vatRegistrationStatus: VatRegStatus.Value,
                          incorporationDate: Option[LocalDate],
                          ivPassed:Boolean = false)

object CurrentProfile {
  implicit val format: Format[CurrentProfile] = (
    (__ \ "companyName").format[String] and
    (__ \ "registrationID").format[String] and
    (__ \ "transactionID").format[String] and
    (__ \ "vatRegistrationStatus").format[VatRegStatus.Value] and
    (__ \ "incorporationDate").formatNullable[LocalDate] and
    (__ \ "ivPassed").format[Boolean]
  )(CurrentProfile.apply, unlift(CurrentProfile.unapply))
}
