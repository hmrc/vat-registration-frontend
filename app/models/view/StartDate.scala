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

package models.view

import models.api.VatScheme
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json

case class StartDate(dateType: String,
                     year: Option[String],
                     month: Option[String],
                     day: Option[String]) extends ViewModelTransformer {

  override def toString: String = {
    val d = day.getOrElse("")
    val m = month.getOrElse("")
    val y = year.getOrElse("")
    s"$y/$m/$d"
  }

  def toDate: DateTime = {
    val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
    formatter.parseDateTime(toString)
  }

  def toApi[VatChoice](vatChoice: VatChoice): VatChoice = {
    vatChoice
  }

}

object StartDate extends ApiModelTransformer {

  val COMPANY_REGISTRATION_DATE = "COMPANY_REGISTRATION_DATE"
  val BUSINESS_START_DATE = "BUSINESS_START_DATE"
  val SPECIFIC_DATE = "SPECIFIC_DATE"

  implicit val format = Json.format[StartDate]

  def empty: StartDate = StartDate("", None, None, None)

  def fromDate(d: DateTime): StartDate = {
    StartDate(SPECIFIC_DATE, Some(d.year.get().toString), Some(d.monthOfYear().get.toString), Some(d.dayOfMonth().get.toString))
  }

  def fromApi[V:StartDate](vatScheme: VatScheme): StartDate = {
    fromDate(vatScheme.vatChoice.startDate)
  }

}
