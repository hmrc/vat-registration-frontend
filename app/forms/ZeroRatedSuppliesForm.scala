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
import models.TurnoverEstimates
import play.api.data.Form
import play.api.data.Forms._

object ZeroRatedSuppliesForm {

  implicit val errorCode: String = "zeroRatedSupplies"
  val zeroRatedSuppliesKey = "zeroRatedSupplies"

  def form(turnoverEstimates: TurnoverEstimates): Form[BigDecimal] = Form(
    single(
      zeroRatedSuppliesKey ->
        text
          .verifying(mandatoryFullNumericText)
          .transform[BigDecimal](string =>
            BigDecimal(string.replace(",", "")).setScale(2, BigDecimal.RoundingMode.HALF_UP),
            _.toString
          )
          .verifying(inRange[BigDecimal](0, turnoverEstimates.turnoverEstimate))
    )
  )
}
