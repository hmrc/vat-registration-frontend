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

package models.api.trafficmanagement

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, __}

import java.time.LocalDate

case class RegistrationInformation(internalId: String,
                                   registrationId: String,
                                   status: RegistrationStatus,
                                   regStartDate: Option[LocalDate] = None,
                                   channel: RegistrationChannel)

object RegistrationInformation {
  implicit val format: Format[RegistrationInformation] =
    ((__ \ "internalId").format[String] and
      (__ \ "registrationId").format[String] and
      (__ \ "status").format[RegistrationStatus] and
      (__ \ "regStartDate").formatNullable[LocalDate] and
      (__ \ "channel").format[RegistrationChannel]
      ) (RegistrationInformation.apply, unlift(RegistrationInformation.unapply))
}

