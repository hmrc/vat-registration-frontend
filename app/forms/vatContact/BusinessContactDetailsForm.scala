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
import org.apache.commons.lang3.StringUtils
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.voa.play.form.ConditionalMappings._
import uk.gov.voa.play.form._

object BusinessContactDetailsForm {

  val EMAIL_PATTERN = """[A-Za-z0-9\-_.]{1,70}@[A-Za-z0-9\-_.]{1,70}""".r
  val PHONE_NUMBER_PATTERN = """[\d]{1,20}""".r

  implicit val e: ErrorCode = "businessContactDetails"

  def blankStringCondition(field: String): Condition = !_.get(field).exists(StringUtils.isNotBlank)

  private val EMAIL = "email"
  private val DAYTIME_PHONE = "daytimePhone"
  private val MOBILE = "mobile"
  private val WEBSITE = "website"

  val form = Form(
    mapping(
      EMAIL -> textMapping(EMAIL)
        .verifying(regexPattern(EMAIL_PATTERN)(s"$e.email")),
      DAYTIME_PHONE -> mandatoryIf(
        blankStringCondition(MOBILE),
        textMapping(DAYTIME_PHONE)
          .verifying(nonEmptyValidText(PHONE_NUMBER_PATTERN)(s"$e.daytimePhone"))),
      MOBILE -> mandatoryIf(
        blankStringCondition(DAYTIME_PHONE),
        textMapping(MOBILE)
          .verifying(nonEmptyValidText(PHONE_NUMBER_PATTERN)(s"$e.mobile"))),
      WEBSITE -> optional(text)
    )(BusinessContactDetails.apply)(BusinessContactDetails.unapply)
  )

}
