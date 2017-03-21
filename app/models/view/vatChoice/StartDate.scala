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

package models.view.vatChoice

import java.time.LocalDate

import models.api.{VatChoice, VatScheme}
import models.{ApiModelTransformer, DateModel, ViewModelTransformer}
import play.api.libs.json.Json

case class StartDate(dateType: String = "", date: Option[LocalDate] = None)

object StartDate {

  def fromDateModel(dateType: String, dateModel: Option[DateModel]): StartDate =
    StartDate(dateType, dateModel.flatMap(_.toLocalDate))

  def toDateModel(startDate: StartDate): Option[(String, Option[DateModel])] =
    startDate.date.map(d => (startDate.dateType, Some(DateModel.fromLocalDate(d))))

  val DEFAULT_DATE: LocalDate = LocalDate.of(1970, 1, 1)

  val COMPANY_REGISTRATION_DATE = "COMPANY_REGISTRATION_DATE"
  val BUSINESS_START_DATE = "BUSINESS_START_DATE"
  val SPECIFIC_DATE = "SPECIFIC_DATE"

  val validSelection: String => Boolean = Seq(COMPANY_REGISTRATION_DATE, BUSINESS_START_DATE, SPECIFIC_DATE).contains

  implicit val format = Json.format[StartDate]

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer[StartDate] { vs: VatScheme =>
    vs.vatChoice.map(vc => fromLocalDate(vc.startDate))
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: StartDate, g: VatChoice) =>
    g.copy(startDate = c.date.getOrElse(DEFAULT_DATE))
  }

  def fromLocalDate(d: LocalDate): StartDate =
  // TODO: Remove check when start date becomes optional in next story
    if (d.isEqual(DEFAULT_DATE)) {
      StartDate(StartDate.COMPANY_REGISTRATION_DATE)
    } else {
      StartDate(StartDate.SPECIFIC_DATE, Some(d))
    }

}
