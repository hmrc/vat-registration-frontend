/*
 * Copyright 2018 HM Revenue & Customs
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

import java.time.LocalDate

import features.financials.models.DateSelection
import features.financials.models.DateSelection._
import forms.FormValidation._
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}


object VoluntaryDateFormIncorp {

  val voluntarySelectionEmptyKey = "validation.startDate.choice.missing"
  val voluntarySelectionInvalidKey = "validation.startDate.choice.missing"
  val voluntaryDateEmptyKey = "validation.startDate.missing"
  val voluntaryDateInvalidKey = "validation.startDate.invalid"

  val dateBelowIncorp = "validation.startDate.range.belowIncorp"
  val dateAboveThree = "validation.startDate.range.above"
  val dateWithinFourYears = "validation.startDate.range.below4y"

  val VOLUNTARY_SELECTION: String = "startDateRadio"
  val VOLUNTARY_DATE: String = "startDate"

  def form(incorpDate: LocalDate) = Form(
    tuple(
      VOLUNTARY_SELECTION -> text.verifying(StopOnFirstFail(
        mandatory(voluntarySelectionEmptyKey),
        matches(List(company_registration_date, business_start_date, specific_date), voluntarySelectionInvalidKey)
      )).transform(DateSelection.withName, (s:DateSelection.Value) => s.toString),
      VOLUNTARY_DATE      -> mandatoryIf(
        isEqual(VOLUNTARY_SELECTION, specific_date),
        tuple("day" -> text, "month" -> text, "year" -> text).verifying(StopOnFirstFail(
          nonEmptyDate(voluntaryDateEmptyKey),
          validDate(voluntaryDateInvalidKey),
          withinRange(incorpDate, LocalDate.now().plusMonths(3), dateBelowIncorp, dateAboveThree),
          withinFourYearsPast(dateWithinFourYears)
        )).transform[LocalDate](
          date => LocalDate.of(date._3.toInt,date._2.toInt,date._1.toInt),
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        )
      )
    )
  )
}
