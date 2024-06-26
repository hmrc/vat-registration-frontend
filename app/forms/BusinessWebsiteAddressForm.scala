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

package forms

import forms.FormValidation._
import forms.constraints.utils.ConstraintUtil.ConstraintUtil
import play.api.data.Form
import play.api.data.Forms.{single, text}

import scala.util.matching.Regex

object BusinessWebsiteAddressForm {

  val businessWebsiteAddressKey = "businessWebsiteAddress"
  implicit val errorCode: ErrorCode = businessWebsiteAddressKey
  val regex: Regex = """^(((HTTP|http)(S|s)?\:\/\/((WWW|www)\.)?)|((WWW|www)\.))?[a-zA-Z0-9\[_~\:\/?#\]@!&'()*+\-,;=% ]+\.[a-zA-Z]{2,5}(\.[a-zA-Z]{2,5})?(\:[0-9]{1,5})?(\/[a-zA-Z0-9_-]+(\/)?)*$""".r
  val maxLength = 132

  val form: Form[String] =
    Form(
      single(
        businessWebsiteAddressKey -> text.verifying(stopOnFail(
          nonEmptyValidText(regex) andThen
            maxLenText(maxLength)
        ))
      )
    )

}
