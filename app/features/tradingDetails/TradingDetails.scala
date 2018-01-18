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

package features.tradingDetails

import models.S4LKey
import play.api.libs.json._

case class TradingNameView( yesNo : Boolean,
                            tradingName: Option[String] = None)

object TradingNameView {
  implicit val format = Json.format[TradingNameView]
}

case class TradingDetails(tradingNameView: Option[TradingNameView] = None,
                          euGoods: Option[Boolean] = None,
                          applyEori: Option[Boolean] = None)

object TradingDetails {
  implicit val s4lkey: S4LKey[TradingDetails] = S4LKey("tradingDetails")

  val reads: Reads[TradingDetails] = new Reads[TradingDetails] {
    override def reads(json: JsValue): JsResult[TradingDetails] = {
      val tradingName = (json \ "tradingName").asOpt[String]
      val eoriRequested = (json \ "eoriRequested").asOpt[Boolean]

      JsSuccess(TradingDetails(
        Some(TradingNameView(tradingName.isDefined, tradingName)),
        Some(eoriRequested.isDefined),
        eoriRequested
      ))
    }
  }

  val writes: Writes[TradingDetails] = new Writes[TradingDetails] {
    override def writes(s4l: TradingDetails): JsValue = {
      val tradingName = s4l.tradingNameView.get.tradingName.fold(Json.obj()) {tradingName => Json.obj("tradingName" -> tradingName)}
      val eoriReqObj = s4l.applyEori.fold(Json.obj()) {eori => Json.obj("eoriRequested" -> eori)}

      tradingName ++ eoriReqObj
    }
  }

  val apiFormat = Format[TradingDetails](reads, writes)
  implicit val format = Json.format[TradingDetails]
}
