/*
 * Copyright 2023 HM Revenue & Customs
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

package models.api.bars.request

import play.api.libs.json.{Json, OFormat}

final case class BarsSubject(
  title:     Option[String], // e.g. "Mr" etc; must >= 2 character and <= 35 characters long
  name:      Option[String], // Must be between 1 and 70 characters long
  firstName: Option[String], // Must be between 1 and 35 characters long
  lastName:  Option[String], // Must be between 1 and 35 characters long
  dob:       Option[String]  // date of birth: ISO-8601 YYYY-MM-DD
) {
  require(
    (name.isEmpty && firstName.isDefined && lastName.isDefined) ||
      (name.isDefined && firstName.isEmpty && lastName.isEmpty)
  )
}

object BarsSubject {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[BarsSubject] = Json.format

}
