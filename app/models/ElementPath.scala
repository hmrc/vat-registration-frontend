/*
 * Copyright 2017 HM Revenue & Customs
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

package models

import play.api.libs.json._

sealed trait ElementPath {
  val path: String
  val name: String
}

object ElementPath {

  implicit object ElementPathFormatter extends Format[ElementPath] {

    private val pathMap: Map[String, ElementPath] = Seq[ElementPath](
      VatBankAccountPath,
      ZeroRatedTurnoverEstimatePath,
      AccountingPeriodStartPath,
      CulturalCompliancePath,
      LabourCompliancePath,
      FinancialCompliancePath,
      FinChargeFeesPath,
      FinAdditionalNonSecuritiesWorkPath,
      FinDiscretionaryInvestmentManagementServicesPath,
      FinVehicleOrEquipmentLeasingPath,
      FinInvestmentFundManagementServicesPath,
      FinManageFundsAdditionalPath
    ).map(ep => (ep.name, ep)).toMap

    override def writes(e: ElementPath): JsValue = JsString(e.name)

    override def reads(json: JsValue): JsResult[ElementPath] =
      pathMap.get(json.as[String]).fold[JsResult[ElementPath]](JsError("unrecognised element name"))(ep => JsSuccess(ep))

  }

  // $COVERAGE-OFF$
  val finCompElementPaths: List[ElementPath] =
    List(FinChargeFeesPath, FinAdditionalNonSecuritiesWorkPath,
      FinDiscretionaryInvestmentManagementServicesPath, FinVehicleOrEquipmentLeasingPath,
      FinInvestmentFundManagementServicesPath, FinManageFundsAdditionalPath)
  // $COVERAGE-ON$
}

case object VatBankAccountPath extends ElementPath {
  override val path = "financials.bankAccount"
  override val name = "vat-bank-account"
}

case object ZeroRatedTurnoverEstimatePath extends ElementPath {
  override val path = "financials.zeroRatedTurnoverEstimate"
  override val name = "zero-rated-turnover-estimate"
}

case object AccountingPeriodStartPath extends ElementPath {
  override val path = "financials.accountingPeriods.periodStart"
  override val name = "accounting-period-start"
}

// $COVERAGE-OFF$

case object CulturalCompliancePath extends ElementPath {
  override val path = "vatSicAndCompliance.culturalCompliance"
  override val name = "cultural-compliance"
}

case object LabourCompliancePath extends ElementPath {
  override val path = "vatSicAndCompliance.labourCompliance"
  override val name = "labour-compliance"
}

case object FinancialCompliancePath extends ElementPath {
  override val path = "vatSicAndCompliance.financialCompliance"
  override val name = "financial-compliance"
}

case object FinChargeFeesPath extends ElementPath {
  override val path = "vatSicAndCompliance.financialCompliance.chargeFees"
  override val name = "fc-charge-fees"
}

case object FinAdditionalNonSecuritiesWorkPath extends ElementPath {
  override val path = "vatSicAndCompliance.financialCompliance.additionalNonSecuritiesWork"
  override val name = "fc-additional-non-securities-work"
}

case object FinDiscretionaryInvestmentManagementServicesPath extends ElementPath {
  override val path = "vatSicAndCompliance.financialCompliance.discretionaryInvestmentManagementServices"
  override val name = "fc-discretionary-investment-management-services"
}

case object FinVehicleOrEquipmentLeasingPath extends ElementPath {
  override val path = "vatSicAndCompliance.financialCompliance.vehicleOrEquipmentLeasing"
  override val name = "fc-vehicle-or-equipment-leasing"
}

case object FinInvestmentFundManagementServicesPath extends ElementPath {
  override val path = "vatSicAndCompliance.financialCompliance.investmentFundManagementServices"
  override val name = "fc-investment-fund-management-services"
}

case object FinManageFundsAdditionalPath extends ElementPath {
  override val path = "vatSicAndCompliance.financialCompliance.manageFundsAdditional"
  override val name = "fc-manage-funds-additional"
}

// $COVERAGE-ON$
