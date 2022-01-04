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

package models.api.trafficmanagement

import play.api.libs.json.{JsString, Reads, Writes}

sealed trait RegistrationStatus

case object Draft extends RegistrationStatus

case object Submitted extends RegistrationStatus

object RegistrationStatus {

  val stati = Map(
    Draft.toString.toLowerCase -> Draft,
    Submitted.toString.toLowerCase -> Submitted
  )

  def fromString(value: String): RegistrationStatus = stati(value)
  def toJsString(value: RegistrationStatus): JsString = JsString(value.toString.toLowerCase)

  implicit val writes = Writes[RegistrationStatus] { regStatus => toJsString(regStatus) }
  implicit val reads = Reads[RegistrationStatus] { regStatus => regStatus.validate[String] map fromString }

}
