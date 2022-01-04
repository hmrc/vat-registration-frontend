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

import forms.WarehouseNumberConstraints._
import forms.constraints.utils.ConstraintUtil.ConstraintUtil
import forms.constraints.utils.ValidationHelper.{validate, validateNot}
import play.api.data.Form
import play.api.data.Forms.{single, text}
import play.api.data.validation.Constraint

object WarehouseNumberForm {

  val value = "warehouseNumber"

  val form: Form[String] = Form(
    single(value -> text.verifying(
      warehouseNumberMissing andThen
        warehouseNumberRegex andThen
        warehouseNumberFormat
    )
    )
  )
}

object WarehouseNumberConstraints {

  val missingErrorMessage = "validation.warehouseNumber.missing"
  val regexErrorMessage = "validation.warehouseNumber.regex"
  val formatErrorMessage = "validation.warehouseNumber.format"

  val regex = "^[A-Za-z0-9]*$"
  val numberFormat = "^[A-Za-z]{3}[0-9]{12}$"

  val warehouseNumberMissing: Constraint[String] = Constraint(
    warehouseNumber => validate(
      constraint = warehouseNumber.isEmpty,
      errMsg = missingErrorMessage
    )
  )

  val warehouseNumberRegex: Constraint[String] = Constraint(
    warehouseNumber => validateNot(
      constraint = warehouseNumber matches regex,
      errMsg = regexErrorMessage
    )
  )

  val warehouseNumberFormat: Constraint[String] = Constraint(
    warehouseNumber => validateNot(
      constraint = warehouseNumber matches numberFormat,
      errMsg = formatErrorMessage
    )
  )
}