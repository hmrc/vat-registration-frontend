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

package features.businessContact.forms

import features.businessContact.models.CompanyContactDetails
import forms.FormValidation
import forms.FormValidation._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import uk.gov.hmrc.play.mappers.StopOnFirstFail

object CompanyContactDetailsForm {

  val EMAIL_MAX_LENGTH      = 70
  private val FORM_NAME     = "businessContactDetails"
  private val EMAIL         = "email"
  private val DAYTIME_PHONE = "daytimePhone"
  private val MOBILE        = "mobile"
  private val WEBSITE       = "website"

  private def validationError(field: String) = ValidationError(s"validation.businessContactDetails.$field.missing", field)

  implicit val errorCode: ErrorCode = s"$FORM_NAME.$EMAIL"

  val form = Form(
    mapping(
      EMAIL         -> textMapping().verifying(StopOnFirstFail(mandatoryText(),FormValidation.IsEmail,maxLenText(EMAIL_MAX_LENGTH))),
      DAYTIME_PHONE -> optional(text.transform(removeSpaces,identity[String]).verifying(isValidPhoneNumber(FORM_NAME))),
      MOBILE        -> optional(text.transform(removeSpaces,identity[String]).verifying(isValidPhoneNumber(FORM_NAME))),
      WEBSITE       -> optional(text)
    )(CompanyContactDetails.apply)(CompanyContactDetails.unapply).verifying(atLeastOnePhoneNumber)
  )

  def transformErrors(form: Form[CompanyContactDetails]): Form[CompanyContactDetails] = {
    if (form.hasErrors && form.data.filterKeys(_ != "csrfToken").forall(_._2 == "")) {
      form.discardingErrors.withGlobalError("validation.businessContactDetails.missing", "businessContactDetails")
    } else {
      form
    }
  }

  private def atLeastOnePhoneNumber: Constraint[CompanyContactDetails] = Constraint {
    case CompanyContactDetails(_, None, None, _) => Invalid(validationError("atLeastOneNumber"))
    case _                                       => Valid
  }
}
