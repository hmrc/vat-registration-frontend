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

import forms.FormValidation._
import frs.FRSDateChoice
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

object OverBusinessGoodsForm extends RequiredBooleanForm {
  val RADIO_INCLUSIVE: String = "annualCostsInclusiveRadio"

  override val errorMsg: String = "validation.frs.costsInclusive.missing"

  val form = Form(
    single(RADIO_INCLUSIVE -> requiredBoolean)
  )
}

trait OverBusinessGoodsPercentForm extends RequiredBooleanForm {
  val RADIO_INCLUSIVE: String = "annualCostsLimitedRadio"
  val pct: Long

  override val errorMsg: String = "validation.frs.costsLimited.missing"
  override lazy val errorMsgArgs: Seq[Any] = Seq(pct)

  def form = Form(
    single(RADIO_INCLUSIVE -> requiredBoolean)
  )
}

object FRSStartDateForm {

  val frsDateSelectionEmpty = "validation.frs.startDate.choice.missing"
  val dateEmptyKey = "validation.frs.startDate.missing"
  val dateInvalidKey = "validation.frs.startDate.invalid"
  val dateAfter = "validation.frs.startDate.range.below"

  val frsStartDateRadio: String = "frsStartDateRadio"
  val frsStartDateInput: String = "frsStartDate"

  implicit val specificErrorCode: String = "frs.startDate"

  implicit def formatter: Formatter[FRSDateChoice.Value] = new Formatter[FRSDateChoice.Value] {
    override val format = Some(("format.string", Nil))

    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key,"")).right.flatMap {
        case e if e == FRSDateChoice.VATDate.toString => Right(FRSDateChoice.VATDate)
        case e if e == FRSDateChoice.DifferentDate.toString => Right(FRSDateChoice.DifferentDate)
        case _ => Left(Seq(FormError(key, frsDateSelectionEmpty, Nil)))
      }
    }

    def unbind(key: String, value: FRSDateChoice.Value) = Map(key -> value.toString)
  }

  val form = Form(
    tuple(
      frsStartDateRadio -> Forms.of[FRSDateChoice.Value],
      frsStartDateInput -> mandatoryIf(
        isEqual(frsStartDateRadio, FRSDateChoice.DifferentDate),
        tuple("day" -> text, "month" -> text, "year" -> text).verifying(StopOnFirstFail(
          nonEmptyDate(dateEmptyKey),
          validDate(dateInvalidKey),
          dateAfterDate(LocalDate.now().plusDays(2), dateAfter)
        )).transform[LocalDate](
          date => LocalDate.of(date._3.toInt,date._2.toInt,date._1.toInt),
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        )
      )
    )
  )
}

object EstimateTotalSalesForm {
  implicit val errorCode: ErrorCode = "frs.estimateTotalSales"

  val form = Form(single("totalSalesEstimate" -> text
    .verifying(mandatoryNumericText)
    .transform[Long](taxEstimateTextToLong, _.toString)
    .verifying(inRange[Long](1, 99999999999L))
  ))
}

object ChooseBusinessTypeForm {
  implicit val errorCode: ErrorCode = "frs.chooseBusinessType"

  def form(validBusinessTypes: Seq[String]) = Form(single("businessType" -> optional(text)
    .transform[String](_.getOrElse(""), Some(_))
    .verifying(matches(validBusinessTypes.toList, "validation.frs.chooseBusinessType.missing"))
  ))
}