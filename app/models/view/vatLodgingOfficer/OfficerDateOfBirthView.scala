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

import models.api.{DateOfBirth, VatLodgingOfficer, VatScheme, _}
import models.{ApiModelTransformer, DateModel, ViewModelTransformer, _}
import play.api.libs.json.Json

case class OfficerDateOfBirthView(dob: LocalDate, officerName: Option[Name] = None)

object OfficerDateOfBirthView {

  def bind(dateModel: DateModel): OfficerDateOfBirthView = OfficerDateOfBirthView(dateModel.toLocalDate.get) // form ensures valid date

  def unbind(dobView: OfficerDateOfBirthView): Option[DateModel] = Some(DateModel.fromLocalDate(dobView.dob)) // form ensures valid date

  implicit val format = Json.format[OfficerDateOfBirthView]

  implicit val vmReads = ViewModelFormat(
    readF = (group: S4LVatLodgingOfficer) => group.officerDateOfBirth,
    updateF = (c: OfficerDateOfBirthView, g: Option[S4LVatLodgingOfficer]) =>
      g.getOrElse(S4LVatLodgingOfficer()).copy(officerDateOfBirth = Some(c))
  )

  // return a view model from a VatScheme instance
  implicit val modelTransformer = ApiModelTransformer[OfficerDateOfBirthView] { vs: VatScheme =>
    vs.lodgingOfficer.map {
      lodgingOfficer => OfficerDateOfBirthView(lodgingOfficer.dob, Some(lodgingOfficer.name))
    }
  }

  // return a new or updated VatLodgingOfficer from the CurrentAddressView instance
  implicit val viewModelTransformer = ViewModelTransformer { (c: OfficerDateOfBirthView, g: VatLodgingOfficer) =>
    g.copy(dob = DateOfBirth(c.dob.getDayOfMonth, c.dob.getMonthValue, c.dob.getYear))
  }

}
