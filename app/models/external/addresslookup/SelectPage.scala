/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class SelectPage(title: String,
                      heading: String,
                      proposalListLimit: Int,
                      showSearchAgainLink: Boolean,
                      searchAgainLinkText: Option[String],
                      editAddressLinkText: String)

object SelectPage {
  implicit val writes: Writes[SelectPage] = (
    (__ \ "title").write[String] and
    (__ \ "heading").write[String] and
    (__ \ "proposalListLimit").write[Int] and
    (__ \ "showSearchAgainLink").write[Boolean] and
    (__ \ "searchAgainLinkText").writeNullable[String] and
    (__ \ "editAddressLinkText").write[String]
  )(unlift(SelectPage.unapply))
}
