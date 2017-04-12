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
import models.{ApiModelTransformer, ViewModelTransformer}
import play.api.libs.json.Json

case class AdviceOrConsultancy(yesNo: Boolean)

object AdviceOrConsultancy {

  val ADVICE_CONSULTANCY_YES = true
  val ADVICE_CONSULTANCY_NO = false

  implicit val format = Json.format[AdviceOrConsultancy]

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer[AdviceOrConsultancy] { vs: VatScheme =>
    //TODO: Implement once backend and frontend API models are in place for FinancialComplianceType
    None
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: AdviceOrConsultancy, g: VatSicAndCompliance) =>
    //TODO: Implement once backend and frontend API models are in place for FinancialComplianceType
    g
  }

}

