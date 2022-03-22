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

object SupplyWorkersForm {
  val RADIO_YES_NO: String = "value"
  val defaultErrorCode: String = "labourCompliance.supplyWorkers"
  val thirdPartyErrorCode: String = "labourCompliance.supplyWorkers.3pt"

  def form(isTransactor: Boolean): Form[SupplyWorkers] = {
    implicit val errorCode: ErrorCode = if(isTransactor) thirdPartyErrorCode else defaultErrorCode
    Form(
      mapping(
        RADIO_YES_NO -> missingBooleanFieldMapping()
      )(SupplyWorkers.apply)(SupplyWorkers.unapply)
    )
  }
}

object WorkersForm {
  val NUMBER_OF_WORKERS: String = "numberOfWorkers"
  val defaultErrorCode: String = "labourCompliance.numberOfWorkers"
  val thirdPartyErrorCode: String = "labourCompliance.numberOfWorkers.3pt"

  def form(isTransactor: Boolean): Form[Workers] = {
    implicit val errorCode: ErrorCode = if(isTransactor) thirdPartyErrorCode else defaultErrorCode
    Form(
      mapping(
        NUMBER_OF_WORKERS -> text
          .verifying(mandatoryNumericText)
          .transform[Int](strVal => strVal.toInt, intVal => intVal.toString)
          .verifying(_ > 0)
      )(Workers.apply)(Workers.unapply)
    )
  }
}

case class IntermediarySupplyForm(name: Option[String]) extends RequiredBooleanForm {
  val RADIO_YES_NO: String = "value"
  override val errorMsg: String = if(name.isDefined) "validation.labourCompliance.intermediarySupply.3pt.missing" else "validation.labourCompliance.intermediarySupply.missing"
  override lazy val errorMsgArgs: Seq[Any] = if(name.isDefined) Array(name.get) else Nil

  val form = Form(
    mapping(
      RADIO_YES_NO -> requiredBoolean
    )(IntermediarySupply.apply)(IntermediarySupply.unapply)
  )
}