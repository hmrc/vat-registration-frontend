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

import forms.constraints.utils.ValidationHelper.validate
import play.api.data.validation.Constraint

object EmailPasscodeConstraints {

  private val passcodeMaxLength = 6

  def emailPasscodeLength: Constraint[String] = Constraint("email_passcode.incorrect_length")(
    emailPasscode => validate(
      constraint = emailPasscode.isEmpty || emailPasscode.trim.length > passcodeMaxLength,
      errMsg = "capture-email-passcode.error.incorrect_length"
    )
  )

}
