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

package models.external

import java.time.LocalDate

import play.api.libs.json._


final case class IncorpSubscription(transactionId: String, regime: String, subscriber: String, callbackUrl: String)

object IncorpSubscription {
  implicit val format = Json.format[IncorpSubscription]
}

final case class IncorpStatusEvent(status: String, crn: Option[String], incorporationDate: Option[LocalDate], description: Option[String])

object IncorpStatusEvent {
  implicit val format = Json.format[IncorpStatusEvent]
}

final case class IncorporationInfo(subscription: IncorpSubscription, statusEvent: IncorpStatusEvent)

object IncorporationInfo {
  implicit val format = Json.format[IncorporationInfo]
}
