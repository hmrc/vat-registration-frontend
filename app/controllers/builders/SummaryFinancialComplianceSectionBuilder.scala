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

package controllers.builders

import models.api._
import models.view.{SummaryRow, SummarySection}

case class SummaryFinancialComplianceSectionBuilder(vatSicAndCompliance: Option[VatSicAndCompliance] = None)
  extends SummarySectionBuilder {

  override val sectionId: String = "financialCompliance"

  val financialCompliance = vatSicAndCompliance.flatMap(_.financialCompliance)

  val provideAdviceRow: SummaryRow = yesNoRow(
    "provides.advice.or.consultancy",
    financialCompliance.map(_.adviceOrConsultancyOnly),
    controllers.sicAndCompliance.financial.routes.AdviceOrConsultancyController.show()
  )

  val actAsIntermediaryRow: SummaryRow = yesNoRow(
    "acts.as.intermediary",
    financialCompliance.map(_.actAsIntermediary),
    controllers.sicAndCompliance.financial.routes.ActAsIntermediaryController.show()
  )

  val chargesFeesRow: SummaryRow = yesNoRow(
    "charges.fees",
    financialCompliance.flatMap(_.chargeFees),
    controllers.sicAndCompliance.financial.routes.ChargeFeesController.show()
  )

  val additionalWorkRow: SummaryRow = yesNoRow(
    "does.additional.work.when.introducing.client",
    financialCompliance.flatMap(_.additionalNonSecuritiesWork),
    controllers.sicAndCompliance.financial.routes.AdditionalNonSecuritiesWorkController.show()
  )

  val provideDiscretionaryInvestmentRow: SummaryRow = yesNoRow(
    "provides.discretionary.investment.management",
    financialCompliance.flatMap(_.discretionaryInvestmentManagementServices),
    controllers.sicAndCompliance.financial.routes.DiscretionaryInvestmentManagementServicesController.show()
  )

  val leasingVehicleRow: SummaryRow = yesNoRow(
    "involved.in.leasing.vehicles.or.equipment",
    financialCompliance.flatMap(_.vehicleOrEquipmentLeasing),
    controllers.sicAndCompliance.financial.routes.LeaseVehiclesController.show()
  )

  val investmentFundManagementRow: SummaryRow = yesNoRow(
    "provides.investment.fund.management",
    financialCompliance.flatMap(_.investmentFundManagementServices),
    controllers.sicAndCompliance.financial.routes.InvestmentFundManagementController.show()
  )

  val manageAdditionalFundsRow: SummaryRow = yesNoRow(
    "manages.funds.not.included.in.this.list",
    financialCompliance.flatMap(_.manageFundsAdditional),
    controllers.sicAndCompliance.financial.routes.ManageAdditionalFundsController.show()
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (provideAdviceRow, true),
      (actAsIntermediaryRow, true),
      (chargesFeesRow, vatSicAndCompliance.exists(_.financialCompliance.flatMap(_.chargeFees).isDefined)),
      (additionalWorkRow, vatSicAndCompliance.exists(_.financialCompliance.flatMap(_.additionalNonSecuritiesWork).isDefined)),
      (provideDiscretionaryInvestmentRow, vatSicAndCompliance.exists(_.financialCompliance.flatMap(_.discretionaryInvestmentManagementServices).isDefined)),
      (leasingVehicleRow, vatSicAndCompliance.exists(_.financialCompliance.flatMap(_.vehicleOrEquipmentLeasing).isDefined)),
      (investmentFundManagementRow, vatSicAndCompliance.exists(_.financialCompliance.flatMap(_.investmentFundManagementServices).isDefined)),
      (manageAdditionalFundsRow, vatSicAndCompliance.exists(_.financialCompliance.flatMap(_.manageFundsAdditional).isDefined))
    ),
    vatSicAndCompliance.flatMap(_.financialCompliance).isDefined
  )
}
