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

import java.time.LocalDate

case class FlatRateScheme(joinFrs: Option[Boolean] = None,
                          overBusinessGoods: Option[Boolean] = None,
                          estimateTotalSales: Option[BigDecimal] = None,
                          overBusinessGoodsPercent: Option[Boolean] = None,
                          useThisRate: Option[Boolean] = None,
                          frsStart: Option[LocalDate] = None,
                          categoryOfBusiness: Option[String] = None,
                          percent: Option[BigDecimal] = None,
                          limitedCostTrader: Option[Boolean] = None)

object FlatRateScheme {
  implicit val s4lKey: S4LKey[FlatRateScheme] = S4LKey("flatRateScheme")
  implicit val apiKey: ApiKey[FlatRateScheme] = ApiKey("flat-rate-scheme")

  val oldS4lReads: Reads[FlatRateScheme] = (
    (__ \ "joinFrs").readNullable[Boolean] and
    (__ \ "overBusinessGoods").readNullable[Boolean] and
    (__ \ "estimateTotalSales").readNullable[BigDecimal] and
    (__ \ "overBusinessGoodsPercent").readNullable[Boolean] and
    (__ \ "useThisRate").readNullable[Boolean] and
    (__ \ "frsStart" \ "date").readNullable[LocalDate] and
    (__ \ "categoryOfBusiness").readNullable[String] and
    (__ \ "percent").readNullable[BigDecimal] and
    (__ \ "limitedCostTrader").readNullable[Boolean]
  ) (FlatRateScheme.apply _)

  implicit val format: OFormat[FlatRateScheme] = Json.format[FlatRateScheme]
}

object FRSDateChoice extends Enumeration {
  type FRSDateChoice = Value
  val VATDate = Value
  val DifferentDate = Value

  implicit def toString(f: FRSDateChoice.Value): String = f.toString

  implicit val format: Format[FRSDateChoice] = Format(Reads.enumNameReads(FRSDateChoice), Writes.enumNameWrites)
}