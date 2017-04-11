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

package forms.vatTradingDetails

import forms.FormValidation._
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.TradingNameView.TRADING_NAME_YES
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.voa.play.form.ConditionalMappings._

object TradingNameForm {
  
  val RADIO_YES_NO: String = "tradingNameRadio"
  val INPUT_TRADING_NAME: String = "tradingName"

  val TRADING_NAME_REGEX = """^[A-Za-z0-9.,\-()/!"%&*;'<>][A-Za-z0-9 .,\-()/!"%&*;'<>]{0,55}$""".r

  implicit val errorCode: ErrorCode = INPUT_TRADING_NAME

  val form = Form(
    mapping(
      RADIO_YES_NO -> missingFieldMapping().verifying(TradingNameView.valid),
      INPUT_TRADING_NAME -> mandatoryIf(
        isEqual(RADIO_YES_NO, TRADING_NAME_YES),
        text.verifying(nonEmptyValidText(TRADING_NAME_REGEX)))
    )(TradingNameView.apply)(TradingNameView.unapply)
  )

}
