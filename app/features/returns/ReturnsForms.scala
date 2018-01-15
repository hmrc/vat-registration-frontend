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

import features.returns.DateSelection.{specific_date, _}
import features.returns.{DateSelection, Frequency, Stagger}
import forms.FormValidation._
import play.api.data.Forms.{single, tuple, _}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

object AccountingPeriodForm {

  val accountingPeriodInvalidKey = "validation.accounting.period.missing"
  val ACCOUNTING_PERIOD: String = "accountingPeriodRadio"

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

  override val errorMsg = "validation.vat.charge.expectancy.missing"
  val EXPECT_CHARGE_MORE_VAT: String = "chargeExpectancyRadio"

  val form = Form(
    single(EXPECT_CHARGE_MORE_VAT -> requiredBoolean)
  )
}

object MandatoryDateForm {

  val mandatorySelectionInvalidKey = "validation.startDate.choice.missing"
  val dateEmptyKey = "validation.startDate.missing"
  val dateInvalidKey = "validation.startDate.invalid"
  val dateBelowIncorp = "validation.startDateManIncorp.range.belowIncorp"
  val dateAboveCalculated = "validation.startDateManIncorp.range.above"
  val dateWithinFourYears = "validation.startDateManIncorp.range.below4y"

  val MANDATORY_SELECTION: String = "startDateRadio"
  val MANDATORY_DATE: String = "startDate"

  implicit def formatter: Formatter[DateSelection.Value] = new Formatter[DateSelection.Value] {

    override val format = Some(("format.string", Nil))

    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key,"")).right.flatMap {
        case e if e == DateSelection.calculated_date.toString => Right(DateSelection.calculated_date)
        case e if e == DateSelection.business_start_date.toString => Right(DateSelection.business_start_date)
        case e if e == DateSelection.specific_date.toString => Right(DateSelection.specific_date)
        case _ => Left(Seq(FormError(key, mandatorySelectionInvalidKey, Nil)))
      }
    }

    def unbind(key: String, value: DateSelection.Value) = Map(key -> value.toString)
  }

  def form(incorpDate: LocalDate, calculatedDate: LocalDate) = Form(
    tuple(
      MANDATORY_SELECTION -> Forms.of[DateSelection.Value],
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

object ReturnFrequencyForm {

  val returnFrequencyEmptyKey = "validation.vat.return.frequency.missing"
  val RETURN_FREQUENCY: String = "returnFrequencyRadio"

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

object VoluntaryDateForm {

  val voluntarySelectionEmptyKey = "validation.startDate.choice.missing"
  val voluntarySelectionInvalidKey = "validation.startDate.choice.missing"

  val dateEmptyKey = "validation.startDate.missing"
  val dateInvalidKey = "validation.startDate.invalid"
  val dateBelow = "validation.startDate.range.below"
  val dateAfter = "validation.startDate.range.above"

  val VOLUNTARY_SELECTION: String = "startDateRadio"
  val VOLUNTARY_DATE: String = "startDate"

  implicit def formatter: Formatter[DateSelection.Value] = new Formatter[DateSelection.Value] {

    override val format = Some(("format.string", Nil))

    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key,"")).right.flatMap {
        case e if e == DateSelection.company_registration_date.toString => Right(DateSelection.company_registration_date)
        case e if e == DateSelection.business_start_date.toString => Right(DateSelection.business_start_date)
        case e if e == DateSelection.specific_date.toString => Right(DateSelection.specific_date)
        case _ => Left(Seq(FormError(key, voluntarySelectionInvalidKey, Nil)))
      }
    }

    def unbind(key: String, value: DateSelection.Value) = Map(key -> value.toString)
  }

  val form = Form(
    tuple(
      VOLUNTARY_SELECTION -> Forms.of[DateSelection.Value],
      VOLUNTARY_DATE -> mandatoryIf(
        isEqual(VOLUNTARY_SELECTION, specific_date),
        tuple("day" -> text, "month" -> text, "year" -> text).verifying(StopOnFirstFail(
          nonEmptyDate(dateEmptyKey),
          validDate(dateInvalidKey),
          withinRange(LocalDate.now().plusDays(2), LocalDate.now().plusMonths(3), dateBelow, dateAfter)
        )).transform[LocalDate](
          date => LocalDate.of(date._3.toInt,date._2.toInt,date._1.toInt),
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        )
      )
    )
  )
}

object VoluntaryDateFormIncorp {

  val voluntarySelectionInvalidKey = "validation.startDate.choice.missing"
  val voluntaryDateEmptyKey = "validation.startDate.missing"
  val voluntaryDateInvalidKey = "validation.startDate.invalid"

  val dateBelowIncorp = "validation.startDate.range.belowIncorp"
  val dateAboveThree = "validation.startDate.range.above"
  val dateWithinFourYears = "validation.startDate.range.below4y"

  val VOLUNTARY_SELECTION: String = "startDateRadio"
  val VOLUNTARY_DATE: String = "startDate"

  implicit def formatter: Formatter[DateSelection.Value] = new Formatter[DateSelection.Value] {

    override val format = Some(("format.string", Nil))
    
    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key,"")).right.flatMap {
        case e if e == DateSelection.company_registration_date.toString => Right(DateSelection.company_registration_date)
        case e if e == DateSelection.business_start_date.toString => Right(DateSelection.business_start_date)
        case e if e == DateSelection.specific_date.toString => Right(DateSelection.specific_date)
        case _ => Left(Seq(FormError(key, voluntarySelectionInvalidKey, Nil)))
      }
    }

    def unbind(key: String, value: DateSelection.Value) = Map(key -> value.toString)
  }

  def form(incorpDate: LocalDate) = Form(
    tuple(
      VOLUNTARY_SELECTION -> Forms.of[DateSelection.Value],
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
