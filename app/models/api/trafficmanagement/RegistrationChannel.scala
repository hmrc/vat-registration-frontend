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

import play.api.libs.json.{JsString, Reads, Writes}

sealed trait RegistrationChannel

case object VatReg extends RegistrationChannel

case object OTRS extends RegistrationChannel

object RegistrationChannel {

  val stati = Map(
    VatReg.toString.toLowerCase -> VatReg,
    OTRS.toString.toLowerCase -> OTRS
  )

  def fromString(value: String): RegistrationChannel = stati(value)
  def toJsString(value: RegistrationChannel): JsString = JsString(value.toString.toLowerCase)

  implicit val writes = Writes[RegistrationChannel] { regStatus => toJsString(regStatus) }
  implicit val reads = Reads[RegistrationChannel] { regStatus => regStatus.validate[String] map fromString }

}
