/*
 * Copyright 2021 HM Revenue & Customs
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

package models.external

import play.api.libs.json.{OFormat, __}
import play.api.libs.functional.syntax._

case class CompanyRegistrationProfile(status: String,
                                      ctStatus: Option[String] = None)

object CompanyRegistrationProfile {
  implicit val format: OFormat[CompanyRegistrationProfile] = (
    (__ \ "status").format[String] and
    (__ \ "ctStatus").formatNullable[String]
  )(CompanyRegistrationProfile.apply, unlift(CompanyRegistrationProfile.unapply))
}
