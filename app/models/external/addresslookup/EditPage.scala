/*
 * Copyright 2019 HM Revenue & Customs
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

case class EditPage(title: String,
                    heading: String,
                    line1Label: String,
                    line2Label: String,
                    line3Label: String,
                    postcodeLabel: String,
                    countryLabel: String,
                    submitLabel: String,
                    showSearchAgainLink: Boolean,
                    searchAgainLinkText: Option[String])

object EditPage {
  implicit val writes: Writes[EditPage] = (
    (__ \ "title").write[String] and
    (__ \ "heading").write[String] and
    (__ \ "line1Label").write[String] and
    (__ \ "line2Label").write[String] and
    (__ \ "line3Label").write[String] and
    (__ \ "postcodeLabel").write[String] and
    (__ \ "countryLabel").write[String] and
    (__ \ "submitLabel").write[String] and
    (__ \ "showSearchAgainLink").write[Boolean] and
    (__ \ "searchAgainLinkText").writeNullable[String]
  )(unlift(EditPage.unapply))
}
