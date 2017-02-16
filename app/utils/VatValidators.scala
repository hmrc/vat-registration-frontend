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

import forms.vatDetails.{EstimateVatTurnoverForm, TradingNameForm}
import models.view.{EstimateVatTurnover, TradingName}
import play.api.data.validation.{ValidationError, _}

object VatValidators {

  private val TRADING_NAME_REGEX = """^[A-Za-z0-9.,\-()/!"%&*;'<>][A-Za-z0-9 .,\-()/!"%&*;'<>]{0,55}$"""
  private val NON_EMPTY_REGEX = """^(?=\s*\S).*$"""
  private val MAX_TURNOVER_ESTIMATE = 1000000000000000L
  private val MIN_TURNOVER_ESTIMATE = 0L

  val EMPTY_TRADING_NAME_MSG_KEY = "pages.tradingName.validation.empty.tradingName"
  val IN_VALID_TRADING_NAME_MSG_KEY = "pages.tradingName.validation.invalid.tradingName"
  val TURNOVER_ESTIMATE_LOW_MSG_KEY = "pages.estimate.vat.turnover.validation.low"
  val TURNOVER_ESTIMATE_HIGH_MSG_KEY = "pages.estimate.vat.turnover.validation.high"
  val TURNOVER_ESTIMATE_EMPTY_MSG_KEY = "pages.estimate.vat.turnover.validation.empty"

  def tradingNameValidation : Constraint[TradingName] = Constraint("constraint.tradingName")({
    text =>
      val errors = text match {
        case _ if text.yesNo == TradingName.TRADING_NAME_YES && !text.tradingName.getOrElse("").matches(NON_EMPTY_REGEX)
        => Seq(ValidationError(EMPTY_TRADING_NAME_MSG_KEY, TradingNameForm.INPUT_TRADING_NAME))
        case _ if text.yesNo == TradingName.TRADING_NAME_YES && !text.tradingName.getOrElse("").matches(TRADING_NAME_REGEX)
        => Seq(ValidationError(IN_VALID_TRADING_NAME_MSG_KEY, TradingNameForm.INPUT_TRADING_NAME))
        case _ => Nil
      }
      if (errors.isEmpty) Valid else Invalid(errors)
  })

  def turnoverEstimateValidation : Constraint[EstimateVatTurnover] = Constraint("constraint.turnoverEstimate")({
    text =>
      val errors = text match {
        case EstimateVatTurnover(None)
        => Seq(ValidationError(TURNOVER_ESTIMATE_EMPTY_MSG_KEY, EstimateVatTurnoverForm.INPUT_ESTIMATE))
        case _ if text.vatTurnoverEstimate.getOrElse(0L) < MIN_TURNOVER_ESTIMATE
        => Seq(ValidationError(TURNOVER_ESTIMATE_LOW_MSG_KEY, EstimateVatTurnoverForm.INPUT_ESTIMATE))
        case _ if text.vatTurnoverEstimate.getOrElse(0L) > MAX_TURNOVER_ESTIMATE
        => Seq(ValidationError(TURNOVER_ESTIMATE_HIGH_MSG_KEY, EstimateVatTurnoverForm.INPUT_ESTIMATE))
        case _ => Nil
      }
      if (errors.isEmpty) Valid else Invalid(errors)
  })


}
