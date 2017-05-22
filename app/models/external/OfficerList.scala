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

package models.external

import models.api.Name
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import cats.Show.show
import models.api.ScrsAddress.inlineShow.inline
import models.view.vatLodgingOfficer.OfficerHomeAddressView
import models.{ApiModelTransformer => MT}
import org.apache.commons.lang3.text.WordUtils
import play.api.data.validation.ValidationError
import play.api.libs.json._

case class Officer(
                    name: Name,
                    role: String,
                    resignedOn: Option[DateTime] = None,
                    appointmentLink: Option[String] = None // custom read to pick up (if required - TBC)
                  )

object Officer {


  implicit val rd: Reads[Officer] = (
    (__ \ "name_elements").read[Name](Name.normalizeNameReads) and
      (__ \ "officer_role").read[String] and
      (__ \ "resigned_on").readNullable[DateTime] and
      (__ \ "appointment_link").readNullable[String]
    )(Officer.apply _)

  implicit val wt: Writes[Officer] = (
    (__ \ "name_elements").write[Name] and
      (__ \ "officer_role").write[String] and
      (__ \ "resigned_on").writeNullable[DateTime] and
      (__ \ "appointment_link").writeNullable[String]
    ) (unlift(Officer.unapply))

  val empty = Officer(Name.empty, "", None, None)
}

case class OfficerList(items: Seq[Officer])

object OfficerList{
    implicit val formatModel: Reads[OfficerList] = __.read[Seq[Officer]] map OfficerList.apply
}
