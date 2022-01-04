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

package forms

import models.api.returns.{StoringGoodsForDispatch, StoringOverseas, StoringWithinUk}
import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

class StoringGoodsForm {
  private final val field = "value"

  private final val UkWarehouseValue = "UK"
  private final val OverseasWarehouseValue = "OVERSEAS"

  val form = Form[StoringGoodsForDispatch](
    single(
      field -> of(formatter)
    )
  )


  def formatter: Formatter[StoringGoodsForDispatch] = new Formatter[StoringGoodsForDispatch] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], StoringGoodsForDispatch] =
      data.get(field) match {
        case Some(UkWarehouseValue) => Right(StoringWithinUk)
        case Some(OverseasWarehouseValue) => Right(StoringOverseas)
        case _ => Left(Seq(FormError(field, "pages.netp.warehouseLocation.error")))
      }

    override def unbind(key: String, value: StoringGoodsForDispatch): Map[String, String] = {
      val answer = value match {
        case StoringWithinUk => UkWarehouseValue
        case StoringOverseas => OverseasWarehouseValue
      }

      Map(field -> answer)
    }
  }
}
