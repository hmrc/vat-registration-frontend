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

import models.{ApiModelTransformer, ViewModelTransformer}
import models.api.{VatChoice, VatScheme}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json

case class StartDate(dateType: String,
                     day: Option[Int],
                     month: Option[Int],
                     year: Option[Int])
  extends ViewModelTransformer[VatChoice] {

  override def toString: String = {
    val d = day.getOrElse(1)
    val m = month.getOrElse(1)
    val y = year.getOrElse(1970)
    s"$d/$m/$y"
  }

  // Upserts (selectively converts) a View model object to its API model counterpart
  override def toApi(vatChoice: VatChoice): VatChoice =
    vatChoice.copy(startDate = toDateTime)

  def toDateTime: DateTime =
    DateTimeFormat.forPattern("dd/MM/yyyy").parseDateTime(toString)
}

object StartDate extends ApiModelTransformer[StartDate] {

  val COMPANY_REGISTRATION_DATE = "COMPANY_REGISTRATION_DATE"
  val BUSINESS_START_DATE = "BUSINESS_START_DATE"
  val SPECIFIC_DATE = "SPECIFIC_DATE"

  implicit val format = Json.format[StartDate]

  // Returns a view model for a specific part of a given VatScheme API model
  override def apply(vatScheme: VatScheme): StartDate =
    fromDateTime(vatScheme.vatChoice.startDate)

  def fromDateTime(d: DateTime): StartDate =
    if (d.toString("dd/MM/yyyy") == "01/01/1970") {
      StartDate.empty(COMPANY_REGISTRATION_DATE)
    } else {
      StartDate(StartDate.SPECIFIC_DATE,
        Some(d.dayOfMonth.get()),
        Some(d.monthOfYear().get),
        Some(d.year().get)
      )
    }

  def empty: StartDate = StartDate("", None, None, None)

  def empty(dateType: String): StartDate = StartDate(dateType, None, None, None)
}