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
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import forms.FormValidation._
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

object MandatoryDateForm extends RequiredBooleanForm {

  //TODO: CHANGE THESE
  val mandatorySelectionEmptyKey = "validation.startDate.choice.missing"
  val mandatorySelectionInvalidKey = "validation.startDate.choice.missing"
  val dateEmptyKey = "validation.startDate.missing"
  val dateInvalidKey = "validation.startDate.invalid"
  val dateBelowIncorp = "validation.startDateManIncorp.range.belowIncorp"
  val dateAboveCalculated = "validation.startDateManIncorp.range.above"
  val dateWithinFourYears = "validation.startDateManIncorp.range.below4y"

  val MANDATORY_SELECTION: String = "startDateRadio"
  val MANDATORY_DATE: String = "startDate"

  def form(incorpDate: LocalDate, calculatedDate: LocalDate) = Form(
    tuple(
      MANDATORY_SELECTION -> text.verifying(StopOnFirstFail(
                               mandatory(mandatorySelectionEmptyKey),
                               matches(List(calculated_date, specific_date), mandatorySelectionInvalidKey)
                             )).transform(DateSelection.withName, (s:DateSelection.Value) => s.toString),
      MANDATORY_DATE      -> mandatoryIf(
                               isEqual(MANDATORY_SELECTION, specific_date),
                               tuple("day" -> text, "month" -> text, "year" -> text).verifying(StopOnFirstFail(
                                 nonEmptyDate(dateEmptyKey),
                                 validDate(dateInvalidKey),
                                 withinRange(incorpDate, calculatedDate, dateBelowIncorp, dateAboveCalculated),
                                 withinFourYearsPast(dateWithinFourYears)
                               )).transform[LocalDate](
                                 date => LocalDate.of(date._3.toInt,date._2.toInt,date._1.toInt),
                                 date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
                               )
                             )
    )
  )

}
