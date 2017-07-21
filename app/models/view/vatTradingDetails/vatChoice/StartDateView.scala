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

package models.view.vatTradingDetails.vatChoice

import java.time.LocalDate

import models._
import models.api.{VatScheme, VatStartDate}
import play.api.libs.json.Json

import scala.util.Try

case class StartDateView(dateType: String = "", date: Option[LocalDate] = None, ctActiveDate: Option[LocalDate] = None) {

  def withCtActiveDateOption(d: LocalDate): StartDateView = this.copy(ctActiveDate = Some(d))

}

object StartDateView {

  def bind(dateType: String, dateModel: Option[DateModel]): StartDateView =
    StartDateView(dateType, dateModel.flatMap(_.toLocalDate))

  def unbind(startDate: StartDateView): Option[(String, Option[DateModel])] =
    Try {
      startDate.date.fold((startDate.dateType, Option.empty[DateModel])) {
        d => (startDate.dateType, Some(DateModel.fromLocalDate(d)))
      }
    }.toOption

  val COMPANY_REGISTRATION_DATE = "COMPANY_REGISTRATION_DATE"
  val BUSINESS_START_DATE = "BUSINESS_START_DATE"
  val SPECIFIC_DATE = "SPECIFIC_DATE"

  val validSelection: String => Boolean = Seq(COMPANY_REGISTRATION_DATE, BUSINESS_START_DATE, SPECIFIC_DATE).contains

  implicit val format = Json.format[StartDateView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LTradingDetails) => group.startDate,
    updateF = (c: StartDateView, g: Option[S4LTradingDetails]) =>
      g.getOrElse(S4LTradingDetails()).copy(startDate = Some(c))
  )

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer[StartDateView] { vs: VatScheme =>
    vs.tradingDetails.map(_.vatChoice.vatStartDate).collect {
      case VatStartDate(dateType, d@_) => StartDateView(dateType, d)
    }
  }

}
