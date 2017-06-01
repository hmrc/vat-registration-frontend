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

package models.api

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Officer(
                    name: Name,
                    role: String,
                    dateOfBirth: Option[DateOfBirth] = None,
                    resignedOn: Option[DateTime] = None,
                    appointmentLink: Option[String] = None // custom read to pick up (if required - TBC)
                  ){

  override def equals(obj: Any): Boolean = obj match {
    case Officer(nameObj, roleObj, _, _, _)
      if role.equalsIgnoreCase(roleObj) && (nameObj == name) => true
    case _ => false
  }

  override def hashCode: Int = 1 // TODO temporary fix
}

object Officer {

  implicit val rd: Reads[Officer] = (
      (__ \ "name_elements").read[Name](Name.normalizeNameReads) and
          (__ \ "officer_role").read[String] and
          (__ \ "date_of_birth").readNullable[DateOfBirth] and
          (__ \ "resigned_on").readNullable[DateTime] and
          (__ \ "appointment_link").readNullable[String]
    ) (Officer.apply _)

  implicit val wt: Writes[Officer] = (
      (__ \ "name_elements").write[Name] and
          (__ \ "officer_role").write[String] and
          (__ \ "date_of_birth").writeNullable[DateOfBirth] and
          (__ \ "resigned_on").writeNullable[DateTime] and
          (__ \ "appointment_link").writeNullable[String]
    ) (unlift(Officer.unapply))

  val empty = Officer(Name.empty, "", None, None, None)

}

case class OfficerList(items: Seq[Officer])

object OfficerList {
  implicit val reads: Reads[OfficerList] = (__ \ "officers").read[Seq[Officer]] map OfficerList.apply
}
