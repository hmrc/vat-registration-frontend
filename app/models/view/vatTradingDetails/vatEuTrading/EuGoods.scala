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

package models.view.vatTradingDetails.vatEuTrading

import models.{ApiModelTransformer, ViewModelTransformer}
import models.api.{VatScheme, VatSicAndCompliance}
import models.view.sicAndCompliance.labour.TemporaryContracts
import play.api.libs.json.Json

case class EuGoods(yesNo: String)

object TemporaryContracts {

  val TEMP_CONTRACTS_YES = "TEMP_CONTRACTS_YES"
  val TEMP_CONTRACTS_NO = "TEMP_CONTRACTS_NO"

  val valid = (item: String) => List(TEMP_CONTRACTS_YES, TEMP_CONTRACTS_NO).contains(item.toUpperCase)

  implicit val format = Json.format[TemporaryContracts]

  implicit val modelTransformer = ApiModelTransformer[TemporaryContracts] { (vs: VatScheme) =>
    for {
      vsc <- vs.vatSicAndCompliance
      lc <- vsc.labourCompliance
      tc <- lc.temporaryContracts
    } yield TemporaryContracts(if (tc) TEMP_CONTRACTS_YES else TEMP_CONTRACTS_NO)
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: TemporaryContracts, g: VatSicAndCompliance) =>
    g.copy(labourCompliance = g.labourCompliance.map(_.copy(temporaryContracts = Some(c.yesNo == TEMP_CONTRACTS_YES))))
  }

}



