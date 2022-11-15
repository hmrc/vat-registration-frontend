/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json._

sealed trait BusinessRegistrationStatus

case object RegisteredStatus extends BusinessRegistrationStatus

case object FailedStatus extends BusinessRegistrationStatus

case object NotCalledStatus extends BusinessRegistrationStatus

object BusinessRegistrationStatus {
  implicit val format: Format[BusinessRegistrationStatus] = new Format[BusinessRegistrationStatus] {
    val RegisteredKey = "REGISTERED"
    val FailedKey = "REGISTRATION_FAILED"
    val NotCalledKey = "REGISTRATION_NOT_CALLED"

    override def writes(status: BusinessRegistrationStatus): JsValue =
      status match {
        case RegisteredStatus => JsString(RegisteredKey)
        case FailedStatus => JsString(FailedKey)
        case NotCalledStatus => JsString(NotCalledKey)
      }

    override def reads(json: JsValue): JsResult[BusinessRegistrationStatus] =
      json.validate[String].map {
        case RegisteredKey => RegisteredStatus
        case FailedKey => FailedStatus
        case NotCalledKey => NotCalledStatus
      }
  }
}
