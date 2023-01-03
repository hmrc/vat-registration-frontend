/*
 * Copyright 2023 HM Revenue & Customs
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

package models.api.vatapplication

import play.api.libs.json._
import services.VatApplicationService.OverseasComplianceAnswer

case class OverseasCompliance(goodsToOverseas: Option[Boolean] = None,
                              goodsToEu: Option[Boolean] = None,
                              storingGoodsForDispatch: Option[StoringGoodsForDispatch] = None,
                              usingWarehouse: Option[Boolean] = None,
                              fulfilmentWarehouseNumber: Option[String] = None,
                              fulfilmentWarehouseName: Option[String] = None)

object OverseasCompliance {
  implicit val format: Format[OverseasCompliance] = Json.format[OverseasCompliance]
}

sealed trait StoringGoodsForDispatch extends OverseasComplianceAnswer
case object StoringWithinUk extends StoringGoodsForDispatch
case object StoringOverseas extends StoringGoodsForDispatch

object StoringGoodsForDispatch {
  val statusMap: Map[StoringGoodsForDispatch, String] = Map(
    StoringWithinUk -> "UK",
    StoringOverseas -> "OVERSEAS"
  )
  val inverseMap: Map[String, StoringGoodsForDispatch] = statusMap.map(_.swap)

  def fromString(value: String): StoringGoodsForDispatch = inverseMap(value)

  def toJsString(value: StoringGoodsForDispatch): JsString = JsString(statusMap(value))

  val writes: Writes[StoringGoodsForDispatch] = Writes[StoringGoodsForDispatch] { storingGoods =>
    toJsString(storingGoods)
  }
  val reads: Reads[StoringGoodsForDispatch] = Reads[StoringGoodsForDispatch] { storingGoods =>
    storingGoods.validate[String] map fromString
  }
  implicit val format: Format[StoringGoodsForDispatch] = Format(reads, writes)
}
