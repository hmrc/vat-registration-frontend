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

package models.view

import models.api.{VatFinancials, VatScheme}
import models.{ApiModelTransformer, ViewModelTransformer}
import play.api.libs.json.Json

case class EstimateVatTurnover(vatTurnoverEstimate: Option[Long])

object EstimateVatTurnover {

  implicit val format = Json.format[EstimateVatTurnover]

  implicit val modelTransformer = ApiModelTransformer[EstimateVatTurnover] { (vs: VatScheme) =>
    vs.financials.map(_.turnoverEstimate).collect {
      case turnoverEstimate => EstimateVatTurnover(Some(turnoverEstimate))
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: EstimateVatTurnover, g: VatFinancials) => {
      c.vatTurnoverEstimate match {
        case Some(turnover) => g.copy(turnoverEstimate = turnover)
        case None => g
      }
    }
  }

}
