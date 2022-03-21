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

import forms.FormValidation.{mandatory, maxLenText, nonEmptyValidText}
import models.external.Name
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import uk.gov.hmrc.play.mappers.StopOnFirstFail

object FormerNameCaptureForm {
  val maxLength = 35
  val firstNameMissing = "validation.formerNameCapture.first.missing"
  val lastNameMissing = "validation.formerNameCapture.last.missing"
  val regex = """^[A-Za-z0-9 ,.&'\/-]*$""".r

  def form: Form[Name] = Form[Name](
    mapping(
      "formerFirstName" -> text.verifying(
        StopOnFirstFail(
          mandatory(firstNameMissing),
          nonEmptyValidText(regex)("formerNameCapture.first"),
          maxLenText(maxLength)("formerNameCapture.first")
        )
      ),
      "formerLastName" -> text.verifying(
        StopOnFirstFail(
          mandatory(lastNameMissing),
          nonEmptyValidText(regex)("formerNameCapture.last"),
          maxLenText(maxLength)("formerNameCapture.last")
        )
      )
    )((first, last) => Name(Some(first), None, last, None))(name => Name.unapply(name).map {
      case (Some(first), _, last, _) => (first, last)
      case (None, _, last, _) => ("", last)
    }))
}