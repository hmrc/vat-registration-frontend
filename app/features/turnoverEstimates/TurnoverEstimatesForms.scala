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
import uk.gov.hmrc.play.mappers.StopOnFirstFail

object EstimateVatTurnoverForm {

  val TURNOVER_ESTIMATE: String = "turnoverEstimate"

  val estimateVatTurnoverMissing = "validation.estimate.vat.turnover.missing"
  val estimateVatTurnoverTooLow = "validation.estimate.vat.turnover.low"
  val estimateVatTurnoverTooHigh = "validation.estimate.vat.turnover.high"
  val estimateVatTurnoverInvalid = "validation.estimate.vat.turnover.invalid"

  val form: Form[Long] = Form(
    single(
      TURNOVER_ESTIMATE -> trimmedText.verifying(StopOnFirstFail(
        mandatory(estimateVatTurnoverMissing),
        verifyIsNumeric(estimateVatTurnoverInvalid),
        boundedLong(estimateVatTurnoverTooLow, estimateVatTurnoverTooHigh))
      ).transform[Long](_.toLong, _.toString)
    )
  )
}
