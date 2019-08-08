/*
 * Copyright 2019 HM Revenue & Customs
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

package features.sicAndCompliance.forms

import forms.FormValidation.{ErrorCode, boundedInt, intToText, mandatoryNumericText, numberOfWorkersToInt, textMapping}
import features.sicAndCompliance.models.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import play.api.data.Form
import play.api.data.Forms.{mapping, text}

object CompanyProvideWorkersForm {
  val RADIO_YES_NO: String = "companyProvideWorkersRadio"
  implicit val errorCode: ErrorCode = "companyProvideWorkers"

  val form = Form(
    mapping(
      RADIO_YES_NO -> textMapping().verifying(CompanyProvideWorkers.valid)
    )(CompanyProvideWorkers.apply)(CompanyProvideWorkers.unapply)
  )
}

object WorkersForm {
  val NUMBER_OF_WORKERS: String = "numberOfWorkers"

  implicit val errorCode: ErrorCode = "labourCompliance.numberOfWorkers"

  val form = Form(
    mapping(
      NUMBER_OF_WORKERS -> text.verifying(mandatoryNumericText).
        transform(numberOfWorkersToInt, intToText).verifying(boundedInt)
    )(Workers.apply)(Workers.unapply)
  )
}

object TemporaryContractsForm {
  val RADIO_YES_NO: String = "temporaryContractsRadio"

  val form = Form(
    mapping(
      RADIO_YES_NO -> textMapping()("labourCompliance.temporaryContracts").verifying(TemporaryContracts.valid)
    )(TemporaryContracts.apply)(TemporaryContracts.unapply)
  )
}

object SkilledWorkersForm {
  val RADIO_YES_NO: String = "skilledWorkersRadio"
  implicit val errorCode: ErrorCode = "skilledWorkers"

  val form = Form(
    mapping(
      RADIO_YES_NO -> textMapping().verifying(SkilledWorkers.valid)
    )(SkilledWorkers.apply)(SkilledWorkers.unapply)
  )
}
