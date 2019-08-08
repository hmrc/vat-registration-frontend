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

package models.api

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class SicCode(code: String,
                   description: String,
                   displayDetails: String)

object SicCode {

  implicit val format: Format[SicCode] =
    ((__ \ "code").format[String] and
      (__ \ "desc").format[String] and
      (__ \ "indexes").format[String]
       ) (SicCode.apply, unlift(SicCode.unapply))

  val reads: Reads[SicCode] =
    ((__ \ "code").read[String] and
      (__ \ "desc").read[String] and
      (__ \ "indexes").read[String].orElse(Reads.pure(""))
      ) (SicCode.apply _)


  val readsList: Reads[List[SicCode]] =
    (__ \ "sicCodes").lazyRead(Reads.list[SicCode](reads))

}
