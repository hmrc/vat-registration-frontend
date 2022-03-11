/*
 * Copyright 2022 HM Revenue & Customs
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

package forms

import forms.FormValidation.Dates.{nonEmptyDateModel, validDateModel}
import forms.FormValidation.{ErrorCode, inRange, missingBooleanFieldMapping, textMapping}
import models.DateModel
import models.view._
import play.api.data.Form
import play.api.data.Forms.{mapping, text}

import java.time.LocalDate

object FormerNameDateForm {
  implicit val errorCode: ErrorCode = "formerNameDate"

  implicit object LocalDateOrdering extends Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
  }

  val maxDate: LocalDate = LocalDate.now().plusDays(1)

  def form(dob: LocalDate): Form[FormerNameDateView] = Form(
    mapping(
      "formerNameDate" -> mapping(
        "day" -> text,
        "month" -> text,
        "year" -> text
      )(DateModel.apply)(DateModel.unapply).verifying(nonEmptyDateModel(validDateModel(inRange(dob, maxDate))))
    )(FormerNameDateView.bind)(FormerNameDateView.unbind)
  )
}

object HomeAddressForm {
  val ADDRESS_ID: String = "homeAddressRadio"

  val form = Form(
    mapping(
      ADDRESS_ID -> textMapping()("applicantHomeAddress")
    )(HomeAddressView(_))(view => Option(view.addressId))
  )
}

object PreviousAddressForm {
  val RADIO_YES_NO: String = "previousAddressQuestionRadio"

  val form = Form(
    mapping(
      RADIO_YES_NO -> missingBooleanFieldMapping()("previousAddressQuestion")
    )(PreviousAddressView.apply(_))(view => Option(view.yesNo))
  )
}