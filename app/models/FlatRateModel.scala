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

package models

import java.time.LocalDate

import play.api.libs.json._

object FRSDateChoice extends Enumeration {
  type FRSDateChoice = Value
  val VATDate = Value
  val DifferentDate = Value

  implicit def toString(f : FRSDateChoice.Value) : String = f.toString
  implicit val format: Format[FRSDateChoice] = Format(Reads.enumNameReads(FRSDateChoice), Writes.enumNameWrites)
}

case class FlatRateScheme(joinFrs : Option[Boolean] = None,
                          overBusinessGoods : Option[Boolean] = None,
                          estimateTotalSales : Option[Long] = None,
                          overBusinessGoodsPercent : Option[Boolean] = None,
                          useThisRate : Option[Boolean] = None,
                          frsStart : Option[Start] = None,
                          categoryOfBusiness : Option[String] = None,
                          percent : Option[BigDecimal] = None,
                          limitedCostTrader: Option[Boolean] = None)

object FlatRateScheme {
  val s4lkey: S4LKey[FlatRateScheme] = S4LKey("flatRateScheme")

  val reads: Reads[FlatRateScheme] = new Reads[FlatRateScheme] {
    override def reads(json: JsValue): JsResult[FlatRateScheme] = {

      val joinFrs = (json \ "joinFrs").as[Boolean]
      val details = (json \ "frsDetails").validateOpt[JsObject].get
      val start = details.flatMap { js =>
        val date = (js \ "startDate").asOpt[LocalDate]
        (joinFrs, date) match {
          case (true, None) => Some(Start(None))
          case (false, _)   => None
          case _            => Some(Start(date))
        }
      }

      val businessGoods = details.flatMap(js => (js \ "businessGoods").validateOpt[JsObject].get)

      JsSuccess(FlatRateScheme(
        Some(joinFrs),
        if (!joinFrs && details.isEmpty) None else Some(businessGoods.isDefined),
        businessGoods.map(js => (js \ "estimatedTotalSales").as[Long]),
        businessGoods.map(js => (js \ "overTurnover").as[Boolean]),
        if (details.isEmpty) None else Some(joinFrs),
        start,
        details.flatMap(js => (js \ "categoryOfBusiness").asOpt[String]),
        details.flatMap(js => (js \ "percent").asOpt[BigDecimal]),
        details.flatMap(js => (js \ "limitedCostTrader").asOpt[Boolean])
      ))
    }
  }

  val writes: Writes[FlatRateScheme] = new Writes[FlatRateScheme] {
    override def writes(s4l: FlatRateScheme): JsValue = {
      val businessGoods = if(s4l.overBusinessGoods.contains(true)) {
        Some(Json.obj("businessGoods"   -> Json.obj(
          "estimatedTotalSales"           -> s4l.estimateTotalSales,
          "overTurnover"                  -> s4l.overBusinessGoodsPercent
        )))
      } else {
        None
      }

      val details = Seq(
        s4l.categoryOfBusiness.map(c => Json.obj("categoryOfBusiness" -> c)),
        s4l.percent.map(p => Json.obj("percent" -> p)),
        s4l.limitedCostTrader.map(l => Json.obj("limitedCostTrader" -> l)),
        s4l.frsStart.flatMap(_.date).map(d => Json.obj("startDate" -> d)),
        businessGoods
      ).flatten

      val frsDetails = details.fold(Json.obj()) { (_, _) =>
        Json.obj("frsDetails" -> details.reduceLeft(_ ++ _))
      }

      s4l.joinFrs.fold(throw new Exception("[FlatRateModel] [Writes] No value for joinFrs when expected on submission"))(bool =>
        Json.obj("joinFrs" -> bool) ++ frsDetails)
    }
  }

  val apiFormat = Format[FlatRateScheme](reads, writes)
  implicit val format = Json.format[FlatRateScheme]
}
