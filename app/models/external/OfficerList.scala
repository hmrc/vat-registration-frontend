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

package models.external


import cats.Show
import cats.Show.show
import models.Formatters
import org.apache.commons.lang3.text.WordUtils
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Name(forename: Option[String],
                otherForenames: Option[String],
                surname: String,
                title: Option[String] = None) {

  import cats.instances.option._
  import cats.syntax.applicative._
  import models.external.Name.inlineShow.inline

  val id: String = List(title,
    forename,
    otherForenames,
    surname.pure
  ).flatten.mkString.replaceAll(" ", "")

  val asLabel: String = inline show this

}

object Name {
  implicit val format: OFormat[Name] = (
    (__ \ "forename").formatNullable[String] and
      (__ \ "other_forenames").formatNullable[String] and
      (__ \ "surname").format[String] and
      (__ \ "title").formatNullable[String]
    ) (Name.apply, unlift(Name.unapply))

  val normalizeNameReads = (
    (__ \ "forename").readNullable[String](Formatters.normalizeReads) and
      (__ \ "other_forenames").readNullable[String](Formatters.normalizeReads) and
      (__ \ "surname").read[String](Formatters.normalizeReads) and
      (__ \ "title").readNullable[String](Formatters.normalizeReads)
    ) (Name.apply _)


  private def normalisedSeq(name: Name): Seq[String] = {
    import cats.instances.option._
    import cats.syntax.applicative._

    Seq[Option[String]](
      name.title,
      name.forename,
      name.otherForenames,
      name.surname.pure
    ) flatMap (_ map WordUtils.capitalizeFully)

  }

  object htmlShow {
    implicit val html: Show[Name] = show((name: Name) => normalisedSeq(name).mkString("<br/>"))
  }

  object inlineShow {
    implicit val inline = show((name: Name) => normalisedSeq(name).mkString(" "))
  }

}

case class Officer(
                    name: Name,
                    role: String,
                    resignedOn: Option[DateTime] = None,
                    appointmentLink: Option[String] = None // custom read to pick up (if required - TBC)
                  ) {

  override def equals(obj: Any): Boolean = obj match {
    case Officer(nameObj, roleObj, _, _)
      if role.equalsIgnoreCase(roleObj) && (nameObj == name) => true
    case _ => false
  }

  override def hashCode: Int = 1 // bit of a hack, but works
}

object Officer {

  implicit val rd: Reads[Officer] = (
    (__ \ "name_elements").read[Name](Name.normalizeNameReads) and
      (__ \ "officer_role").read[String] and
      (__ \ "resigned_on").readNullable[DateTime] and
      (__ \ "appointment_link").readNullable[String]
    ) (Officer.apply _)

  implicit val wt: Writes[Officer] = (
    (__ \ "name_elements").write[Name] and
      (__ \ "officer_role").write[String] and
      (__ \ "resigned_on").writeNullable[DateTime] and
      (__ \ "appointment_link").writeNullable[String]
    ) (unlift(Officer.unapply))
}

case class OfficerList(items: Seq[Officer])

object OfficerList {
  implicit val reads: Reads[OfficerList] = (__ \ "officers").read[Seq[Officer]] map OfficerList.apply
}
