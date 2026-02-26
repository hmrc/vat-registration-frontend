/*
 * Copyright 2026 HM Revenue & Customs
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
import models.FRSDateChoice
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

import java.time.LocalDate

object OverBusinessGoodsForm extends RequiredBooleanForm {
  private val RADIO_INCLUSIVE: String = "value"

  override val errorMsg: String = "validation.frs.costsInclusive.missing"

  val form = Form(
    single(RADIO_INCLUSIVE -> requiredBoolean)
  )
}

trait OverBusinessGoodsPercentForm extends RequiredBooleanForm {
  private val RADIO_INCLUSIVE: String = "value"
  val pct: BigDecimal

  override val errorMsg: String = "validation.frs.costsLimited.missing"
  override lazy val errorMsgArgs: Seq[Any] = Seq(pct)

  def form: Form[Boolean] = Form(
    single(RADIO_INCLUSIVE -> requiredBoolean)
  )
}

object FRSStartDateForm {

  private val frsDateSelectionEmpty = "validation.frs.startDate.choice.missing"
  val dateEmptyKey = "validation.frs.startDate.missing"
  val dateInvalidKey = "validation.frs.startDate.invalid"
  private val dateBeforeVatStartDate = "validation.frs.startDate.range.below.vatStartDate"
  private val dateAfterMaxKey = "validation.frs.startDate.range.after.maxDate"

  private val frsStartDateRadio: String = "frsStartDateRadio"
  private val frsStartDateInput: String = "frsStartDate"

  implicit val specificErrorCode: String = "frs.startDate"

  implicit def formatter: Formatter[FRSDateChoice.Value] = new Formatter[FRSDateChoice.Value] {
    override val format = Some(("format.string", Nil))

    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key,"")).flatMap {
        case e if e == FRSDateChoice.VATDate.toString => Right(FRSDateChoice.VATDate)
        case e if e == FRSDateChoice.DifferentDate.toString => Right(FRSDateChoice.DifferentDate)
        case _ => Left(Seq(FormError(key, frsDateSelectionEmpty, Nil)))
      }
    }

    def unbind(key: String, value: FRSDateChoice.Value) = Map(key -> value.toString)
  }

  def form(minDate: LocalDate, maxDate: LocalDate) = Form(
    tuple(
      frsStartDateRadio -> Forms.of[FRSDateChoice.Value],
      frsStartDateInput -> mandatoryIf(
        isEqual(frsStartDateRadio, FRSDateChoice.DifferentDate),
        tuple("day" -> text, "month" -> text, "year" -> text).verifying(stopOnFail(
          nonEmptyDate(dateEmptyKey),
          validDate(dateInvalidKey),
          withinRange(minDate, maxDate, dateBeforeVatStartDate, dateAfterMaxKey, List(frsStartDateInput))
        )).transform[LocalDate] ({
          case (day, month, year) => LocalDate.of(year.toInt, month.toInt, day.toInt)
        }, {
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        })
      )
    )
  )
}

object EstimateTotalSalesForm {
  implicit val errorCode: ErrorCode = "frs.estimateTotalSales"
  val regex = """^[0-9 ,]*\.?[0-9]+$""".r
  val commasNotAllowed = """^[^,]+$""".r
  val moreThanTwoDecimalsNotAllowed = """^[0-9]*\.?[0-9]{1,2}$""".r

  val form = Form(single("totalSalesEstimate" -> text
    .verifying(stopOnFail(
      regexPattern(regex),
      matchesRegex(commasNotAllowed, "validation.estimateTotalSales.commasNotAllowed"),
      matchesRegex(moreThanTwoDecimalsNotAllowed, "validation.estimateTotalSales.moreThanTwoDecimalsNotAllowed"),
      mandatoryFullNumericText()))
    )
    .transform[BigDecimal](BigDecimal(_).setScale(2, BigDecimal.RoundingMode.HALF_UP), _.toString)
    .verifying(inRange[BigDecimal](1, BigDecimal("99999999999")))
  )
}

object ChooseBusinessTypeForm {
  implicit val errorCode: ErrorCode = "frs.chooseBusinessType"

  def form(validBusinessTypes: Seq[String]) = Form(single("value" -> optional(text)
    .transform[String](_.getOrElse(""), Some(_))
    .verifying(matches(validBusinessTypes.toList, "validation.frs.chooseBusinessType.missing"))
  ))
}