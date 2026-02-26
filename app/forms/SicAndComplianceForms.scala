/*
 * Copyright 2026 HM Revenue & Customs
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

import forms.FormValidation.{maxLenText, regexPattern, removeNewlineAndTrim, stopOnFail, textMapping}
import play.api.data.Form
import play.api.data.Forms.{single, text}

object BusinessActivityDescriptionForm {
  val DESC_MAX_LENGTH = 250
  val INPUT_DESCRIPTION: String = "description"
  val PartPattern = """^[A-Za-z0-9 \-,.&'/()!]+$""".r

  val form = Form(
    single(
      INPUT_DESCRIPTION -> text.transform(removeNewlineAndTrim, identity[String]).verifying(stopOnFail(
        regexPattern(PartPattern)("businessActivity.description"),
        maxLenText(DESC_MAX_LENGTH)("businessActivity.description")
      ))
    )
  )
}

object MainBusinessActivityForm {
  val NAME_ID: String = "value"

  val form = Form(
    single(
      NAME_ID -> textMapping()("mainBusinessActivity")
    )
  )
}


