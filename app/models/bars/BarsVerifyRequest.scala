/*
 * Copyright 2026 HM Revenue & Customs
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

package models.bars

import play.api.libs.json.{Json, OFormat}

case class BarsPersonalRequest(account: BarsAccount, subject: BarsSubject)

object BarsPersonalRequest {
  implicit val format: OFormat[BarsPersonalRequest] = Json.format[BarsPersonalRequest]
}

case class BarsBusinessRequest(account: BarsAccount, business: BarsBusiness)

object BarsBusinessRequest {
  implicit val format: OFormat[BarsBusinessRequest] = Json.format[BarsBusinessRequest]
}

case class BarsBusiness(companyName: String)

object BarsBusiness {
  implicit val format: OFormat[BarsBusiness] = Json.format[BarsBusiness]
}

case class BarsAccount(sortCode: String, accountNumber: String)

object BarsAccount {
  implicit val format: OFormat[BarsAccount] = Json.format[BarsAccount]
}

case class BarsSubject(name: String)

object BarsSubject {
  implicit val format: OFormat[BarsSubject] = Json.format[BarsSubject]
}

