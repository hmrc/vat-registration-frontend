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

import models.ApiModelTransformer
import models.api.VatScheme
import models.external.Officer
import play.api.libs.json.Json

/**
  * Used to create an view of the external model Officer
  * from a persisted VatLodgingOfficer
  *
  * TODO maybe we don't need this class, instead we can
  * put the modelTransformer in the Officer class...
  */
case class OfficerView(officer: Officer)

object OfficerView {
  implicit val format = Json.format[OfficerView]

  // return a view model from a VatScheme instance
  implicit val modelTransformer = ApiModelTransformer[OfficerView] { vs: VatScheme =>
    vs.lodgingOfficer.map {
      lodgingOfficer => OfficerView(Officer(
        name = lodgingOfficer.name.get,
        role = lodgingOfficer.role.get,
        dateOfBirth = Some(lodgingOfficer.dob.get)))
    }
  }

}