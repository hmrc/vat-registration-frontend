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

import models.{ApiModelTransformer, ViewModelTransformer}
import models.api.{VatContact, VatDigitalContact, VatLodgingOfficer, VatScheme}
import models.view.vatContact.BusinessContactDetails
import play.api.libs.json.{Json, OFormat}

case class OfficerContactDetails(
                                   email: Option[String] = None,
                                   daytimePhone: Option[String] = None,
                                   mobile: Option[String] = None
                                 )


object OfficerContactDetails {

  implicit val format: OFormat[OfficerContactDetails] = Json.format[OfficerContactDetails]

  implicit val modelTransformer = ApiModelTransformer[OfficerContactDetails] { (vs: VatScheme) =>
    Some(OfficerContactDetails(None,None,None))
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: OfficerContactDetails, g: VatLodgingOfficer) =>
    g
  }

}

