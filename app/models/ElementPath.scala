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
    override def writes(e: ElementPath): JsValue = JsString(e.name)

    override def reads(json: JsValue): JsResult[ElementPath] = json.as[String] match {
      case VatBankAccountPath.name => JsSuccess(VatBankAccountPath)
      case ZeroRatedTurnoverEstimatePath.name => JsSuccess(ZeroRatedTurnoverEstimatePath)
      case AccountingPeriodStartPath.name => JsSuccess(AccountingPeriodStartPath)
      // $COVERAGE-OFF$
      case FinChargeFeesPath.name => JsSuccess(FinChargeFeesPath)
      case FinAdditionalNonSecuritiesWorkPath.name => JsSuccess(FinAdditionalNonSecuritiesWorkPath)
      case FinDiscretionaryInvestmentManagementServicesPath.name => JsSuccess(FinDiscretionaryInvestmentManagementServicesPath)
      case FinVehicleOrEquipmentLeasingPath.name => JsSuccess(FinVehicleOrEquipmentLeasingPath)
      case FinInvestmentFundManagementServicesPath.name => JsSuccess(FinInvestmentFundManagementServicesPath)
      case FinManageFundsAdditionalPath.name => JsSuccess(FinManageFundsAdditionalPath)
      // $COVERAGE-ON$
      case _ => JsError("unrecognised element name")
    }
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
