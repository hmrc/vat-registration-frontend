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

package models.api {
  import play.api.libs.json._

  case class VatFinancials(turnoverEstimate: Long,
                           zeroRatedTurnoverEstimate: Option[Long] = None)

  object VatFinancials {
    implicit val format: OFormat[VatFinancials] = Json.format[VatFinancials]
  }
}

package models {

  import common.ErrorUtil.fail
  import models.api.{VatFinancials, VatScheme}
  import models.view.vatFinancials.{EstimateVatTurnover, EstimateZeroRatedSales, ZeroRatedSales}
  import play.api.libs.json.{Json, OFormat}

  final case class S4LVatFinancials(estimateVatTurnover: Option[EstimateVatTurnover] = None,
                                    zeroRatedTurnover: Option[ZeroRatedSales] = None,
                                    zeroRatedTurnoverEstimate: Option[EstimateZeroRatedSales] = None)

  object S4LVatFinancials {
    implicit val format: OFormat[S4LVatFinancials] = Json.format[S4LVatFinancials]
    implicit val vatFinancials: S4LKey[S4LVatFinancials] = S4LKey("VatFinancials")

    implicit val modelT = new S4LModelTransformer[S4LVatFinancials] {
      // map VatScheme to S4LVatFinancials
      override def toS4LModel(vs: VatScheme): S4LVatFinancials =
        S4LVatFinancials(
          estimateVatTurnover = ApiModelTransformer[EstimateVatTurnover].toViewModel(vs),
          zeroRatedTurnover = ApiModelTransformer[ZeroRatedSales].toViewModel(vs),
          zeroRatedTurnoverEstimate = ApiModelTransformer[EstimateZeroRatedSales].toViewModel(vs)
        )
    }

    def error = throw fail("VatFinancials")

    implicit val apiT = new S4LApiTransformer[S4LVatFinancials, VatFinancials] {
      override def toApi(c: S4LVatFinancials): VatFinancials =
        VatFinancials(
          turnoverEstimate = c.estimateVatTurnover.map(_.vatTurnoverEstimate).getOrElse(error),
          zeroRatedTurnoverEstimate = c.zeroRatedTurnoverEstimate.map(_.zeroRatedTurnoverEstimate)
        )
    }
  }

}
