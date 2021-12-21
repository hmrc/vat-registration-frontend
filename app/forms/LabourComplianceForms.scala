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
import models.{IntermediarySupply, SupplyWorkers, Workers}
import play.api.data.Form
import play.api.data.Forms.{mapping, text}

object SupplyWorkersForm extends RequiredBooleanForm {
  val RADIO_YES_NO: String = "value"
  override val errorMsg: String = "validation.supplyWorkers.missing"

  val form = Form(
    mapping(
      RADIO_YES_NO -> requiredBoolean
    )(SupplyWorkers.apply)(SupplyWorkers.unapply)
  )
}

object WorkersForm {
  val NUMBER_OF_WORKERS: String = "numberOfWorkers"
  implicit val errorCode: ErrorCode = "labourCompliance.numberOfWorkers"

  val form = Form(
    mapping(
      NUMBER_OF_WORKERS -> text
        .verifying(mandatoryNumericText)
        .transform[Int](strVal => strVal.toInt, intVal => intVal.toString)
        .verifying("validation.labourCompliance.numberOfWorkers.missing", _ > 0)
    )(Workers.apply)(Workers.unapply)
  )
}

object IntermediarySupplyForm extends RequiredBooleanForm {
  val RADIO_YES_NO: String = "value"
  override val errorMsg: String = "validation.labourCompliance.intermediarySupply.missing"

  val form = Form(
    mapping(
      RADIO_YES_NO -> requiredBoolean
    )(IntermediarySupply.apply)(IntermediarySupply.unapply)
  )
}

