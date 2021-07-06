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

package models.external

import play.api.libs.json._
import uk.gov.hmrc.http.InternalServerException


sealed trait RegistrationStatus

case class Registered(registeredBusinessPartnerId: String) extends RegistrationStatus

case object RegistrationFailed extends RegistrationStatus

case object RegistrationNotCalled extends RegistrationStatus

object RegistrationStatus {
  val registrationStatusKey = "registrationStatus"
  val registeredBusinessPartnerIdKey = "registeredBusinessPartnerId"
  val RegisteredKey = "REGISTERED"
  val RegistrationFailedKey = "REGISTRATION_FAILED"
  val RegistrationNotCalledKey = "REGISTRATION_NOT_CALLED"

  implicit val format: Format[RegistrationStatus] = new Format[RegistrationStatus] {
    override def writes(registrationStatus: RegistrationStatus): JsValue =
      registrationStatus match {
        case Registered(businessPartnerId) => Json.obj(
          registrationStatusKey -> RegisteredKey,
          registeredBusinessPartnerIdKey -> businessPartnerId
        )
        case RegistrationFailed =>
          Json.obj(registrationStatusKey -> RegistrationFailedKey)
        case RegistrationNotCalled =>
          Json.obj(registrationStatusKey -> RegistrationNotCalledKey)
        case _ =>
          throw new InternalServerException("Invalid registration status")
      }

    override def reads(json: JsValue): JsResult[RegistrationStatus] =
      (json \ registrationStatusKey).validate[String] match {
        case JsSuccess(RegisteredKey, _) =>
          (json \ registeredBusinessPartnerIdKey).validate[String].map {
            businessPartnerId => Registered(businessPartnerId)
          }
        case JsSuccess(RegistrationFailedKey, path) =>
          JsSuccess(RegistrationFailed, path)
        case JsSuccess(RegistrationNotCalledKey, path) =>
          JsSuccess(RegistrationNotCalled, path)
        case _ =>
          throw new InternalServerException("Invalid registration status")
      }
  }
}

