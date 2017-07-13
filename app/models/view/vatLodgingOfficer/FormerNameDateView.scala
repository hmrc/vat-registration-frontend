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
import models.api._
import play.api.libs.json.Json

import scala.util.Try

case class FormerNameDateView(date: LocalDate)

object FormerNameDateView {

  def bind(dateModel: DateModel): FormerNameDateView =
    FormerNameDateView(dateModel.toLocalDate.get)  // form ensures valid date

  def unbind(formerNameDate: FormerNameDateView): Option[DateModel] =
      Some(DateModel.fromLocalDate(formerNameDate.date))  // form ensures valid date

  implicit val format = Json.format[FormerNameDateView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LVatLodgingOfficer) => group.formerNameDate,
    updateF = (c: FormerNameDateView, g: Option[S4LVatLodgingOfficer]) =>
      g.getOrElse(S4LVatLodgingOfficer()).copy(formerNameDate = Some(c))
  )

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer[FormerNameDateView] { vs: VatScheme =>
    vs.lodgingOfficer.flatMap(_.changeOfName.formerName).collect {
      case FormerName(_, Some(d)) => FormerNameDateView(d)
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: FormerNameDateView, g: VatLodgingOfficer) =>
    g.copy(changeOfName =
      g.changeOfName.copy(formerName =
        g.changeOfName.formerName.map(formerName =>
          formerName.copy(dateOfNameChange = Some(c.date)))))
  }

}
