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

package features.officers.models.view

import models.view.vatLodgingOfficer.{CompletionCapacityView, OfficerSecurityQuestionsView}
import play.api.libs.json._

case class LodgingOfficer(comletionCapacity: Option[String],
                          securityQuestions: Option[OfficerSecurityQuestionsView])

object LodgingOfficer {
  implicit val format: Format[LodgingOfficer] = Json.format[LodgingOfficer]

  val apiWrites: Writes[LodgingOfficer] = new Writes[LodgingOfficer] {
    override def writes(o: LodgingOfficer) = Json.parse(
      s"""
        |{
        | "name" : {
        |   "first" : "${o.comletionCapacity.get.completionCapacity.get.name.forename.get}",
        |   "middle" : "${o.comletionCapacity.get.completionCapacity.get.name.otherForenames.getOrElse("")}",
        |   "last" : "${o.comletionCapacity.get.completionCapacity.get.name.surname}",
        | },
        | "role" : "${o.comletionCapacity.get.completionCapacity.get.role}"
        | "dob" : "${o.securityQuestions.get.dob}",
        | "nino" : "${o.securityQuestions.get.nino}"
        |}
      """.stripMargin
    )
  }
}
