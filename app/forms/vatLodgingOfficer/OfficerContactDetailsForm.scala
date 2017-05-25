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

package forms.vatLodgingOfficer

import forms.FormValidation._
import models.view.vatLodgingOfficer.OfficerContactDetails
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

object OfficerContactDetailsForm {

  val EMAIL_PATTERN = """^([A-Z|a-z|0-9|._-]+)@([A-Z|a-z|0-9|._-]+)$""".r
  val PHONE_NUMBER_PATTERN = """[\d]{1,20}""".r

  private val FORM_NAME = "officerContactDetails"

  private val EMAIL = "email"
  private val DAYTIME_PHONE = "daytimePhone"
  private val MOBILE = "mobile"

  private def validationError(field: String) =
    ValidationError(s"validation.officerContact.missing", field)

  val form = Form(
    mapping(
      EMAIL -> optional(text.verifying(regexPattern(EMAIL_PATTERN)(s"$FORM_NAME.$EMAIL"))),
      DAYTIME_PHONE -> optional(text.verifying(regexPattern(PHONE_NUMBER_PATTERN)(s"$FORM_NAME.$DAYTIME_PHONE"))),
      MOBILE -> optional(text.verifying(regexPattern(PHONE_NUMBER_PATTERN)(s"$FORM_NAME.$MOBILE")))
    )(OfficerContactDetails.apply)(OfficerContactDetails.unapply).verifying(atLeastOneContactDetail)
  )

  def atLeastOneContactDetail: Constraint[OfficerContactDetails] = Constraint {
    case OfficerContactDetails(None, None, None) =>
      Invalid(Seq(EMAIL,MOBILE,DAYTIME_PHONE).map(validationError))
    case _ => Valid
  }

}
