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

import forms.constraints.utils.ConstraintUtil.ConstraintUtil
import forms.constraints.utils.ValidationHelper.{validate, validateNot}
import play.api.data.Form
import play.api.data.Forms.{single, text}
import play.api.data.validation.Constraint

object TransactorTelephoneForm {

  val telephoneNumberKey = "telephoneNumber"

  val form =
    Form(
      single(
        telephoneNumberKey -> text.verifying(
          TransactorTelephoneConstraints.telephoneNumberEmpty andThen
          TransactorTelephoneConstraints.telephoneNumberLength andThen
          TransactorTelephoneConstraints.telephoneNumberFormat
        )
      )
    )
}

object TransactorTelephoneConstraints {

  private val telephoneNumberRegex = """^[A-Z0-9 )/(*#+-]+$"""

  private val telephoneNumberMaxLength = 24

  def telephoneNumberFormat: Constraint[String] = Constraint("telephoneNumber.incorrectFormat")(
    telephoneNumber => validateNot(
      constraint = telephoneNumber matches telephoneNumberRegex,
      errMsg = "telephoneNumber.error.incorrectFormat"
    )
  )

  def telephoneNumberEmpty: Constraint[String] = Constraint("telephoneNumber.nothingEntered")(
    telephoneNumber => validate(
      constraint = telephoneNumber.isEmpty,
      errMsg = "telephoneNumber.error.nothingEntered"
    )
  )

  def telephoneNumberLength: Constraint[String] = Constraint("telephoneNumber.incorrectLength")(
    telephoneNumber => validate(
      constraint = telephoneNumber.trim.length > telephoneNumberMaxLength,
      errMsg = "telephoneNumber.error.incorrectLength"
    )
  )
}
