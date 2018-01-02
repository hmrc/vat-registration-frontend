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

package features.officers.models.view

import models.external.Officer
import models.view.vatLodgingOfficer.OfficerSecurityQuestionsView
import play.api.libs.json._

case class LodgingOfficer(completionCapacity: Option[String],
                          securityQuestions: Option[OfficerSecurityQuestionsView])

object LodgingOfficer {
  implicit val format: Format[LodgingOfficer] = Json.format[LodgingOfficer]

  def apiWrites(officer: Officer): Writes[LodgingOfficer] = new Writes[LodgingOfficer] {
    override def writes(o: LodgingOfficer) = {

      val lastName = Json.obj("last" -> officer.name.surname)
      val firstName = officer.name.forename.fold(Json.obj())(v => Json.obj("first" -> v))
      val middleName = officer.name.otherForenames.fold(Json.obj())(v => Json.obj("middle" -> v))
      val name = lastName ++ firstName ++ middleName

      name ++ Json.parse(
        s"""
           |{
           | "role" : "${officer.role}"
           | "dob" : "${o.securityQuestions.get.dob}",
           | "nino" : "${o.securityQuestions.get.nino}"
           |}
        """.stripMargin
      ).as[JsObject]
    }
  }
}
