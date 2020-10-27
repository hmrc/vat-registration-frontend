/*
 * Copyright 2020 HM Revenue & Customs
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


import cats.Show
import cats.Show.show
import org.apache.commons.lang3.text.WordUtils
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.JodaReads._
import play.api.libs.json.JodaWrites._
import play.api.libs.json._

case class Name(first: Option[String],
                middle: Option[String],
                last: String,
                title: Option[String] = None) {

  import cats.instances.option._
  import cats.syntax.applicative._
  import models.external.Name.inlineShow.inline

  val id: String = List(title,
    first,
    middle,
    last.pure
  ).flatten.mkString.replaceAll(" ", "")

  val asLabel: String = inline show this

}

object Name {
  implicit val format: Format[Name] = Json.format[Name]

  private def normalisedSeq(name: Name): Seq[String] = {
    import cats.instances.option._
    import cats.syntax.applicative._

    Seq[Option[String]](
      name.title,
      name.first,
      name.middle,
      name.last.pure
    ) flatMap (_ map WordUtils.capitalizeFully)

  }

  object htmlShow {
    implicit val html: Show[Name] = show((name: Name) => normalisedSeq(name).mkString("<br/>"))
  }

  object inlineShow {
    implicit val inline = show((name: Name) => normalisedSeq(name).mkString(" "))
  }

}

case class Applicant(
                    name: Name,
                    role: String,
                    resignedOn: Option[DateTime] = None,
                    appointmentLink: Option[String] = None // custom read to pick up (if required - TBC)
                  ) {

  override def equals(obj: Any): Boolean = obj match {
    case Applicant(nameObj, roleObj, _, _)
      if role.equalsIgnoreCase(roleObj) && (nameObj == name) => true
    case _ => false
  }

  override def hashCode: Int = 1 // bit of a hack, but works
}

object Applicant {

  implicit val rd: Reads[Applicant] = (
    (__ \ "name_elements").read[Name] and
      (__ \ "applicant_role").read[String] and
      (__ \ "resigned_on").readNullable[DateTime] and
      (__ \ "appointment_link").readNullable[String]
    ) (Applicant.apply _)

  implicit val wt: Writes[Applicant] = (
    (__ \ "name_elements").write[Name] and
      (__ \ "applicant_role").write[String] and
      (__ \ "resigned_on").writeNullable[DateTime] and
      (__ \ "appointment_link").writeNullable[String]
    ) (unlift(Applicant.unapply))
}
