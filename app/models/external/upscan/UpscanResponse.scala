/*
 * Copyright 2022 HM Revenue & Customs
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

package models.external.upscan

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, __}

case class UpscanResponse(reference: String, href: String, fields: Map[String, String])

object UpscanResponse {

  implicit val reads: Format[UpscanResponse] = (
    (__ \ "reference").format[String] and
      (__ \ "uploadRequest" \ "href").format[String] and
      (__ \ "uploadRequest" \ "fields").format[Map[String, String]]
    ) (UpscanResponse.apply, unlift(UpscanResponse.unapply))

}
