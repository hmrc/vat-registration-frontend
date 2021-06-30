/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.FormValidation._
import models.CompanyContactDetails
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import uk.gov.hmrc.play.mappers.StopOnFirstFail

object CompanyContactDetailsForm {

  val emailMaxLength = 70
  private val formName = "businessContactDetails"
  private val email = "email"
  private val daytimePhone = "daytimePhone"
  private val mobile = "mobile"
  private val website = "website"
  private val atLeastOneNumber = "atLeastOneNumber"

  implicit val errorCode: ErrorCode = s"$formName.$email"

  val form = Form(
    mapping(
      email         -> textMapping().verifying(StopOnFirstFail(mandatoryText(),FormValidation.IsEmail,maxLenText(emailMaxLength))),
      daytimePhone -> optional(text.transform(removeSpaces,identity[String]).verifying(isValidPhoneNumber(formName))),
      mobile        -> optional(text.transform(removeSpaces,identity[String]).verifying(isValidPhoneNumber(formName))),
      website       -> optional(text)
    )(CompanyContactDetails.apply)(CompanyContactDetails.unapply).verifying(atLeastOnePhoneNumber)
  )

  def transformErrors(form: Form[CompanyContactDetails]): Form[CompanyContactDetails] = {
    if (form.hasErrors && form.data.filterKeys(_ != "csrfToken").forall(_._2 == "")) {
      form.discardingErrors.withGlobalError("validation.businessContactDetails.missing", "email")
    } else {
      form
    }
  }

  private def atLeastOnePhoneNumber: Constraint[CompanyContactDetails] = Constraint {
    case CompanyContactDetails(_, None, None, _) => Invalid(ValidationError(s"validation.businessContactDetails.$atLeastOneNumber.missing", daytimePhone, atLeastOneNumber))
    case _                                       => Valid
  }
}
