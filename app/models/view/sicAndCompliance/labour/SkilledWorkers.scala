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

package models.view.sicAndCompliance.labour

import models.api.{VatScheme, VatSicAndCompliance}
import models.{ApiModelTransformer, S4LVatSicAndCompliance, ViewModelFormat, ViewModelTransformer}
import play.api.libs.json.Json

case class  SkilledWorkers(yesNo: String)

object SkilledWorkers {

  val SKILLED_WORKERS_YES = "SKILLED_WORKERS_YES"
  val SKILLED_WORKERS_NO = "SKILLED_WORKERS_NO"

  val valid = (item: String) => List(SKILLED_WORKERS_YES, SKILLED_WORKERS_NO).contains(item.toUpperCase)

  implicit val format = Json.format[SkilledWorkers]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LVatSicAndCompliance) => group.skilledWorkers,
    updateF = (c: SkilledWorkers, g: Option[S4LVatSicAndCompliance]) =>
      g.getOrElse(S4LVatSicAndCompliance()).copy(skilledWorkers = Some(c))
  )

  implicit val modelTransformer = ApiModelTransformer[SkilledWorkers] { (vs: VatScheme) =>
    for {
      vsc <- vs.vatSicAndCompliance
      lc <- vsc.labourCompliance
      sw <- lc.skilledWorkers
    } yield SkilledWorkers(if (sw) SKILLED_WORKERS_YES else SKILLED_WORKERS_NO)
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: SkilledWorkers, g: VatSicAndCompliance) =>
    g.copy(labourCompliance = g.labourCompliance.map(_.copy(skilledWorkers = Some(c.yesNo == SKILLED_WORKERS_YES))))
  }

}
