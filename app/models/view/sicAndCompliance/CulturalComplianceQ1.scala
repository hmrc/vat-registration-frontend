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

import models.api.compliance.VatCulturalCompliance
import models.{ApiModelTransformer, ViewModelTransformer}
import models.api.{VatScheme, VatSicAndCompliance}
import play.api.libs.json.Json

case class CulturalComplianceQ1(yesNo: String)

object CulturalComplianceQ1 {

  val NOT_PROFIT_YES = "NOT_PROFIT_YES"
  val NOT_PROFIT_NO = "NOT_PROFIT_NO"

  val valid = (item: String) => List(NOT_PROFIT_YES, NOT_PROFIT_NO).contains(item.toUpperCase)

  implicit val format = Json.format[CulturalComplianceQ1]

  implicit val modelTransformer = ApiModelTransformer[CulturalComplianceQ1] { (vs: VatScheme) =>
    None
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: CulturalComplianceQ1, g: VatSicAndCompliance) =>
    g.copy(culturalCompliance = Some(VatCulturalCompliance(c.yesNo == NOT_PROFIT_YES)))
  }

}
