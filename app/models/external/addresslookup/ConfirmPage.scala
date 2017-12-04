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

package models.external.addresslookup

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class ConfirmPage(title: String,
                       heading: String,
                       showSubHeadingAndInfo: Boolean,
                       submitLabel: String,
                       showSearchAgainLink: Boolean,
                       showChangeLink: Boolean,
                       changeLinkText: Option[String])

object ConfirmPage {
  implicit val writes: Writes[ConfirmPage] = (
    (__ \ "title").write[String] and
    (__ \ "heading").write[String] and
    (__ \ "showSubHeadingAndInfo").write[Boolean] and
    (__ \ "submitLabel").write[String] and
    (__ \ "showSearchAgainLink").write[Boolean] and
    (__ \ "showChangeLink").write[Boolean] and
    (__ \ "changeLinkText").writeNullable[String]
  )(unlift(ConfirmPage.unapply))
}
