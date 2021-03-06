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

import models.S4LKey
import play.api.libs.json.{Json, OFormat}

case class EmailAddress(email: String)

object EmailAddress {
  implicit val emailAddress: S4LKey[EmailAddress] = S4LKey("email")
  implicit val format: OFormat[EmailAddress] = Json.format[EmailAddress]
}

case class EmailVerified(emailVerified: Boolean)

object EmailVerified {
  implicit val emailVerified: S4LKey[EmailVerified] = S4LKey("emailVerified")
  implicit val format: OFormat[EmailVerified] = Json.format[EmailVerified]
}
