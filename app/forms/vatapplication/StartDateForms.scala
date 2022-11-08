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

package forms.vatapplication

import forms.FormValidation._
import models.DateSelection
import models.DateSelection.specific_date
import play.api.data.Forms.{tuple, _}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}
import play.api.i18n.Messages
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}
import utils.MessageDateFormat

import java.time.LocalDate

trait StartDateForm {
  protected val START_DATE_SELECTION = "value"
  protected val START_DATE = "startDate"

  protected val startDateChoiceMissing = "validation.startDate.choice.missing"

  protected val dateEmptyKey = "validation.startDate.missing"
  protected val dateInvalidKey = "validation.startDate.invalid"
  protected val dateRange = "validation.startDate.range"

  implicit def formatter: Formatter[DateSelection.Value] = new Formatter[DateSelection.Value] {
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], DateSelection.Value] = {
      Right(data.getOrElse(key, "")).right.flatMap {
        case e if e == DateSelection.company_registration_date.toString => Right(DateSelection.company_registration_date)
        case e if e == DateSelection.business_start_date.toString => Right(DateSelection.business_start_date)
        case e if e == DateSelection.specific_date.toString => Right(DateSelection.specific_date)
        case e if e == DateSelection.calculated_date.toString => Right(DateSelection.calculated_date)
        case _ => Left(Seq(FormError(key, startDateChoiceMissing, Nil)))
      }
    }

    def unbind(key: String, value: DateSelection.Value) = Map(key -> value.toString)
  }
}


object MandatoryDateForm extends StartDateForm {
  private val dateWithinFourYears = "validation.startDateManIncorp.range.below4y"
  val radioAnswer = "value"
  val startDate = "date"

  def form(incorpDate: LocalDate, calculatedDate: LocalDate)(implicit messages: Messages): Form[(DateSelection.Value, Option[LocalDate])] = Form(
    tuple(
      radioAnswer -> Forms.of[DateSelection.Value],
      startDate -> mandatoryIf(isEqual(radioAnswer, specific_date),
        tuple(
          "day" -> text,
          "month" -> text,
          "year" -> text
        ).verifying(StopOnFirstFail(
          nonEmptyDate(dateEmptyKey),
          validDate(dateInvalidKey),
          withinRange(incorpDate, calculatedDate, dateRange, dateRange, List(MessageDateFormat.format(incorpDate), MessageDateFormat.format(calculatedDate))),
          withinFourYearsPast(dateWithinFourYears)
        )).transform[LocalDate](
          { case (day, month, year) => LocalDate.of(year.toInt, month.toInt, day.toInt) },
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        )
      )
    )
  )

}

object VoluntaryDateForm extends StartDateForm {
  def form(dateRangeMin: LocalDate, dateRangeMax: LocalDate)(implicit messages: Messages): Form[(DateSelection.Value, Option[LocalDate])] = Form(
    tuple(
      START_DATE_SELECTION -> Forms.of[DateSelection.Value],
      START_DATE -> mandatoryIf(
        isEqual(START_DATE_SELECTION, specific_date),
        tuple("day" -> text, "month" -> text, "year" -> text).verifying(StopOnFirstFail(
          nonEmptyDate(dateEmptyKey),
          validDate(dateInvalidKey),
          withinRange(dateRangeMin, dateRangeMax, dateRange, dateRange, List(MessageDateFormat.format(dateRangeMin), MessageDateFormat.format(dateRangeMax)))
        )).transform[LocalDate](
          date => LocalDate.of(date._3.toInt, date._2.toInt, date._1.toInt),
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        )
      )
    )
  )
}

object VoluntaryDateFormIncorp extends StartDateForm {
  private val dateWithinFourYears = "validation.startDate.range.below4y"
  private val now3MonthsLater = LocalDate.now().plusMonths(3)

  def form(incorpDate: LocalDate)(implicit messages: Messages): Form[(DateSelection.Value, Option[LocalDate])] = Form(
    tuple(
      START_DATE_SELECTION -> Forms.of[DateSelection.Value],
      START_DATE -> mandatoryIf(
        isEqual(START_DATE_SELECTION, specific_date),
        tuple("day" -> text, "month" -> text, "year" -> text).verifying(StopOnFirstFail(
          nonEmptyDate(dateEmptyKey),
          validDate(dateInvalidKey),
          withinRange(incorpDate, now3MonthsLater, dateRange, dateRange, List(MessageDateFormat.format(incorpDate), MessageDateFormat.format(now3MonthsLater))),
          withinFourYearsPast(dateWithinFourYears)
        )).transform[LocalDate](
          date => LocalDate.of(date._3.toInt, date._2.toInt, date._1.toInt),
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        )
      )
    )
  )
}
