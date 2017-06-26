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

package models.view.frs

import java.time.LocalDate

import models._
import models.api.{VatScheme, VatStartDate, VatTradingDetails}
import play.api.libs.json.Json

import scala.util.Try

case class FrsStartDateView(dateType: String = "", date: Option[LocalDate] = None, ctActiveDate: Option[LocalDate] = None) {

  def withCtActiveDateOption(d: LocalDate): FrsStartDateView = this.copy(ctActiveDate = Some(d))

}

object FrsStartDateView {

  def bind(dateType: String, dateModel: Option[DateModel]): FrsStartDateView =
    FrsStartDateView(dateType, dateModel.flatMap(_.toLocalDate))

  def unbind(startDate: FrsStartDateView): Option[(String, Option[DateModel])] =
    Try {
      startDate.date.fold((startDate.dateType, Option.empty[DateModel])) {
        d => (startDate.dateType, Some(DateModel.fromLocalDate(d)))
      }
    }.toOption

  val VAT_REGISTRATION_DATE = "VAT_REGISTRATION_DATE"
  val DIFFERENT_DATE = "DIFFERENT_DATE"

  val validSelection: String => Boolean = Seq(VAT_REGISTRATION_DATE, DIFFERENT_DATE).contains

  implicit val format = Json.format[FrsStartDateView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LFlatRateSchemeAnswers) => group.frsStartDate,
    updateF = (c: FrsStartDateView, g: Option[S4LFlatRateSchemeAnswers]) =>
      g.getOrElse(S4LFlatRateSchemeAnswers()).copy(frsStartDate = Some(c))
  )

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer[FrsStartDateView] { vs: VatScheme =>
    None
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: FrsStartDateView, g: VatTradingDetails) =>
   g
  }

}
