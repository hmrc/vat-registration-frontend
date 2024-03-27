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
import play.api.data.Form
import play.api.data.Forms.{single, text, tuple}

import java.time.LocalDate

object FormerNameDateForm {
  val radioKey: String = "formerNameDate"

  val maxDate: LocalDate = LocalDate.now().plusDays(1)

  protected val dateEmptyKey = "validation.formerNameDate.missing"
  protected val dateInvalidKey = "validation.formerNameDate.invalid"
  protected val dateRangeBelow = "validation.formerNameDate.range.below"
  protected val dateRangeAbove = "validation.formerNameDate.range.above"

  def form(dateOfBirth: LocalDate): Form[LocalDate] = Form(
    single(
      radioKey -> tuple(
        "day" -> text,
        "month" -> text,
        "year" -> text
      ).verifying(stopOnFail(
        nonEmptyDate(dateEmptyKey),
        validDate(dateInvalidKey),
        withinRange(
          minDate = dateOfBirth,
          maxDate = maxDate,
          beforeMinErr = dateRangeBelow,
          afterMaxErr = dateRangeAbove,
          args = Nil
        )
      )).transform[LocalDate](
        date => LocalDate.of(date._3.toInt, date._2.toInt, date._1.toInt),
        date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
      )
    )
  )
}

object PreviousAddressForm {
  val radioKey: String = "value"

  def form(errorCode: ErrorCode = "previousAddressQuestion"): Form[Boolean] =
    Form(
      single(
        radioKey -> missingBooleanFieldMapping()(errorCode)
      )
    )
}