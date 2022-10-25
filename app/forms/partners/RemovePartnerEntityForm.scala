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

package forms.partners

import forms.FormValidation.ErrorCode
import forms.RequiredBooleanForm
import play.api.data.Form
import play.api.data.Forms.single

case class RemovePartnerEntityForm(name: Option[String]) extends RequiredBooleanForm {

  override val errorMsg = "validation.entity.removePartner.missing"
  override lazy val errorMsgArgs = List(name)
  val removePartnerKey = "value"
  implicit val errorCode: ErrorCode = "entity.removePartner"

  val form: Form[Boolean] = Form(
    single(removePartnerKey -> requiredBoolean)
  )
}
