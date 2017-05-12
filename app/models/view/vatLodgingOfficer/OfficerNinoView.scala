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

package models.view.vatLodgingOfficer

import models.api.{VatLodgingOfficer, VatScheme}
import models.{ApiModelTransformer, ViewModelTransformer}
import play.api.libs.json.Json

case class OfficerNinoView(nino: String)

object OfficerNinoView {

  implicit val format = Json.format[OfficerNinoView]

  // return a view model from a VatScheme instance
  implicit val modelTransformer = ApiModelTransformer[OfficerNinoView] { vs: VatScheme =>
    vs.lodgingOfficer.map(_.nino).collect {
      case n: String => OfficerNinoView(n)
    }
  }

  // return a new or updated VatLodgingOfficer from the CurrentAddressView instance
  implicit val viewModelTransformer = ViewModelTransformer { (c: OfficerNinoView, g: VatLodgingOfficer) => {
      g.copy(nino = c.nino)
    }
  }
}
