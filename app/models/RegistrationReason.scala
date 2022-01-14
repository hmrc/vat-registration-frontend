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

package models

import play.api.libs.json._

sealed trait RegistrationReason {
  val key: String
  val humanReadableKey: String
}

object RegistrationReason {
  val mapping = Map(
    ForwardLook.key -> ForwardLook,
    BackwardLook.key -> BackwardLook,
    Voluntary.key -> Voluntary,
    NonUk.key -> NonUk,
    TransferOfAGoingConcern.key -> TransferOfAGoingConcern,
    GroupRegistration.key -> GroupRegistration,
    SuppliesOutsideUk.key -> SuppliesOutsideUk
  )
  val inverseMapping: Map[RegistrationReason, String] = mapping.map(_.swap)

  val reads: Reads[RegistrationReason] = Reads { json => json.validate[String].map(mapping) }
  val writes: Writes[RegistrationReason] = Writes { registrationReason => JsString(registrationReason.key) }
  implicit val format: Format[RegistrationReason] = Format(reads, writes)
}

object ForwardLook extends RegistrationReason {
  val key = "0016"
  val humanReadableKey = "Forward Look"
}

object BackwardLook extends RegistrationReason {
  val key = "0015"
  val humanReadableKey = "Backward Look"
}

object Voluntary extends RegistrationReason {
  val key = "0018"
  val humanReadableKey = "Voluntary"
}

object NonUk extends RegistrationReason {
  val key = "0003"
  val humanReadableKey = "Non-UK"
}

object SuppliesOutsideUk extends RegistrationReason {
  val key = "0017"
  val humanReadableKey = "Supplies outside the UK"
}

object GroupRegistration extends RegistrationReason {
  val key = "0011"
  val humanReadableKey = "Group Registration"
}

object TransferOfAGoingConcern extends RegistrationReason {
  val key = "0014"
  val humanReadableKey = "Transfer of a Going Concern"
}
