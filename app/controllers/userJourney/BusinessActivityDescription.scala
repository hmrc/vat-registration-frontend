package controllers.userJourney

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
class BusinessActivityDescription {

}

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

import play.api.libs.json.Json
import

case class BusinessActivityDescription(vatTurnoverEstimate: Option[Long] = None)

object BusinessActivityDescription {

  implicit val format = Json.format[BusinessActivityDescription]

  implicit val modelTransformer = ApiModelTransformer { (vs: VatScheme) =>
    vs.si.map(_.turnoverEstimate).collect {
      case turnoverEstimate => BusinessActivityDescription(Some(turnoverEstimate))
    }.getOrElse(BusinessActivityDescription())
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: BusinessActivityDescription, g: SICAndComplianceType) =>
    g.copy(turnoverEstimate = c.vatTurnoverEstimate.getOrElse(0L))
  }

}
