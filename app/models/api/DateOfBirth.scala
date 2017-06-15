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

import models.StringToNumberReaders._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.language.implicitConversions

case class DateOfBirth(day: Int, month: Int, year: Int)

object DateOfBirth {

  implicit def toLocalDate(dob: DateOfBirth): LocalDate = LocalDate.of(dob.year, dob.month, dob.day)

  def apply(localDate: LocalDate): DateOfBirth = DateOfBirth(
    day = localDate.getDayOfMonth,
    month = localDate.getMonthValue,
    year = localDate.getYear
  )

  // TODO remove once no longer required
  val empty = DateOfBirth(1, 1, 1967)

  implicit val formatter = (
    (__ \ "day").format(__.readStringifiedInt) and
      (__ \ "month").format(__.readStringifiedInt) and
      (__ \ "year").format(__.readStringifiedInt)
    ) (DateOfBirth.apply, unlift(DateOfBirth.unapply))


}

