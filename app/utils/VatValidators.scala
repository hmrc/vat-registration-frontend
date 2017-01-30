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

import forms.vatDetails.TradingNameForm
import models.view.TradingName
import play.api.data.validation.{ValidationError, _}

object VatValidators {

  private val tradingNameRegex = """^[A-Za-z0-9.,\-()/!"%&*;'<>][A-Za-z0-9.,\-()/!"%&*;'<>]{0,55}$"""
  private val nonEmptyRegex = """^(?=\s*\S).*$"""
  val IN_VALID_TRADING_NAME_MSG_KEY = "pages.tradingName.validation.invalid.tradingName"
  val EMPTY_TRADING_NAME_MSG_KEY = "pages.tradingName.validation.empty.tradingName"

  def tradingNameValidation : Constraint[TradingName] = Constraint("constraint.tradingName")({
    text =>
      val errors = text match {
        case _ if text.yesNo == TradingName.TRADING_NAME_YES && !text.tradingName.get.matches(nonEmptyRegex)
        => Seq(ValidationError(EMPTY_TRADING_NAME_MSG_KEY, TradingNameForm.INPUT_TRADING_NAME))
        case _ if text.yesNo == TradingName.TRADING_NAME_YES && !text.tradingName.get.matches(tradingNameRegex)
        => Seq(ValidationError(IN_VALID_TRADING_NAME_MSG_KEY, TradingNameForm.INPUT_TRADING_NAME))
        case _ => Nil
      }
      if (errors.isEmpty) Valid else Invalid(errors)
  })

}
