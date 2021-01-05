/*
 * Copyright 2021 HM Revenue & Customs
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

import models.TradingNameView
import forms.FormValidation._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.voa.play.form.ConditionalMappings._

object ApplyForEoriForm extends RequiredBooleanForm {
  override val errorMsg = "validation.applyForEori.missing"
  val RADIO_YES_NO: String = "value"
  val form: Form[Boolean] = Form(
    single(RADIO_YES_NO -> requiredBoolean)
  )

}

object TradingNameForm extends RequiredBooleanForm {
  override val errorMsg = "validation.tradingNameRadio.missing"
  val INPUT_TRADING_NAME: String = "tradingName"
  val invalidNameSet: Set[String] = Set("limited", "ltd", "llp", "plc")
  implicit val errorCode: ErrorCode = INPUT_TRADING_NAME
  val RADIO_YES_NO: String = "value"
  val TRADING_NAME_REGEX = """^[A-Za-z0-9 .,\-()/!"%&*;'<>]+$""".r

  val form = Form(
    tuple(
      RADIO_YES_NO -> requiredBoolean,
      INPUT_TRADING_NAME -> mandatoryIf(
        isEqual(RADIO_YES_NO, "true"),
        text.transform(removeNewlineAndTrim, identity[String]).verifying(StopOnFirstFail(
          nonEmptyValidText(TRADING_NAME_REGEX),
          isValidTradingName("tradingName"),
          maxLenText(35)
        ))
      )
    )
  )

  def isValidTradingName(tradingNameForm: String): Constraint[String] = Constraint { tradingName: String =>

    val isValidTradingName: Boolean = tradingName.matches("""^[A-Za-z0-9 .,\-()/!"%&*;'<>]+$""")
    val wordSet = tradingName.toLowerCase.split(" ").toSet

    if (isValidTradingName) {
      if (invalidNameSet.intersect(wordSet).nonEmpty) {
        Invalid(s"validation.$tradingNameForm.invalid")
      } else {
        Valid
      }
    } else {
      Invalid(s"validation.$tradingNameForm.invalid")
    }

  }

  def fillWithPrePop(optTradingNameFormData: Option[TradingNameView]): Form[(Boolean, Option[String])] = {
    optTradingNameFormData match {
      case Some(tradingNameFormData) =>
        form.fill(tradingNameFormData.yesNo, tradingNameFormData.tradingName)
      case None =>
        form
    }
  }
}
