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

import features.returns.models.Start
import models.S4LKey
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json._

import scala.util.control.NoStackTrace

object AnnualCosts extends Enumeration {
  type AnnualCosts = Value
  val AlreadyDoesSpend = Value
  val WillSpend = Value
  val DoesNotSpend = Value

  implicit def toString(f : AnnualCosts.Value) : String = f.toString
  implicit val format = Format(Reads.enumNameReads(AnnualCosts), Writes.enumNameWrites)
  implicit def boolTo(bool : Option[Boolean]) = bool.map(if (_) AnnualCosts.AlreadyDoesSpend else AnnualCosts.DoesNotSpend)

  def toBool(f : AnnualCosts.Value): Boolean = f match {
    case AnnualCosts.DoesNotSpend => false
    case _ => true
  }
  def toBool(f : Option[AnnualCosts.Value]): Option[Boolean] = f map {
    case AnnualCosts.DoesNotSpend => false
    case _ => true
  }

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
      val start = (json \ "frsDetails" \ "start").asOpt[Start]
      val joinFrs = (json \ "joinFrs").asOpt[Boolean]

      JsSuccess(FlatRateScheme(
        joinFrs,
        (json \ "frsDetails" \ "overBusinessGoods").asOpt[Boolean],
        (json \ "frsDetails" \ "vatInclusiveTurnover").asOpt[Long],
        (json \ "frsDetails" \ "overBusinessGoodsPercent").asOpt[Boolean],
        if (joinFrs.get) Some(start.fold(false)(_ => true)) else None,
        start,
        (json \ "frsDetails" \ "categoryOfBusiness").asOpt[String],
        (json \ "frsDetails" \ "percent").asOpt[BigDecimal]
      ))
    }
  }

  val writes: Writes[FlatRateScheme] = new Writes[FlatRateScheme] {
    override def writes(s4l: FlatRateScheme): JsValue = {
      def purgeNull(jsObj : JsObject) : JsObject =
        JsObject(jsObj.value.filterNot {
          case (_, value) => value == JsNull
        })

      s4l.joinFrs match {
        case Some(false)    => Json.obj("joinFrs" -> false)
        case Some(true)     => Json.obj("joinFrs" -> true, "frsDetails" -> purgeNull(Json.obj(
          "overBusinessGoods" -> AnnualCosts.toBool(s4l.overBusinessGoods),
          "categoryOfBusiness" -> s4l.categoryOfBusiness,
          "percent" -> s4l.percent,
          "overBusinessGoodsPercent" -> AnnualCosts.toBool(s4l.overBusinessGoodsPercent),
          "vatInclusiveTurnover" -> s4l.vatTaxableTurnover,
          "start" -> s4l.frsStart
        )))
        case None           => throw new Exception("[FlatRateModel] [Writes] No value for joinFrs when expected on submission")
      }
    }
  }

  val apiFormat = Format[FlatRateScheme](reads, writes)
  implicit val format = Json.format[FlatRateScheme]

  def empty = FlatRateScheme()
}