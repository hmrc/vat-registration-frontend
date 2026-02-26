/*
 * Copyright 2026 HM Revenue & Customs
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

import scala.util.matching.Regex

object StandardRateSuppliesForm {

  implicit val errorCode: String = "standardRateSupplies"
  val standardRateSuppliesKey = "standardRateSupplies"
  val regex: Regex = """^[0-9 .]+$""".r
  private val penceNotAllowed = """^[^.]+$""".r

  def form: Form[BigDecimal] = Form(
    single(
      standardRateSuppliesKey ->
        text.verifying(stopOnFail(
            regexPattern(regex),
            matchesRegex(penceNotAllowed, "validation.standardRateSupplies.penceNotAllowed"),
            mandatoryFullNumericText()
          ))
          .transform[BigDecimal](string =>
            BigDecimal(string).setScale(2, BigDecimal.RoundingMode.HALF_UP),
            _.toString
          )
          .verifying(inRange[BigDecimal](0, BigDecimal("999999999999999")))
    )
  )
}




