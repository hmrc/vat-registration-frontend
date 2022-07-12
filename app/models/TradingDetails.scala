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

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class TradingNameView(yesNo: Boolean,
                           tradingName: Option[String] = None)

object TradingNameView {
  implicit val format = Json.format[TradingNameView]
}

case class TradingDetails(tradingNameView: Option[TradingNameView] = None,
                          shortOrgName: Option[String] = None)

object TradingDetails {
  implicit val s4lKey: S4LKey[TradingDetails] = S4LKey("tradingDetails")

  val reads: Reads[TradingDetails] = (
    (__ \ "tradingName").readNullable[String].map[Option[TradingNameView]](tradingName => Some(TradingNameView(tradingName.isDefined, tradingName))) and
      (__ \ "shortOrgName").readNullable[String]
    ) (TradingDetails.apply _)

  val writes: Writes[TradingDetails] = (
    (__ \ "tradingName").writeNullable[String].contramap[Option[TradingNameView]](_.flatMap(_.tradingName)) and
      (__ \ "shortOrgName").writeNullable[String]
    ) (unlift(TradingDetails.unapply))

  val apiFormat = Format[TradingDetails](reads, writes)
  implicit val format = Json.format[TradingDetails]
}