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
import models.{ApiModelTransformer, DateModel, _}
import play.api.libs.json.Json

case class OfficerSecurityQuestionsView(dob: LocalDate, nino: String, officerName: Option[Name] = None)

object OfficerSecurityQuestionsView {

  def bind(dateModel: DateModel, nino: String): OfficerSecurityQuestionsView =
    OfficerSecurityQuestionsView(dateModel.toLocalDate.get, nino) // form ensures valid date

  def unbind(dobView: OfficerSecurityQuestionsView): Option[(DateModel, String)] =
    Some(DateModel.fromLocalDate(dobView.dob), dobView.nino) // form ensures valid date

  implicit val format = Json.format[OfficerSecurityQuestionsView]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LVatLodgingOfficer) => group.officerSecurityQuestions,
    updateF = (c: OfficerSecurityQuestionsView, g: Option[S4LVatLodgingOfficer]) =>
      g.getOrElse(S4LVatLodgingOfficer()).copy(officerSecurityQuestions = Some(c))
  )

  // return a view model from a VatScheme instance
  implicit val modelTransformer = ApiModelTransformer[OfficerSecurityQuestionsView] { vs: VatScheme =>
    vs.lodgingOfficer.map {
      lodgingOfficer => OfficerSecurityQuestionsView(lodgingOfficer.dob, lodgingOfficer.nino, Some(lodgingOfficer.name))
    }
  }

}
