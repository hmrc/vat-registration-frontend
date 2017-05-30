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

import models.api._
import models.{ApiModelTransformer, S4LVatLodgingOfficer, VMReads, ViewModelTransformer}
import play.api.libs.json.{Json, OFormat}

case class OfficerContactDetailsView(
                                      email: Option[String] = None,
                                      daytimePhone: Option[String] = None,
                                      mobile: Option[String] = None
                                    )


object OfficerContactDetailsView {

  implicit val format: OFormat[OfficerContactDetailsView] = Json.format[OfficerContactDetailsView]


  implicit val vmReads = VMReads(
    readF = (group: S4LVatLodgingOfficer) => group.officerContactDetails,
    updateF = (c: OfficerContactDetailsView, g: Option[S4LVatLodgingOfficer]) =>
      g.getOrElse(S4LVatLodgingOfficer()).copy(officerContactDetails = Some(c))
  )

  implicit val modelTransformer = ApiModelTransformer[OfficerContactDetailsView] { (vs: VatScheme) =>
    vs.lodgingOfficer.map(_.contact).collect {
      case VatDigitalContact(email,tel,mob) =>
        OfficerContactDetailsView(email = Some(email), daytimePhone = tel, mobile = mob)
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: OfficerContactDetailsView, g: VatLodgingOfficer) =>
    g.copy(contact = VatDigitalContact(c.email.getOrElse(""), c.daytimePhone, c.mobile))
  }

}

