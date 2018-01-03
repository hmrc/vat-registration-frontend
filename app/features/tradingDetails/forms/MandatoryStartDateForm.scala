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

package forms.vatTradingDetails.vatChoice

import java.time.LocalDate

import forms.FormValidation.Dates.{nonEmptyDateModel, validDateModel}
import forms.FormValidation.{ErrorCode, textMapping}
import play.api.data.Form
import play.api.data.Forms._
import models.{DateModel, MonthYearModel}
import models.view.vatTradingDetails.vatChoice.StartDateView
import play.api.Logger
import play.api.data.Forms.mapping
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

object MandatoryStartDateForm {
  val RADIO_INPUT_NAME = "startDateRadio"

  implicit object LocalDateOrdering extends Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
  }
  implicit val specificErrorCode: String = "startDateManIncorp"

  def minDateValidation(incorpDate : LocalDate): MinimumDateValidation = {
    val fouryears = LocalDate.now().minusYears(4)
    if (fouryears.isAfter(incorpDate)) FourYearsSinceIncorporatedDate(fouryears) else StandardIncorporatedDate(incorpDate)
  }

  def inRangeCustom(incorpDate : LocalDate, maxValue : LocalDate)(implicit ordering: Ordering[LocalDate], e: ErrorCode): Constraint[LocalDate] =
    Constraint[LocalDate] { (t: LocalDate) =>
      val minDate = minDateValidation(incorpDate)
      val minValue = minDate.date

      Logger.info(s"Checking constraint for value $t in the range of [$minValue, $maxValue]")
      (ordering.compare(t, minValue).signum, ordering.compare(t, maxValue).signum) match {
        case (1, -1) | (0, _) | (_, 0) => Valid
        case (_, 1) => Invalid(ValidationError(s"validation.$e.range.above", maxValue.format(MonthYearModel.FORMAT_D_MMMM_Y)))
        case (-1, _) => Invalid(minDate match {
            case FourYearsSinceIncorporatedDate(_) => ValidationError(s"validation.$e.range.below4y", minValue)
            case StandardIncorporatedDate(_) => ValidationError(s"validation.$e.range.belowIncorp", minValue)
          }
        )
      }
    }

  def form(incorpDate : LocalDate, calculatedDate : LocalDate) = {
    implicit val injectedDate = Some(calculatedDate)

    Form(
      mapping(
        RADIO_INPUT_NAME -> textMapping()("startDate.choice").verifying(StartDateView.validSelection),
        "startDate" -> mandatoryIf(
          isEqual(RADIO_INPUT_NAME, StartDateView.SPECIFIC_DATE),
          mapping(
            "day" -> text,
            "month" -> text,
            "year" -> text
          )(DateModel.apply)(DateModel.unapply).verifying(
            nonEmptyDateModel(validDateModel(inRangeCustom(incorpDate, calculatedDate))))
        )
      )(StartDateView.bind)(StartDateView.unbind)
    )
  }
}
