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

package forms

import java.time.LocalDate

import models.view._
import forms.FormValidation.Dates.{nonEmptyDateModel, validDateModel}
import forms.FormValidation.{ErrorCode, inRange, maxLenText, missingBooleanFieldMapping, nonEmptyValidText, textMapping, _}
import models.DateModel
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

object FormerNameForm {
  val FORMER_NAME_MAX_LENGTH = 70
  val RADIO_YES_NO: String = "formerNameRadio"
  val INPUT_FORMER_NAME: String = "formerName"

  val FORMER_NAME_REGEX = """^[A-Za-z0-9.,\-()/!"%&*;'<>][A-Za-z0-9 .,\-()/!"%&*;'<>]*$""".r

  implicit val errorCode: ErrorCode = INPUT_FORMER_NAME

  def form = Form(
    mapping(
      RADIO_YES_NO -> missingBooleanFieldMapping()("formerName.choice"),
      INPUT_FORMER_NAME -> mandatoryIf(
        isEqual(RADIO_YES_NO, "true"),
        text.verifying(StopOnFirstFail(
          nonEmptyValidText(FORMER_NAME_REGEX),
          maxLenText(FORMER_NAME_MAX_LENGTH)
        ))
      )
    )(FormerNameView.apply)(FormerNameView.unapply)
  )
}

object FormerNameDateForm {
  implicit val errorCode: ErrorCode = "formerNameDate"

  implicit object LocalDateOrdering extends Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
  }

  val minDate: LocalDate = LocalDate.of(2000, 1, 1)
  val maxDate: LocalDate = LocalDate.now().plusDays(1)

  val form = Form(
    mapping(
      "formerNameDate" -> mapping(
        "day" -> text,
        "month" -> text,
        "year" -> text
      )(DateModel.apply)(DateModel.unapply).verifying(nonEmptyDateModel(validDateModel(inRange(minDate, maxDate))))
    )(FormerNameDateView.bind)(FormerNameDateView.unbind)
  )
}

object ContactDetailsForm {
  val EMAIL_MAX_LENGTH = 70

  private val FORM_NAME = "applicantContactDetails"

  private val EMAIL = "email"
  private val DAYTIME_PHONE = "daytimePhone"
  private val MOBILE = "mobile"
  private val PROVIDE_ONE_CONTACT = "atLeastOneContact"

  implicit val errorCode: ErrorCode = "applicantContactDetails.email"

  private def validationError(field: String) = ValidationError(s"validation.applicantContact.$field.missing", field)

  val form = Form(
    mapping(
      DAYTIME_PHONE -> optional(text.transform(removeSpaces, identity[String]).verifying(isValidPhoneNumber(FORM_NAME))),
      EMAIL -> optional(text.verifying(StopOnFirstFail(FormValidation.IsEmail(s"$FORM_NAME.$EMAIL"), maxLenText(EMAIL_MAX_LENGTH)))),
      MOBILE -> optional(text.transform(removeSpaces, identity[String]).verifying(isValidPhoneNumber(FORM_NAME)))
    )(ContactDetailsView.apply)(ContactDetailsView.unapply).verifying(atLeastOneContactDetail)
  )

  def atLeastOneContactDetail: Constraint[ContactDetailsView] = Constraint {
    case ContactDetailsView(None, None, None) => Invalid(validationError(PROVIDE_ONE_CONTACT))
    case _ => Valid
  }
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