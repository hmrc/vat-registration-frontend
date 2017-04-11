/*
 * Copyright 2017 HM Revenue & Customs
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

package forms.vatContact

import forms.FormValidation._
import models.view.vatContact.BusinessContactDetails
import play.api.data.Form
import play.api.data.Forms._

object BusinessContactDetailsForm {

  val EMAIL_PATTERN = """[A-Za-z0-9\-_.]{1,70}@[A-Za-z0-9\-_.]{1,70}""".r
  val PHONE_NUMBER_PATTERN = """[\d]{1,20}""".r

  implicit val errorCode: ErrorCode = "vatContact.businessContactDetails"

  val form = Form(
    mapping(
      "email" -> text.verifying(regexPattern(EMAIL_PATTERN)),
      "daytimePhone" -> optional(text.verifying(regexPattern(PHONE_NUMBER_PATTERN, mandatory = false))),
      "mobile" -> optional(text.verifying(regexPattern(PHONE_NUMBER_PATTERN, mandatory = false))),
      "website" -> optional(text)
    )(BusinessContactDetails.apply)(BusinessContactDetails.unapply)
  )

}
