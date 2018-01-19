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

import forms.FormValidation._
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.voa.play.form.ConditionalMappings._

object ApplyEoriForm extends RequiredBooleanForm {
  override val errorMsg = "validation.applyEori.missing"
  val RADIO_YES_NO: String = "applyEoriRadio"

  val form = Form(
    single(RADIO_YES_NO -> requiredBoolean)
  )
}

object EuGoodsForm extends RequiredBooleanForm {
  override val errorMsg = "validation.euGoods.missing"
  val RADIO_YES_NO: String = "euGoodsRadio"
  val form = Form(
    single(RADIO_YES_NO -> requiredBoolean)
  )

}

object TradingNameForm extends RequiredBooleanForm {
  override val errorMsg = "validation.tradingNameRadio.missing"
  val INPUT_TRADING_NAME: String = "tradingName"

  implicit val errorCode: ErrorCode = INPUT_TRADING_NAME
  val RADIO_YES_NO: String = "tradingNameRadio"
  val TRADING_NAME_REGEX = """^[A-Za-z0-9.,\-()/!"%&*;'<>][A-Za-z0-9 .,\-()/!"%&*;'<>]{0,55}$""".r

  val form = Form(
    tuple(
      RADIO_YES_NO -> requiredBoolean,
      INPUT_TRADING_NAME -> mandatoryIf(
        isEqual(RADIO_YES_NO, "true"),
        text.verifying(nonEmptyValidText(TRADING_NAME_REGEX))
      )
    )
  )
}