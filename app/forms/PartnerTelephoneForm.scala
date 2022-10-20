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

import forms.FormValidation.{ErrorCode, removeSpaces}
import forms.constraints.TelephoneNumberConstraints
import forms.constraints.utils.ConstraintUtil.ConstraintUtil
import play.api.data.Form
import play.api.data.Forms.{single, text}

object PartnerTelephoneForm {

  val partnerTelephoneKey = "partnerTelephone"
  implicit val errorCode: ErrorCode = "validation.partner.telephoneNumber"

  val form =
    Form(
      single(
        partnerTelephoneKey -> text.transform(removeSpaces, identity[String]).verifying(
          TelephoneNumberConstraints.telephoneNumberEmpty andThen
            TelephoneNumberConstraints.telephoneNumberFormat andThen
            TelephoneNumberConstraints.telephoneNumberMinLength andThen
            TelephoneNumberConstraints.telephoneNumberMaxLength
        )
      )
    )
}
