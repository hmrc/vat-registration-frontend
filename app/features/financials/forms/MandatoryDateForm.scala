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
import play.api.data.{Form, FormError, Forms}
import play.api.data.Forms._
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import forms.FormValidation._
import play.api.data.format.Formatter
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

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

    // default play binding is to data.getOrElse(key, "false")
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
