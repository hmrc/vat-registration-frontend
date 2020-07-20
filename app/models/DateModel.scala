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

package models

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, ResolverStyle}

import scala.util.Try


case class DateModel(day: String, month: String, year: String) {

  def toLocalDate: Option[LocalDate] = Try {
    LocalDate.parse(s"$day-$month-$year", DateModel.formatter)
  }.toOption

}

object DateModel {

  //uuuu for year as we're using STRICT ResolverStyle
  val formatter = DateTimeFormatter.ofPattern("d-M-uuuu").withResolverStyle(ResolverStyle.STRICT)

  def fromLocalDate(localDate: LocalDate): DateModel =
    DateModel(localDate.getDayOfMonth.toString, localDate.getMonthValue.toString, localDate.getYear.toString)

}
