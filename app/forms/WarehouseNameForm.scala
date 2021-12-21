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

import forms.WarehouseNameConstraints._
import forms.constraints.utils.ConstraintUtil.ConstraintUtil
import forms.constraints.utils.ValidationHelper.{validate, validateNot}
import play.api.data.Form
import play.api.data.Forms.{single, text}
import play.api.data.validation.Constraint

object WarehouseNameForm {

  val value = "warehouseName"

  val form: Form[String] = Form(
    single(value -> text.verifying(
      warehouseNameMissing andThen
        warehouseNameFormat
    )
    )
  )
}

object WarehouseNameConstraints {

  val missingErrorMessage = "validation.warehouseName.missing"
  val formatErrorMessage = "validation.warehouseName.format"

  val regex = "^[A-Za-z0-9 -,.&‘’/()!]{1,250}$"

  val warehouseNameMissing: Constraint[String] = Constraint(
    warehouseName => validate(
      constraint = warehouseName.isEmpty,
      errMsg = missingErrorMessage
    )
  )

  val warehouseNameFormat: Constraint[String] = Constraint(
    warehouseName => validateNot(
      constraint = warehouseName matches regex,
      errMsg = formatErrorMessage
    )
  )
}