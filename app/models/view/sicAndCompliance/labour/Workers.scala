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
import play.api.libs.json.{Json, OFormat}

case class Workers(numberOfWorkers: Int)

object Workers {

  implicit val format: OFormat[Workers] = Json.format[Workers]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LVatSicAndCompliance) => group.workers,
    updateF = (c: Workers, g: Option[S4LVatSicAndCompliance]) =>
      g.getOrElse(S4LVatSicAndCompliance()).copy(workers = Some(c))
  )

  implicit val modelTransformer = ApiModelTransformer[Workers] { (vs: VatScheme) =>
    for {
      vsc <- vs.vatSicAndCompliance
      lc <- vsc.labourCompliance
      w <- lc.workers
    } yield Workers(w)
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: Workers, g: VatSicAndCompliance) => {
    g.copy(labourCompliance = g.labourCompliance.map(_.copy(workers = Some(c.numberOfWorkers))))
  }
  }

}

