/*
 * Copyright 2020 HM Revenue & Customs
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
import java.time.format.DateTimeFormatter

import models.DateSelection.{specific_date, _}
import models.Stagger
import forms.FormValidation._
import models.{DateSelection, Frequency, Stagger}
import play.api.data.Forms.{single, tuple, _}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.hmrc.time.workingdays.BankHolidaySet
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

import scala.language.implicitConversions

trait StartDateForm {
  protected val START_DATE_SELECTION = "startDateRadio"
  protected val START_DATE           = "startDate"

  protected val startDateChoiceMissing  = "validation.startDate.choice.missing"

  protected val dateEmptyKey    = "validation.startDate.missing"
  protected val dateInvalidKey  = "validation.startDate.invalid"
  protected val dateRange       = "validation.startDate.range"

  protected val dateFormat = DateTimeFormatter
    .ofLocalizedDate(java.time.format.FormatStyle.LONG)
    .withLocale(java.util.Locale.UK)

  implicit def formatter: Formatter[DateSelection.Value] = new Formatter[DateSelection.Value] {

    override val format = Some(("format.string", Nil))

    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key,"")).right.flatMap {
        case e if e == DateSelection.company_registration_date.toString => Right(DateSelection.company_registration_date)
        case e if e == DateSelection.business_start_date.toString       => Right(DateSelection.business_start_date)
        case e if e == DateSelection.specific_date.toString             => Right(DateSelection.specific_date)
        case e if e == DateSelection.calculated_date.toString           => Right(DateSelection.calculated_date)
        case _                                                          => Left(Seq(FormError(key, startDateChoiceMissing, Nil)))
      }
    }

    def unbind(key: String, value: DateSelection.Value) = Map(key -> value.toString)
  }
}

object AccountingPeriodForm {

  private val accountingPeriodInvalidKey = "validation.accounting.period.missing"
  private val ACCOUNTING_PERIOD          = "accountingPeriodRadio"

  implicit def formatter: Formatter[Stagger.Value] = new Formatter[Stagger.Value] {

    override val format = Some(("format.string", Nil))

    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key,"")).right.flatMap {
        case e if e == Stagger.jan.toString => Right(Stagger.jan)
        case e if e == Stagger.feb.toString => Right(Stagger.feb)
        case e if e == Stagger.mar.toString => Right(Stagger.mar)
        case _ => Left(Seq(FormError(key, accountingPeriodInvalidKey, Nil)))
      }
    }

    def unbind(key: String, value: Stagger.Value) = Map(key -> value.toString)
  }

  val form = Form(
    single(ACCOUNTING_PERIOD -> Forms.of[Stagger.Value])
  )
}

object ChargeExpectancyForm extends RequiredBooleanForm {
  override val errorMsg      = "validation.vat.charge.expectancy.missing"
  val EXPECT_CHARGE_MORE_VAT = "chargeExpectancyRadio"

  val form = Form(
    single(EXPECT_CHARGE_MORE_VAT -> requiredBoolean)
  )
}

object MandatoryDateForm extends StartDateForm {
  private val dateWithinFourYears = "validation.startDateManIncorp.range.below4y"

  def form(incorpDate: LocalDate, calculatedDate: LocalDate): Form[(DateSelection.Value, Option[LocalDate])] = Form(
    tuple(
      START_DATE_SELECTION -> Forms.of[DateSelection.Value],
      START_DATE           -> mandatoryIf(
        isEqual(START_DATE_SELECTION, specific_date),
        tuple("day" -> text, "month" -> text, "year" -> text).verifying(StopOnFirstFail(
          nonEmptyDate(dateEmptyKey),
          validDate(dateInvalidKey),
          withinRange(incorpDate, calculatedDate, dateRange, dateRange, List(incorpDate.format(dateFormat), calculatedDate.format(dateFormat))),
          withinFourYearsPast(dateWithinFourYears)
        )).transform[LocalDate](
          date => LocalDate.of(date._3.toInt,date._2.toInt,date._1.toInt),
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        )
      )
    )
  )

}

object ReturnFrequencyForm {

  private val returnFrequencyEmptyKey = "validation.vat.return.frequency.missing"
  private val RETURN_FREQUENCY        = "returnFrequencyRadio"

  implicit def formatter: Formatter[Frequency.Value] = new Formatter[Frequency.Value] {

    override val format = Some(("format.string", Nil))

    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key,"")).right.flatMap {
        case e if e == Frequency.monthly.toString => Right(Frequency.monthly)
        case e if e == Frequency.quarterly.toString => Right(Frequency.quarterly)
        case _ => Left(Seq(FormError(key, returnFrequencyEmptyKey, Nil)))
      }
    }

    def unbind(key: String, value: Frequency.Value) = Map(key -> value.toString)
  }

  val form = Form(
    single(RETURN_FREQUENCY -> Forms.of[Frequency.Value]
  ))
}

object VoluntaryDateForm extends StartDateForm {
  def form(dateRangeMin: LocalDate, dateRangeMax: LocalDate)(implicit bhs: BankHolidaySet): Form[(DateSelection.Value, Option[LocalDate])] = Form(
    tuple(
      START_DATE_SELECTION -> Forms.of[DateSelection.Value],
      START_DATE -> mandatoryIf(
        isEqual(START_DATE_SELECTION, specific_date),
        tuple("day" -> text, "month" -> text, "year" -> text).verifying(StopOnFirstFail(
          nonEmptyDate(dateEmptyKey),
          validDate(dateInvalidKey),
          withinRange(dateRangeMin, dateRangeMax, dateRange, dateRange, List(dateRangeMin.format(dateFormat), dateRangeMax.format(dateFormat)))
        )).transform[LocalDate](
          date => LocalDate.of(date._3.toInt,date._2.toInt,date._1.toInt),
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        )
      )
    )
  )
}

object VoluntaryDateFormIncorp extends StartDateForm {
  private val dateWithinFourYears = "validation.startDate.range.below4y"
  private val dateIsWeekend       = "validation.startDate.isWeekend"

  private val now3MonthsLater = LocalDate.now().plusMonths(3)

  def form(incorpDate: LocalDate)(implicit bhs: BankHolidaySet) = Form(
    tuple(
      START_DATE_SELECTION -> Forms.of[DateSelection.Value],
      START_DATE           -> mandatoryIf(
        isEqual(START_DATE_SELECTION, specific_date),
        tuple("day" -> text, "month" -> text, "year" -> text).verifying(StopOnFirstFail(
          nonEmptyDate(dateEmptyKey),
          validDate(dateInvalidKey),
          withinRange(incorpDate, now3MonthsLater, dateRange, dateRange, List(incorpDate.format(dateFormat), now3MonthsLater.format(dateFormat))),
          withinFourYearsPast(dateWithinFourYears)
        )).transform[LocalDate](
          date => LocalDate.of(date._3.toInt,date._2.toInt,date._1.toInt),
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        )
      )
    )
  )
}
