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

import models._
import models.api.VatScheme
import play.api.libs.json.Json

case class BusinessActivityDescription(description: String)

object BusinessActivityDescription {

  implicit val format = Json.format[BusinessActivityDescription]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LVatSicAndCompliance) => group.description,
    updateF = (c: BusinessActivityDescription, g: Option[S4LVatSicAndCompliance]) =>
      g.getOrElse(S4LVatSicAndCompliance()).copy(description = Some(c))
  )

  implicit val modelTransformer = ApiModelTransformer { (vs: VatScheme) =>
    vs.vatSicAndCompliance.map(_.businessDescription).collect {
      case description => BusinessActivityDescription(description)
    }
  }

}
