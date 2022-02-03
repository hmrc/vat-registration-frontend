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

import forms.FormValidation._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints.maxLength
import uk.gov.hmrc.play.mappers.StopOnFirstFail

class AgentNameForm {

  import AgentNameForm._

  def apply(): Form[(String, String)] = Form(
    mapping(
      firstNameField -> {
        implicit val code: ErrorCode = messageKey(firstNameField)
        text.verifying(StopOnFirstFail(
          maxLength(maxNameLength, "validation.agentName.firstName.maxLen"),
          nonEmptyValidText(nameRegex)
        ))
      },
      lastNameField -> {
        implicit val code: ErrorCode = messageKey(lastNameField)
        text.verifying(StopOnFirstFail(
          maxLength(maxNameLength, "validation.agentName.lastName.maxLen"),
          nonEmptyValidText(nameRegex)
        ))
      }
    )(Tuple2.apply)(Tuple2.unapply)
  )

}

object AgentNameForm {

  val messageKey = (field: String) => s"agentName.$field"
  val firstNameField = "firstName"
  val lastNameField = "lastName"

  val nameRegex = "^[A-Za-z0-9\\s,\\-.&'\\\\/]{1,35}".r
  val maxNameLength = 35

}
