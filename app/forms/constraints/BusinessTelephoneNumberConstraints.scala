/*
 * Copyright 2024 HM Revenue & Customs
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

object BusinessTelephoneNumberConstraints {

  private val telephoneNumberRegex = """^[+]?[0-9 ]+$"""

  private val minLength = 8

  private val maxLength = 15

  def telephoneNumberFormat: Constraint[String] = Constraint("telephone_number.incorrect_format")(
    telephoneNumber => validateNot(
      constraint = telephoneNumber matches telephoneNumberRegex,
      errMsg = "validation.business.telephoneNumber.invalid"
    )
  )

  def telephoneNumberEmpty: Constraint[String] = Constraint("telephone_number.nothing_entered")(
    telephoneNumber => validate(
      constraint = telephoneNumber.isEmpty,
      errMsg = "validation.business.telephoneNumber.missing"
    )
  )

  def telephoneNumberMinLength: Constraint[String] = Constraint("telephone_number.incorrect_min_length")(
    telephoneNumber => validate(
      constraint = ("""\d+""".r findAllIn telephoneNumber).mkString.length < minLength,
      errMsg = "validation.business.telephoneNumber.minlength"
    )
  )

  def telephoneNumberMaxLength: Constraint[String] = Constraint("telephone_number.incorrect_max_length")(
    telephoneNumber => validate(
      constraint = ("""\d+""".r findAllIn telephoneNumber).mkString.length > maxLength,
      errMsg = "validation.business.telephoneNumber.maxlength"
    )
  )
}
