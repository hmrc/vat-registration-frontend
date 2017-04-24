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

package controllers.builders

import models.api._
import models.view.{SummaryRow, SummarySection}
import org.apache.commons.lang3.StringUtils

case class SummaryCompanyProvidingFinancialSectionBuilder
(
  vatSicAndCompliance: Option[VatSicAndCompliance] = None
)
  extends SummarySectionBuilder {

  val provideAdviceRow: SummaryRow = SummaryRow(
    "companyProvidingFinancial.provides.advice.or.consultancy",
    vatSicAndCompliance.flatMap( _.financialCompliance.map(_.adviceOrConsultancyOnly)).collect {
      case true => "app.common.yes"
    }.getOrElse("app.common.no"),
    Some(controllers.sicAndCompliance.financial.routes.AdviceOrConsultancyController.show())
  )

  val actAsIntermediaryRow: SummaryRow = SummaryRow(
    "companyProvidingFinancial.acts.as.intermediary",
    vatSicAndCompliance.flatMap( _.financialCompliance.map(_.actAsIntermediary)).collect {
    case true => "app.common.yes"
    }.getOrElse("app.common.no"),
    Some(controllers.sicAndCompliance.financial.routes.ActAsIntermediaryController.show())
  )

  val chargesFeesRow: SummaryRow = SummaryRow(
    "companyProvidingFinancial.charges.fees",
    vatSicAndCompliance.flatMap( _.financialCompliance.flatMap(_.chargeFees)).collect {
      case true => "app.common.yes"
    }.getOrElse("app.common.no"),
    Some(controllers.sicAndCompliance.financial.routes.ChargeFeesController.show())
  )

  val additionalWorkRow: SummaryRow = SummaryRow(
    "companyProvidingFinancial.does.additional.work.when.introducing.client",
    vatSicAndCompliance.flatMap( _.financialCompliance.flatMap(_.additionalNonSecuritiesWork)).collect {
      case true => "app.common.yes"
    }.getOrElse("app.common.no"),
    Some(controllers.sicAndCompliance.financial.routes.AdditionalNonSecuritiesWorkController.show())
  )

  val provideDiscretionaryInvestmentRow: SummaryRow = SummaryRow(
    "companyProvidingFinancial.provides.discretionary.investment.management",
    vatSicAndCompliance.flatMap( _.financialCompliance.flatMap(_.discretionaryInvestmentManagementServices)).collect {
      case true => "app.common.yes"
    }.getOrElse("app.common.no"),
    Some(controllers.sicAndCompliance.financial.routes.DiscretionaryInvestmentManagementServicesController.show())
  )

  val leasingVehicleRow: SummaryRow = SummaryRow(
    "companyProvidingFinancial.involved.in.leasing.vehicles.or.equipment",
    vatSicAndCompliance.flatMap( _.financialCompliance.flatMap(_.vehicleOrEquipmentLeasing)).collect {
      case true => "app.common.yes"
    }.getOrElse("app.common.no"),
    Some(controllers.sicAndCompliance.financial.routes.LeaseVehiclesController.show())
  )

  val investmentFundManagementRow: SummaryRow = SummaryRow(
    "companyProvidingFinancial.provides.investment.fund.management",
    vatSicAndCompliance.flatMap( _.financialCompliance.flatMap(_.investmentFundManagementServices)).collect {
      case true => "app.common.yes"
    }.getOrElse("app.common.no"),
    Some(controllers.sicAndCompliance.financial.routes.InvestmentFundManagementController.show())
  )

  val manageAdditionalFundsRow: SummaryRow = SummaryRow(
    "companyProvidingFinancial.manages.funds.not.included.in.this.list",
    vatSicAndCompliance.flatMap( _.financialCompliance.flatMap(_.manageFundsAdditional)).collect {
      case true => "app.common.yes"
    }.getOrElse("app.common.no"),
    Some(controllers.sicAndCompliance.financial.routes.ManageAdditionalFundsController.show())
  )

  val section: SummarySection = SummarySection(
    id = "companyProvidingFinancial",
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
    vatSicAndCompliance.map( _.financialCompliance.isDefined)
  )
}
