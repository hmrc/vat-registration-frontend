/*
 * Copyright 2018 HM Revenue & Customs
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

package features.officer.forms

import java.time.LocalDate

import features.officer.models.view._
import forms.FormValidation
import forms.FormValidation.Dates.{nonEmptyDateModel, validDateModel}
import forms.FormValidation.{ErrorCode, inRange, maxLenText, missingBooleanFieldMapping, nonEmptyValidText, textMapping, _}
import models.DateModel
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

object SecurityQuestionsForm {
  //val NINO_REGEX = """^[[A-Z]&&[^DFIQUV]][[A-Z]&&[^DFIQUVO]] ?\d{2} ?\d{2} ?\d{2} ?[A-D]{1}$""".r

  implicit object LocalDateOrdering extends Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
  }

  val minDate: LocalDate = LocalDate.of(1900, 1, 1)
  val maxDate: LocalDate = LocalDate.now()

  val form = Form(
    mapping(
      "dob" -> {
        implicit val errorCodeDob: ErrorCode = "security.questions.dob"
        mapping(
          "day" -> text,
          "month" -> text,
          "year" -> text
        )(DateModel.apply)(DateModel.unapply).verifying(nonEmptyDateModel(validDateModel(inRange(minDate, maxDate))))
      }
//      ,
//      "nino" -> {
//        implicit val errorCodeNino: ErrorCode = "security.questions.nino"
//        text.verifying(nonEmptyValidText(NINO_REGEX))
//      }
    )(SecurityQuestionsView.bind)(SecurityQuestionsView.unbind)
  )
}

object FormerNameForm {
  val FORMER_NAME_MAX_LENGTH = 70
  val RADIO_YES_NO: String = "formerNameRadio"
  val INPUT_FORMER_NAME: String = "formerName"

  val FORMER_NAME_REGEX = """^[A-Za-z0-9.,\-()/!"%&*;'<>][A-Za-z0-9 .,\-()/!"%&*;'<>]*$""".r

  implicit val errorCode: ErrorCode = INPUT_FORMER_NAME

  def isNotMatchingCompletionCapacityId(matcher: String, errorMsg: String): Constraint[String] = Constraint[String] {
    input: String =>
      if (matcher.toLowerCase != input.replaceAll(" ", "").toLowerCase) Valid else Invalid(errorMsg)
  }

  def form(completionCapacityId: String) = Form(
    mapping(
      RADIO_YES_NO -> missingBooleanFieldMapping()("formerName.choice"),
      INPUT_FORMER_NAME -> mandatoryIf(
        isEqual(RADIO_YES_NO, "true"),
        text.verifying(StopOnFirstFail(
          nonEmptyValidText(FORMER_NAME_REGEX),
          maxLenText(FORMER_NAME_MAX_LENGTH),
          isNotMatchingCompletionCapacityId(completionCapacityId, "validation.formerName.match.cc")
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

  val maxDate: LocalDate = LocalDate.now()

  def form(minDate: LocalDate) = Form(
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
  val EMAIL_MAX_LENGTH      = 70

  private val FORM_NAME = "officerContactDetails"

  private val EMAIL         = "email"
  private val DAYTIME_PHONE = "daytimePhone"
  private val MOBILE        = "mobile"
  private val PROVIDE_ONE_CONTACT = "atLeastOneContact"

  implicit val errorCode: ErrorCode = "officerContactDetails.email"

  private def validationError(field: String) = ValidationError(s"validation.officerContact.$field.missing", field)

  val form = Form(
    mapping(
      EMAIL         -> optional(text.verifying(StopOnFirstFail(FormValidation.IsEmail(s"$FORM_NAME.$EMAIL"),maxLenText(EMAIL_MAX_LENGTH)))),
      DAYTIME_PHONE -> optional(text.transform(removeSpaces,identity[String]).verifying(isValidPhoneNumber(FORM_NAME))),
      MOBILE        -> optional(text.transform(removeSpaces,identity[String]).verifying(isValidPhoneNumber(FORM_NAME)))
    )(ContactDetailsView.apply)(ContactDetailsView.unapply).verifying(atLeastOneContactDetail)
  )

  def atLeastOneContactDetail: Constraint[ContactDetailsView] = Constraint {
    case ContactDetailsView(None, None, None) => Invalid(validationError(PROVIDE_ONE_CONTACT))
    case _                                    => Valid
  }
}

object HomeAddressForm {
  val ADDRESS_ID: String = "homeAddressRadio"

  val form = Form(
    mapping(
      ADDRESS_ID -> textMapping()("officerHomeAddress")
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
