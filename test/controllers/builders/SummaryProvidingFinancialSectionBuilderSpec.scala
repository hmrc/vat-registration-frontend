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

import java.time.LocalDate

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.{VatChoice, VatComplianceFinancial, VatSicAndCompliance}
import models.view.SummaryRow
import models.view.vatTradingDetails.vatChoice.StartDateView

class SummaryProvidingFinancialSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  val defaultFinancialCompliance = VatComplianceFinancial(adviceOrConsultancyOnly = false,
                                    actAsIntermediary = false,
                                    additionalNonSecuritiesWork = Some(false),
                                    vehicleOrEquipmentLeasing = Some(false),
                                    chargeFees = Some(false),
                                    discretionaryInvestmentManagementServices = Some(false),
                                    investmentFundManagementServices = Some(false),
                                    manageFundsAdditional = Some(false)
                                    )

  val testVatSicAndCompliance = Some(VatSicAndCompliance(businessDescription ="TEST"))

  "The section builder composing a financial details section" should {


    "with provideAdviceRow render" should {

      " 'No' selected provideAdviceRow " in {
        val builder = SummaryCompanyProvidingFinancialSectionBuilder()
        builder.provideAdviceRow mustBe
          SummaryRow(
            "companyProvidingFinancial.provides.advice.or.consultancy",
            "app.common.no",
            Some(controllers.sicAndCompliance.financial.routes.AdviceOrConsultancyController.show())
          )
      }


      " 'YES' selected for provideAdviceRow" in {
        val compliance = VatSicAndCompliance("Business Described", financialCompliance = Some(defaultFinancialCompliance.copy(adviceOrConsultancyOnly=true)))
        val builder = SummaryCompanyDetailsSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "companyProvidingFinancial.provides.advice.or.consultancy",
            "app.common.yes",
            Some(controllers.sicAndCompliance.financial.routes.AdviceOrConsultancyController.show())
          )
      }
    }


    "with actAsIntermediaryRow render" should {

      " 'No' selected actAsIntermediaryRow " in {
        val builder = SummaryCompanyProvidingFinancialSectionBuilder()
        builder.provideAdviceRow mustBe
          SummaryRow(
            "companyProvidingFinancial.acts.as.intermediary",
            "app.common.no",
            Some(controllers.sicAndCompliance.financial.routes.ActAsIntermediaryController.show())
          )
      }


      " 'YES' selected for actAsIntermediaryRow" in {
        val compliance = VatSicAndCompliance("Business Described", financialCompliance = Some(defaultFinancialCompliance.copy(actAsIntermediary=true)))
        val builder = SummaryCompanyDetailsSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "companyProvidingFinancial.acts.as.intermediary",
            "app.common.yes",
            Some(controllers.sicAndCompliance.financial.routes.ActAsIntermediaryController.show())
          )
      }
    }


    "with chargesFeesRow render" should {

      " 'No' selected chargesFeesRow " in {
        val builder = SummaryCompanyProvidingFinancialSectionBuilder()
        builder.provideAdviceRow mustBe
          SummaryRow(
            "companyProvidingFinancial.charges.fees",
            "app.common.no",
            Some(controllers.sicAndCompliance.financial.routes.ChargeFeesController.show())
          )
      }


      " 'YES' selected for chargesFeesRow" in {
        val compliance = VatSicAndCompliance("Business Described", financialCompliance = Some(defaultFinancialCompliance.copy(chargeFees=Some(true))))
        val builder = SummaryCompanyDetailsSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "companyProvidingFinancial.charges.fees",
            "app.common.yes",
            Some(controllers.sicAndCompliance.financial.routes.ChargeFeesController.show())
          )
      }
    }

    "with additionalWorkRow render" should {

      " 'No' selected additionalWorkRow " in {
        val builder = SummaryCompanyProvidingFinancialSectionBuilder()
        builder.provideAdviceRow mustBe
          SummaryRow(
            "companyProvidingFinancial.does.additional.work.when.introducing.client",
            "app.common.no",
            Some(controllers.sicAndCompliance.financial.routes.AdditionalNonSecuritiesWorkController.show())
          )
      }


      " 'YES' selected for additionalWorkRow" in {
        val compliance = VatSicAndCompliance("Business Described", financialCompliance = Some(defaultFinancialCompliance.copy(additionalNonSecuritiesWork=Some(true))))
        val builder = SummaryCompanyDetailsSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "companyProvidingFinancial.does.additional.work.when.introducing.client",
            "app.common.yes",
            Some(controllers.sicAndCompliance.financial.routes.AdditionalNonSecuritiesWorkController.show())
          )
      }
    }


    "with provideDiscretionaryInvestmentRow render" should {

      " 'No' selected provideDiscretionaryInvestmentRow " in {
        val builder = SummaryCompanyProvidingFinancialSectionBuilder()
        builder.provideAdviceRow mustBe
          SummaryRow(
            "companyProvidingFinancial.provides.discretionary.investment.management",
            "app.common.no",
            Some(controllers.sicAndCompliance.financial.routes.DiscretionaryInvestmentManagementServicesController.show())
          )
      }


      " 'YES' selected for provideDiscretionaryInvestmentRow" in {
        val compliance = VatSicAndCompliance("Business Described", financialCompliance = Some(defaultFinancialCompliance.copy(discretionaryInvestmentManagementServices=Some(true))))
        val builder = SummaryCompanyDetailsSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "companyProvidingFinancial.provides.discretionary.investment.management",
            "app.common.yes",
            Some(controllers.sicAndCompliance.financial.routes.DiscretionaryInvestmentManagementServicesController.show())
          )
      }
    }

    "with leasingVehicleRow render" should {

      " 'No' selected leasingVehicleRow " in {
        val builder = SummaryCompanyProvidingFinancialSectionBuilder()
        builder.provideAdviceRow mustBe
          SummaryRow(
            "companyProvidingFinancial.involved.in.leasing.vehicles.or.equipment",
            "app.common.no",
            Some(controllers.sicAndCompliance.financial.routes.LeaseVehiclesController.show())
          )
      }


      " 'YES' selected for leasingVehicleRow" in {
        val compliance = VatSicAndCompliance("Business Described", financialCompliance = Some(defaultFinancialCompliance.copy(vehicleOrEquipmentLeasing=Some(true))))
        val builder = SummaryCompanyDetailsSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "companyProvidingFinancial.involved.in.leasing.vehicles.or.equipment",
            "app.common.yes",
            Some(controllers.sicAndCompliance.financial.routes.LeaseVehiclesController.show())
          )
      }
    }

    "with investmentFundManagementRow render" should {

      " 'No' selected investmentFundManagementRow " in {
        val builder = SummaryCompanyProvidingFinancialSectionBuilder()
        builder.provideAdviceRow mustBe
          SummaryRow(
            "companyProvidingFinancial.provides.investment.fund.management",
            "app.common.no",
            Some(controllers.sicAndCompliance.financial.routes.InvestmentFundManagementController.show())
          )
      }


      " 'YES' selected for investmentFundManagementRow" in {
        val compliance = VatSicAndCompliance("Business Described", financialCompliance = Some(defaultFinancialCompliance.copy(investmentFundManagementServices=Some(true))))
        val builder = SummaryCompanyDetailsSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "companyProvidingFinancial.provides.investment.fund.management",
            "app.common.yes",
            Some(controllers.sicAndCompliance.financial.routes.InvestmentFundManagementController.show())
          )
      }
    }

    "with manageAdditionalFundsRow render" should {

      " 'No' selected manageAdditionalFundsRow " in {
        val builder = SummaryCompanyProvidingFinancialSectionBuilder()
        builder.provideAdviceRow mustBe
          SummaryRow(
            "companyProvidingFinancial.manages.funds.not.included.in.this.list",
            "app.common.no",
            Some(controllers.sicAndCompliance.financial.routes.ManageAdditionalFundsController.show())
          )
      }


      " 'YES' selected for manageAdditionalFundsRow" in {
        val compliance = VatSicAndCompliance("Business Described", financialCompliance = Some(defaultFinancialCompliance.copy(manageFundsAdditional=Some(true))))
        val builder = SummaryCompanyDetailsSectionBuilder(vatSicAndCompliance = Some(compliance))
        builder.companyBusinessDescriptionRow mustBe
          SummaryRow(
            "companyProvidingFinancial.manages.funds.not.included.in.this.list",
            "app.common.yes",
            Some(controllers.sicAndCompliance.financial.routes.ManageAdditionalFundsController.show())
          )
      }
    }

  }
}
