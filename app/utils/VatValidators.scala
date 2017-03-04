/*
 * Copyright 2017 HM Revenue & Customs
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

package utils

import forms.vatDetails.AccountingPeriodForm.RADIO_ACCOUNTING_PERIOD
import forms.vatDetails.EstimateVatTurnoverForm._
import forms.vatDetails.EstimateZeroRatedSalesForm._
import models.view.{AccountingPeriod, EstimateVatTurnover, EstimateZeroRatedSales}
import org.apache.commons.lang3.StringUtils
import play.api.data.validation.{ValidationError, _}

import scala.util.matching.Regex

object VatValidators {

  private val MAX_TURNOVER_ESTIMATE = 1000000000000000L
  private val MIN_TURNOVER_ESTIMATE = 0L

  val MAX_ESTIMATE = 1000000000000000L
  val MIN_ESTIMATE = 0L


  val TURNOVER_ESTIMATE_LOW_MSG_KEY = "pages.estimate.vat.turnover.validation.low"
  val TURNOVER_ESTIMATE_HIGH_MSG_KEY = "pages.estimate.vat.turnover.validation.high"
  val TURNOVER_ESTIMATE_EMPTY_MSG_KEY = "pages.estimate.vat.turnover.validation.empty"
  val EMPTY_ACCOUNTING_PERIOD_MSG_KEY = "error.required"

  val ZERO_RATED_SALES_ESTIMATE_LOW_MSG_KEY = "pages.estimate.zero.rated.sales.validation.low"
  val ZERO_RATED_SALES_ESTIMATE_HIGH_MSG_KEY = "pages.estimate.zero.rated.sales.validation.high"
  val ZERO_RATED_SALES_ESTIMATE_EMPTY_MSG_KEY = "pages.estimate.zero.rated.sales.validation.empty"

  def turnoverEstimateValidation : Constraint[EstimateVatTurnover] = Constraint("constraint.turnoverEstimate")({
    text =>
      val errors = text match {
        case EstimateVatTurnover(None)
          => Seq(ValidationError(TURNOVER_ESTIMATE_EMPTY_MSG_KEY, TURNOVER_ESTIMATE))
        case EstimateVatTurnover(Some(estimateVatTurnover)) if estimateVatTurnover < MIN_TURNOVER_ESTIMATE
          => Seq(ValidationError(TURNOVER_ESTIMATE_LOW_MSG_KEY, TURNOVER_ESTIMATE))
        case EstimateVatTurnover(Some(estimateVatTurnover)) if estimateVatTurnover > MAX_TURNOVER_ESTIMATE
          => Seq(ValidationError(TURNOVER_ESTIMATE_HIGH_MSG_KEY, TURNOVER_ESTIMATE))
        case _ => Nil
      }
      if (errors.isEmpty) Valid else Invalid(errors)
  })

  def zeroRatedSalesEstimateValidation : Constraint[EstimateZeroRatedSales] = Constraint("constraint.zeroRatedSalesEstimate")({
    text =>
      val errors = text match {
        case EstimateZeroRatedSales(None)
          => Seq(ValidationError(ZERO_RATED_SALES_ESTIMATE_EMPTY_MSG_KEY, ZERO_RATED_SALES_ESTIMATE))
        case EstimateZeroRatedSales(Some(zeroRatedSalesEstimate)) if zeroRatedSalesEstimate < MIN_TURNOVER_ESTIMATE
          => Seq(ValidationError(ZERO_RATED_SALES_ESTIMATE_LOW_MSG_KEY, ZERO_RATED_SALES_ESTIMATE))
        case EstimateZeroRatedSales(Some(zeroRatedSalesEstimate)) if zeroRatedSalesEstimate > MAX_TURNOVER_ESTIMATE
          => Seq(ValidationError(ZERO_RATED_SALES_ESTIMATE_HIGH_MSG_KEY, ZERO_RATED_SALES_ESTIMATE))
        case _ => Nil
      }
      if (errors.isEmpty) Valid else Invalid(errors)
  })

  def accountingPeriodValidation : Constraint[AccountingPeriod] = Constraint("constraint.accountingPeriod")({
    text =>
      val errors = text match {
        case AccountingPeriod(None) => Seq(ValidationError(EMPTY_ACCOUNTING_PERIOD_MSG_KEY, RADIO_ACCOUNTING_PERIOD))
        case _ => Nil
      }
      if (errors.isEmpty) Valid else Invalid(errors)
  })

  def nonEmptyValidText(field: String, pattern: Regex): Constraint[String] = Constraint[String] {
    input: String =>
      input match {
        case `pattern`(_*) => Valid
        case s if StringUtils.isNotBlank(s) => Invalid(s"validation.$field.invalid")
        case _ => Invalid(s"validation.$field.empty")
      }
  }

}
