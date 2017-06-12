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

package models.view.sicAndCompliance.financial

import models.api.{VatScheme, VatSicAndCompliance}
import models.{ApiModelTransformer, S4LVatSicAndCompliance, ViewModelFormat, ViewModelTransformer}
import play.api.libs.json.Json

case class ChargeFees(yesNo: Boolean)

object ChargeFees {

  implicit val format = Json.format[ChargeFees]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LVatSicAndCompliance) => group.chargeFees,
    updateF = (c: ChargeFees, g: Option[S4LVatSicAndCompliance]) =>
      g.getOrElse(S4LVatSicAndCompliance()).copy(chargeFees = Some(c))
  )

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer[ChargeFees] { vs: VatScheme =>
    for {
      vsc <- vs.vatSicAndCompliance
      fc <- vsc.financialCompliance
      cf <- fc.chargeFees
    } yield ChargeFees(cf)
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: ChargeFees, g: VatSicAndCompliance) =>
    g.copy(financialCompliance = g.financialCompliance.map(_.copy(chargeFees = Some(c.yesNo))))
  }

}


