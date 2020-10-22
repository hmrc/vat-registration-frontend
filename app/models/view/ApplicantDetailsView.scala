/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDate

import models.DateModel
import models.api.Address
import play.api.libs.json.{Json, OFormat}

case class FormerNameView(yesNo: Boolean, formerName: Option[String] = None)

object FormerNameView {
  implicit val format = Json.format[FormerNameView]
}

case class FormerNameDateView(date: LocalDate)

object FormerNameDateView {

  def bind(dateModel: DateModel): FormerNameDateView =
    FormerNameDateView(dateModel.toLocalDate.get) // form ensures valid date

  def unbind(formerNameDate: FormerNameDateView): Option[DateModel] =
    Some(DateModel.fromLocalDate(formerNameDate.date)) // form ensures valid date

  implicit val format = Json.format[FormerNameDateView]
}

case class HomeAddressView(addressId: String, address: Option[Address] = None)

object HomeAddressView {
  implicit val format = Json.format[HomeAddressView]
}

case class PreviousAddressView(yesNo: Boolean, address: Option[Address] = None)

object PreviousAddressView {
  implicit val format = Json.format[PreviousAddressView]
}
