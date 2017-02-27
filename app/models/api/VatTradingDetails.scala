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

package models.api

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class VatTradingDetails(tradingName: String = "")

object VatTradingDetails {
  val apiReads: Reads[VatTradingDetails] =
    (__ \ "trading-name").read[String].map(VatTradingDetails.apply)

  val apiWrites: Writes[VatTradingDetails] =
    (__ \ "trading-name").write[String].contramap(_.tradingName)

  implicit val format = Format(apiReads, apiWrites)

}
