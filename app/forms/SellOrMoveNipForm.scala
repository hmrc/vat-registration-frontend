/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

object SellOrMoveNipForm extends RequiredBooleanForm {
  override val errorMsg = "nip.error.missing"
  val inputAmount: String = "sellOrMoveNip"
  implicit val errorCode: ErrorCode = inputAmount
  val yesNo: String = "value"
  val regex = """^[0-9 ,]*\.?[0-9]+$""".r
  val commasNotAllowed = """^[^,]+$""".r
  val moreThanTwoDecimalsNotAllowed = """^[0-9]*\.?[0-9]{1,2}$""".r

  val form = Form(
    tuple(
      yesNo -> requiredBoolean,
      inputAmount -> mandatoryIf(
        isEqual(yesNo, "true"),
        text.verifying(stopOnFail(
          regexPattern(regex),
          matchesRegex(commasNotAllowed, "validation.sellOrMoveNip.commasNotAllowed"),
          matchesRegex(moreThanTwoDecimalsNotAllowed, "validation.sellOrMoveNip.moreThanTwoDecimalsNotAllowed"),
          mandatoryFullNumericText
        ))
        .transform[BigDecimal](s =>
          BigDecimal(s).setScale(2, BigDecimal.RoundingMode.HALF_UP),
          _.toString
        )
      )
    )
  )
}
