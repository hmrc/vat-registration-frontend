/*
 * Copyright 2020 HM Revenue & Customs
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

package models.external.addresslookup

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class AddressJourneyBuilder(continueUrl: String,
                                 homeNavHref: String,
                                 navTitle: String,
                                 showPhaseBanner: Boolean,
                                 alphaPhase: Boolean,
                                 phaseBannerHtml: String,
                                 includeHMRCBranding: Boolean,
                                 showBackButtons: Boolean,
                                 deskProServiceName: String,
                                 ukMode: Boolean,
                                 lookupPage: LookupPage,
                                 selectPage: SelectPage,
                                 editPage: EditPage,
                                 confirmPage: ConfirmPage)

object AddressJourneyBuilder {
  implicit val writes: Writes[AddressJourneyBuilder] = (
    (__ \ "continueUrl").write[String] and
    (__ \ "homeNavHref").write[String] and
    (__ \ "navTitle").write[String] and
    (__ \ "showPhaseBanner").write[Boolean] and
    (__ \ "alphaPhase").write[Boolean] and
    (__ \ "phaseBannerHtml").write[String] and
    (__ \ "includeHMRCBranding").write[Boolean] and
    (__ \ "showBackButtons").write[Boolean] and
    (__ \ "deskProServiceName").write[String] and
    (__ \ "ukMode").write[Boolean] and
    (__ \ "lookupPage").write[LookupPage](LookupPage.writes) and
    (__ \ "selectPage").write[SelectPage](SelectPage.writes) and
    (__ \ "editPage").write[EditPage](EditPage.writes) and
    (__ \ "confirmPage").write[ConfirmPage](ConfirmPage.writes)
  )(unlift(AddressJourneyBuilder.unapply))
}
