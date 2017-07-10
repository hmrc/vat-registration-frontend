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

package models.view.vatLodgingOfficer

import java.time.LocalDate

import models._
import models.api.{VatFlatRateScheme, VatScheme, VatStartDate, VatTradingDetails}
import play.api.libs.json.Json

import scala.util.Try

case class FormerNameDateView(dateType: String = "", date: Option[LocalDate] = None)

object FormerNameDateView {

  def bind(dateType: String, dateModel: Option[DateModel]): FormerNameDateView =
    FormerNameDateView(dateType, dateModel.flatMap(_.toLocalDate))

  def unbind(formerNameDate: FormerNameDateView): Option[(String, Option[DateModel])] =
    Try {
      formerNameDate.date.fold((formerNameDate.dateType, Option.empty[DateModel])) {
        d => (formerNameDate.dateType, Some(DateModel.fromLocalDate(d)))
      }
    }.toOption

  implicit val format = Json.format[FormerNameDateView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LFlatRateScheme) => group.formerNameDate,
    updateF = (c: FormerNameDateView, g: Option[S4LFlatRateScheme]) =>
      g.getOrElse(S4LFlatRateScheme()).copy(formerNameDate = Some(c))
  )

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer[FormerNameDateView] { vs: VatScheme =>
    vs.vatFlatRateScheme.collect{
      case VatFlatRateScheme(_, _, _, _, Some(dateType), d@_) => FormerNameDateView(dateType, d)
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: FormerNameDateView, g: VatFlatRateScheme) =>
    g.copy(whenDoYouWantToJoinFrs = Some(c.dateType), startDate = c.date)
  }

}
