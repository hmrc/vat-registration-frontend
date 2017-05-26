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

import java.time.LocalDate

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

import models.StringToNumberReaders._
case class DateOfBirth(day: Int, month: Int, year: Int)

object DateOfBirth {
 // implicit val format: OFormat[DateOfBirth] = Json.format[DateOfBirth]

  implicit def toLocalDate(dob: DateOfBirth): LocalDate = LocalDate.of(dob.year, dob.month, dob.day)

  // TODO remove once no longer required
  val empty = DateOfBirth(1,1,1980)

  implicit val formater = (
    (__ \ "day").format((__).readStringifiedInt) and
      (__ \ "month").format((__).readStringifiedInt) and
      (__ \ "year").format((__).readStringifiedInt)
    )(DateOfBirth.apply _, unlift(DateOfBirth.unapply _))


}

