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

package forms.constraints

import forms.constraints.utils.ValidationHelper.{validate, validateNot}
import play.api.data.validation.Constraint

object TelephoneNumberConstraints {

  private val telephoneNumberRegex = """^[A-Z0-9 )/(*#+-]+$"""

  private val telephoneNumberMaxLength = 24

  def telephoneNumberFormat: Constraint[String] = Constraint("telephone_number.incorrect_format")(
    telephoneNumber => validateNot(
      constraint = telephoneNumber matches telephoneNumberRegex,
      errMsg = "capture-telephone-number.error.incorrect_format"
    )
  )

  def telephoneNumberEmpty: Constraint[String] = Constraint("email_address.nothing_entered")(
    telephoneNumber => validate(
      constraint = telephoneNumber.isEmpty,
      errMsg = "capture-telephone-number.error.nothing_entered"
    )
  )

  def telephoneNumberLength: Constraint[String] = Constraint("email_address.incorrect_length")(
    telephoneNumber => validate(
      constraint = telephoneNumber.trim.length > telephoneNumberMaxLength,
      errMsg = "capture-telephone-number.error.incorrect_length"
    )
  )

}
