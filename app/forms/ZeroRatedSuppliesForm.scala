/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.play.mappers.StopOnFirstFail

object ZeroRatedSuppliesForm {

  implicit val errorCode: String = "zeroRatedSupplies"
  val zeroRatedSuppliesKey = "zeroRatedSupplies"
  val regex = """^[0-9 ,]*\.?[0-9]+$""".r
  val commasNotAllowed = """^[^,]+$""".r
  val moreThanTwoDecimalsNotAllowed = """^[0-9]*\.?[0-9]{1,2}$""".r

  def form(turnoverEstimate: BigDecimal): Form[BigDecimal] = Form(
    single(
      zeroRatedSuppliesKey ->
        text
          .verifying(StopOnFirstFail(
            regexPattern(regex),
            matchesRegex(commasNotAllowed, "validation.zeroRatedSupplies.commasNotAllowed"),
            matchesRegex(moreThanTwoDecimalsNotAllowed, "validation.zeroRatedSupplies.moreThanTwoDecimalsNotAllowed"),
            mandatoryFullNumericText))
          .transform[BigDecimal](string =>
            BigDecimal(string).setScale(2, BigDecimal.RoundingMode.HALF_UP),
            _.toString
          )
          .verifying(inRange[BigDecimal](0, turnoverEstimate))
    )
  )
}
