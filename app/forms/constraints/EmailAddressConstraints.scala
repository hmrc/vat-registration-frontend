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

package forms.constraints

import forms.constraints.utils.ValidationHelper.{validate, validateNot}
import play.api.data.validation.Constraint

object EmailAddressConstraints {

  // http://emailregex.com/
  // n.b. this regex rejects upper case characters
  private val emailRegex =
  """^(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\
    |x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[
    |a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4]
    |[0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\
    |x09\x0b\x0c\x0e-\x7f])+)\])$""".stripMargin

  private val emailMaxLength = 132

  def emailAddressFormat: Constraint[String] = Constraint("email_address.incorrect_format")(
    emailAddress => validateNot(
      constraint = emailAddress matches emailRegex,
      errMsg = "capture-email-address.error.incorrect_format"
    )
  )

  def emailAddressLength: Constraint[String] = Constraint("email_address.incorrect_format")(
    emailAddress => validate(
      constraint = emailAddress.isEmpty || emailAddress.trim.length > emailMaxLength,
      errMsg = "capture-email-address.error.incorrect_length"
    )
  )

}
