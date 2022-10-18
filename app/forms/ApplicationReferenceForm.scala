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

import forms.FormValidation.{mandatory, matchesRegex}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints.maxLength
import uk.gov.hmrc.play.mappers.StopOnFirstFail

class ApplicationReferenceForm {

  def apply(): Form[String] = Form[String](
      single(
        ApplicationReferenceForm.fieldName -> text.verifying(
          StopOnFirstFail(
            mandatory("applicationReference.error.missing"),
            maxLength(ApplicationReferenceForm.lengthLimit, "applicationReference.error.length"),
            matchesRegex(ApplicationReferenceForm.regex, "applicationReference.error.invalid")
          )
        )
      )
  )

}

object ApplicationReferenceForm {

  val fieldName: String = "value"
  val regex = """^[A-Za-z0-9\s'‘()\[\]{}<>!«»"ʺ˝ˮ?/\\ +=%#*&$€£_\-@¥.,:;]{1,100}$""".r
  val lengthLimit = 100

}
