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

import forms.FormValidation._
import play.api.data.Form
import play.api.data.Forms.{single, text}
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.play.mappers.StopOnFirstFail

object SoleTraderNameForm {
  val errMsg = "validation.soleTrader.tradingName.missing"
  val tradingNameKey = "trading-name"
  val invalidNameSet: Set[String] = Set("limited", "ltd", "llp", "plc")
  implicit val errorCode: ErrorCode = tradingNameKey
  val tradingNameRegEx = """^[A-Za-z0-9 ,.&''\/-]+$""".r


  val form = Form(
      single(
        tradingNameKey -> text.transform(removeNewlineAndTrim, identity[String]).verifying(StopOnFirstFail(
          nonEmptyValidText(tradingNameRegEx),
          isValidTradingName("tradingName"),
          maxLenText(160)
        ))
      )
    )

  private def isValidTradingName(soleTraderNameForm: String): Constraint[String] = Constraint { tradingName: String =>
    val isValidTradingName: Boolean = tradingName.matches("""^[A-Za-z0-9 ,.&''\/-]+$""")
    val wordSet = tradingName.toLowerCase.split(" ").toSet

    if (isValidTradingName) {
      if (invalidNameSet.intersect(wordSet).nonEmpty) {
        Invalid(s"validation.$soleTraderNameForm.invalid")
      } else {
        Valid
      }
    } else {
      Invalid(s"validation.$soleTraderNameForm.invalid")
    }
  }
}
