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

import models.api.{VatChoice, VatScheme}
import models.{ApiModelTransformer, ViewModelTransformer}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json

case class StartDate(dateType: String = "",
                     day: Option[Int] = None,
                     month: Option[Int] = None,
                     year: Option[Int] = None) {

  override def toString: String = {
    val d = day.getOrElse(1)
    val m = month.getOrElse(1)
    val y = year.getOrElse(1970)
    s"$d/$m/$y"
  }

  def toDateTime: DateTime = StartDate.pattern.parseDateTime(toString)

}

object StartDate {

  def default: StartDate = StartDate(COMPANY_REGISTRATION_DATE)

  val pattern = DateTimeFormat.forPattern("dd/MM/yyyy")

  val COMPANY_REGISTRATION_DATE = "COMPANY_REGISTRATION_DATE"
  val BUSINESS_START_DATE = "BUSINESS_START_DATE"
  val SPECIFIC_DATE = "SPECIFIC_DATE"

  implicit val format = Json.format[StartDate]

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer { vs: VatScheme =>
    vs.vatChoice match {
      case Some(vc) => Some(fromDateTime(vc.startDate))
      case None => None
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: StartDate, g: VatChoice) =>
    g.copy(startDate = c.toDateTime)
  }

  def fromDateTime(d: DateTime): StartDate =
  // TODO: Remove check when start date becomes optional in next story
    if (d.toString("dd/MM/yyyy") == "31/12/1969" || d.toString("dd/MM/yyyy") == "01/01/1970") {
      StartDate.default
    } else {
      StartDate(StartDate.SPECIFIC_DATE,
        Some(d.dayOfMonth.get()),
        Some(d.monthOfYear().get),
        Some(d.year().get)
      )
    }

}
