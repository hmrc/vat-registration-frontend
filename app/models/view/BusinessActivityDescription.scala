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

import models.api.{SicAndCompliance, VatScheme}
import models.{ApiModelTransformer, ViewModelTransformer}
import play.api.libs.json.Json

case class BusinessActivityDescription(description: String)

object BusinessActivityDescription {

  implicit val format = Json.format[BusinessActivityDescription]

  implicit val modelTransformer = ApiModelTransformer { (vs: VatScheme) =>
    vs.sicAndCompliance.map(_.description).collect {
      case description => BusinessActivityDescription(description)
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: BusinessActivityDescription, g: SicAndCompliance) =>
    g.copy(description = c.description)
  }

}
