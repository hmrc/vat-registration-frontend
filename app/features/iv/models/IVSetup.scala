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

package features.iv.models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class IVSetup(origin: String,
                   completionURL: String,
                   failureURL: String,
                   confidenceLevel: Int,
                   userData: UserData)

case class UserData(firstName: String,
                    lastName: String,
                    dateOfBirth: String,
                    nino: String)

object UserData {
  implicit val writes: Writes[UserData] = (
    (__ \ "firstName").write[String] and
    (__ \ "lastName").write[String] and
    (__ \ "dateOfBirth").write[String] and
    (__ \ "nino").write[String]
  )(unlift(UserData.unapply))
}

object IVSetup {
  implicit val writes: Writes[IVSetup] = (
    (__ \ "origin").write[String] and
    (__ \ "completionURL").write[String] and
    (__ \ "failureURL").write[String] and
    (__ \ "confidenceLevel").write[Int] and
    (__ \ "userData").write[UserData]
  )(unlift(IVSetup.unapply))
}
