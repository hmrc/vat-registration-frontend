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
package models.view.sicAndCompliance

import models.api.{VatComplianceLabour, VatScheme, VatSicAndCompliance}
import models.{ApiModelTransformer, ViewModelTransformer}
import play.api.libs.json.Json

case class  CompanyProvideWorkers(yesNo: String)

object CompanyProvideWorkers {

  val PROVIDE_WORKERS_YES = "PROVIDE_WORKERS_YES"
  val PROVIDE_WORKERS_NO = "PROVIDE_WORKERS_NO"

  val valid = (item: String) => List(PROVIDE_WORKERS_YES, PROVIDE_WORKERS_NO).contains(item.toUpperCase)

  implicit val format = Json.format[CulturalComplianceQ1]

  implicit val modelTransformer = ApiModelTransformer[CulturalComplianceQ1] { (vs: VatScheme) =>
    vs.vatSicAndCompliance.flatMap(_.culturalCompliance).map { q1 =>
      CulturalComplianceQ1(if (q1.notForProfit) PROVIDE_WORKERS_YES else PROVIDE_WORKERS_NO)
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: CompanyProvideWorkers, g: VatSicAndCompliance) =>
    g.copy(labourCompliance = Some(VatComplianceLabour(c.yesNo == PROVIDE_WORKERS_YES, None, None, None)))
  }

}
