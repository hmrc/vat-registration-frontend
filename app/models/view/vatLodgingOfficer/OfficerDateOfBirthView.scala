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

import models.api.{DateOfBirth, VatLodgingOfficer, VatScheme}
import models.{ApiModelTransformer, DateModel, ViewModelTransformer}
import play.api.libs.json.Json

case class OfficerDateOfBirthView(dob: LocalDate)

object OfficerDateOfBirthView {
  def bind(dateModel: DateModel): OfficerDateOfBirthView = OfficerDateOfBirthView(dateModel.toLocalDate.get) // TODO ???

  def unbind(dobView: OfficerDateOfBirthView): Option[DateModel] = Some(DateModel.fromLocalDate(dobView.dob)) // TODO ???

  implicit val format = Json.format[OfficerDateOfBirthView]

  // return a view model from a VatScheme instance
  implicit val modelTransformer = ApiModelTransformer[OfficerDateOfBirthView] { vs: VatScheme =>
    vs.lodgingOfficer.map(_.dob).collect {
      case Some(DateOfBirth(d,m,y)) => OfficerDateOfBirthView(LocalDate.of(y, m, d))
    }
  }

  // return a new or updated VatLodgingOfficer from the CurrentAddressView instance
  implicit val viewModelTransformer = ViewModelTransformer { (c: OfficerDateOfBirthView, g: VatLodgingOfficer) => {
      g.copy(dob = Some(DateOfBirth(c.dob.getDayOfMonth, c.dob.getMonthValue, c.dob.getYear)))
    }
  }

}
