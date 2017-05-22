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

import cats.Show
import cats.Show.show
import models.api.Name.inlineShow.inline
import org.apache.commons.lang3.text.WordUtils
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.Formatters

case class Name(
                 forename: Option[String],
                 otherForenames: Option[String],
                 surname: String,
                 title: Option[String] = None
                 ){

  import cats.instances.option._
  import cats.syntax.applicative._
  val id: String = List( forename,
    surname.pure,
    otherForenames,
    title
  ).flatten.mkString.replaceAll(" ", "")

  val asLabel: String = inline show this


}

object Name {
  implicit val format = (
    (__ \ "forename").formatNullable[String] and
      (__ \ "other_forenames").formatNullable[String] and
      (__ \ "surname").format[String] and
      (__ \ "title").formatNullable[String]
  )(Name.apply, unlift(Name.unapply))

  val normalizeNameReads = (
    (__ \ "forename").readNullable[String](Formatters.normalizeReads) and
      (__ \ "other_forenames").readNullable[String](Formatters.normalizeReads) and
      (__ \ "surname").read[String](Formatters.normalizeReads) and
      (__ \ "title").readNullable[String](Formatters.normalizeReads)
  )(Name.apply _)

  val empty = Name(None, None, "", None)


  private def normalisedSeq(name: Name): Seq[String] = {
    import cats.instances.option._
    import cats.syntax.applicative._

    Seq[Option[String]](
      name.title,
      name.forename,
      name.otherForenames,
      name.surname.pure
    ).collect {
      case Some(name) => WordUtils.capitalizeFully(name)
    }

  }

  object htmlShow {
    implicit val html: Show[Name] = show((name: Name) => normalisedSeq(name).mkString("<br/>"))
  }

  object inlineShow {
    implicit val inline = show((name: Name) => normalisedSeq(name).mkString(" "))
  }


}
