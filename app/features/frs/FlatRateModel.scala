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

package frs

import java.time.LocalDate

import features.returns.Start
import models.S4LKey
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json._

object AnnualCosts extends Enumeration {
  type AnnualCosts = Value
  val AlreadyDoesSpend = Value
  val WillSpend = Value
  val DoesNotSpend = Value

  implicit def toString(f : AnnualCosts.Value) : String = f.toString
  implicit val format = Format(Reads.enumNameReads(AnnualCosts), Writes.enumNameWrites)

  def toBool(f : Option[AnnualCosts.Value]): Option[Boolean] = f map {
    case AnnualCosts.DoesNotSpend => false
    case _ => true
  }
  def fromBool(bool : Boolean): AnnualCosts.Value = if (bool) AnnualCosts.AlreadyDoesSpend else AnnualCosts.DoesNotSpend

  implicit def formatter : Formatter[AnnualCosts] = new Formatter[AnnualCosts] {
    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key,"")).right.flatMap {
        case "yes"                  => Right(AnnualCosts.AlreadyDoesSpend)
        case "yesWithin12months"     => Right(AnnualCosts.WillSpend)
        case "no"                   => Right(AnnualCosts.DoesNotSpend)
        case _                      => Left(Seq(FormError(key, "error.required", Nil)))
      }
    }
    def unbind(key: String, value: AnnualCosts) = Map(key -> value.toString)
  }
}

object FRSDateChoice extends Enumeration {
  type FRSDateChoice = Value
  val VATDate = Value
  val DifferentDate = Value

  implicit def toString(f : FRSDateChoice.Value) : String = f.toString
  implicit val format = Format(Reads.enumNameReads(FRSDateChoice), Writes.enumNameWrites)
}

case class FlatRateScheme(
                           joinFrs : Option[Boolean] = None,
                           overBusinessGoods : Option[AnnualCosts.Value] = None,
                           vatTaxableTurnover : Option[Long] = None,
                           overBusinessGoodsPercent : Option[AnnualCosts.Value] = None,
                           useThisRate : Option[Boolean] = None,
                           frsStart : Option[Start] = None,
                           categoryOfBusiness : Option[String] = None,
                           percent : Option[BigDecimal] = None
                         )

object FlatRateScheme {
  val s4lkey: S4LKey[FlatRateScheme] = S4LKey("flatRateScheme")

  val reads: Reads[FlatRateScheme] = new Reads[FlatRateScheme] {
    override def reads(json: JsValue): JsResult[FlatRateScheme] = {

      val joinFrs = (json \ "joinFrs").as[Boolean]
      val details = (json \ "frsDetails").validateOpt[JsObject].get
      val start = details.flatMap(js => (js \ "startDate").asOpt[LocalDate].map(date => Start(Some(date))))
      val businessGoods = details.flatMap(js => (js \ "businessGoods").validateOpt[JsObject].get)

      JsSuccess(FlatRateScheme(
        Some(joinFrs),
        if (!joinFrs && businessGoods.isEmpty) None else Some(AnnualCosts.fromBool(businessGoods.isDefined)),
        businessGoods.map(js => (js \ "estimatedTotalSales").as[Long]),
        businessGoods.map(js => AnnualCosts.fromBool((js \ "overTurnover").as[Boolean])),
        if (details.isEmpty) None else Some(joinFrs),
        start,
        details.flatMap(js => (js \ "categoryOfBusiness").asOpt[String]),
        details.flatMap(js => (js \ "percent").asOpt[BigDecimal])
      ))
    }
  }

  val writes: Writes[FlatRateScheme] = new Writes[FlatRateScheme] {
    override def writes(s4l: FlatRateScheme): JsValue = {
      val businessGoods = if(AnnualCosts.toBool(s4l.overBusinessGoods).contains(true)) {
        Some(Json.obj("businessGoods"   -> Json.obj(
          "estimatedTotalSales"           -> s4l.vatTaxableTurnover,
          "overTurnover"                  -> AnnualCosts.toBool(s4l.overBusinessGoodsPercent)
        )))
        } else None

      val details = Seq(
        s4l.categoryOfBusiness.map(c    => Json.obj("categoryOfBusiness" -> c)),
        s4l.percent.map(p               => Json.obj("percent" -> p)),
        s4l.frsStart.map(_.date).map(d  => Json.obj("startDate" -> d)),
        businessGoods
      ).flatten
     val frsDetails = if (details.isEmpty) {
        Json.obj()
      } else {
        Json.obj("frsDetails" -> details.fold(Json.obj())((a,b) => a ++ b))
      }

      s4l.joinFrs.fold(throw new Exception("[FlatRateModel] [Writes] No value for joinFrs when expected on submission"))(bool =>
        Json.obj("joinFrs" -> bool) ++ frsDetails)
    }
  }

  val apiFormat = Format[FlatRateScheme](reads, writes)
  implicit val format = Json.format[FlatRateScheme]
}