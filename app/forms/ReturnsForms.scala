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

import forms.FormValidation._
import models.DateSelection
import models.DateSelection.specific_date
import models.api.returns._
import play.api.data.Forms.{single, tuple, _}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

trait StartDateForm {
  protected val START_DATE_SELECTION = "value"
  protected val START_DATE = "startDate"

  protected val startDateChoiceMissing = "validation.startDate.choice.missing"

  protected val dateEmptyKey = "validation.startDate.missing"
  protected val dateInvalidKey = "validation.startDate.invalid"
  protected val dateRange = "validation.startDate.range"

  val dateFormat: DateTimeFormatter = DateTimeFormatter
    .ofLocalizedDate(java.time.format.FormatStyle.LONG)
    .withLocale(java.util.Locale.UK)

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

object AccountingPeriodForm {

  private val accountingPeriodInvalidKey = "validation.accounting.period.missing"
  private val ACCOUNTING_PERIOD = "value"

  val janStaggerKey = "jan"
  val febStaggerKey = "feb"
  val marStaggerKey = "mar"

  implicit def formatter: Formatter[QuarterlyStagger] = new Formatter[QuarterlyStagger] {
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], QuarterlyStagger] = {
      data.get(key) match {
        case Some(`janStaggerKey`) => Right(JanuaryStagger)
        case Some(`febStaggerKey`) => Right(FebruaryStagger)
        case Some(`marStaggerKey`) => Right(MarchStagger)
        case _ => Left(Seq(FormError(key, accountingPeriodInvalidKey, Nil)))
      }
    }

    def unbind(key: String, value: QuarterlyStagger) =
      Map(key -> {
        value match {
          case JanuaryStagger => janStaggerKey
          case FebruaryStagger => febStaggerKey
          case MarchStagger => marStaggerKey
        }
      })
  }

  val form: Form[QuarterlyStagger] = Form(
    single(ACCOUNTING_PERIOD -> Forms.of[QuarterlyStagger])
  )
}

object ChargeExpectancyForm extends RequiredBooleanForm {
  override val errorMsg = "validation.vat.charge.expectancy.missing"
  val EXPECT_CHARGE_MORE_VAT = "value"

  val form: Form[Boolean] = Form(
    single(EXPECT_CHARGE_MORE_VAT -> requiredBoolean)
  )
}

object MandatoryDateForm extends StartDateForm {
  private val dateWithinFourYears = "validation.startDateManIncorp.range.below4y"
  val radioAnswer = "value"
  val startDate = "date"

  def form(incorpDate: LocalDate, calculatedDate: LocalDate): Form[(DateSelection.Value, Option[LocalDate])] = Form(
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
          withinRange(incorpDate, calculatedDate, dateRange, dateRange, List(incorpDate.format(dateFormat), calculatedDate.format(dateFormat))),
          withinFourYearsPast(dateWithinFourYears)
        )).transform[LocalDate](
          { case (day, month, year) => LocalDate.of(year.toInt, month.toInt, day.toInt) },
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        )
      )
    )
  )

}

object ReturnFrequencyForm {

  private val returnFrequencyEmptyKey = "validation.vat.return.frequency.missing"
  private val RETURN_FREQUENCY = "value"

  val monthlyKey = "monthly"
  val quarterlyKey = "quarterly"
  val annualKey = "annual"

  implicit def formatter: Formatter[ReturnsFrequency] = new Formatter[ReturnsFrequency] {
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], ReturnsFrequency] = {
      data.get(key) match {
        case Some(`monthlyKey`) => Right(Monthly)
        case Some(`quarterlyKey`) => Right(Quarterly)
        case Some(`annualKey`) => Right(Annual)
        case _ => Left(Seq(FormError(key, returnFrequencyEmptyKey, Nil)))
      }
    }

    def unbind(key: String, value: ReturnsFrequency) =
      Map(key -> {
        value match {
          case Monthly => monthlyKey
          case Quarterly => quarterlyKey
          case Annual => annualKey
        }
      })
  }

  val form: Form[ReturnsFrequency] = Form(
    single(RETURN_FREQUENCY -> Forms.of[ReturnsFrequency])
  )
}

object VoluntaryDateForm extends StartDateForm {
  def form(dateRangeMin: LocalDate, dateRangeMax: LocalDate): Form[(DateSelection.Value, Option[LocalDate])] = Form(
    tuple(
      START_DATE_SELECTION -> Forms.of[DateSelection.Value],
      START_DATE -> mandatoryIf(
        isEqual(START_DATE_SELECTION, specific_date),
        tuple("day" -> text, "month" -> text, "year" -> text).verifying(StopOnFirstFail(
          nonEmptyDate(dateEmptyKey),
          validDate(dateInvalidKey),
          withinRange(dateRangeMin, dateRangeMax, dateRange, dateRange, List(dateRangeMin.format(dateFormat), dateRangeMax.format(dateFormat)))
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

  def form(incorpDate: LocalDate): Form[(DateSelection.Value, Option[LocalDate])] = Form(
    tuple(
      START_DATE_SELECTION -> Forms.of[DateSelection.Value],
      START_DATE -> mandatoryIf(
        isEqual(START_DATE_SELECTION, specific_date),
        tuple("day" -> text, "month" -> text, "year" -> text).verifying(StopOnFirstFail(
          nonEmptyDate(dateEmptyKey),
          validDate(dateInvalidKey),
          withinRange(incorpDate, now3MonthsLater, dateRange, dateRange, List(incorpDate.format(dateFormat), now3MonthsLater.format(dateFormat))),
          withinFourYearsPast(dateWithinFourYears)
        )).transform[LocalDate](
          date => LocalDate.of(date._3.toInt, date._2.toInt, date._1.toInt),
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        )
      )
    )
  )
}
