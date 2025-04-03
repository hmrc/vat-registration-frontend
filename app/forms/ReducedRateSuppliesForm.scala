/*
 * Copyright 2025 HM Revenue & Customs
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

object ReducedRateSuppliesForm {

  implicit val errorCode: String = "reducedRateSupplies"
  val reducedRateSuppliesEstimateKey = "reducedRateSupplies"
  val regex = """^[0-9 .]+$""".r
  val penceNotAllowed = """^[^.]+$""".r

  val form: Form[BigDecimal] = Form(
    single(
      reducedRateSuppliesEstimateKey ->
        text
          .verifying(stopOnFail(
            regexPattern(regex),
            matchesRegex(penceNotAllowed, "validation.reducedRateSupplies.penceNotAllowed"),
            mandatoryFullNumericText
          ))
          .transform[BigDecimal](string =>
            BigDecimal(string).setScale(2, BigDecimal.RoundingMode.HALF_UP),
            _.toString
          )
          .verifying(inRange[BigDecimal](0, BigDecimal("999999999999999")))
    )
  )
}

