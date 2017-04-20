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

package forms.test

import models.view.test._
import play.api.data.Form
import play.api.data.Forms._

object TestSetupForm {

  val vatChoiceTestSetupMapping = mapping(
    "taxableTurnoverChoice" -> optional(text),
    "voluntaryChoice" -> optional(text),
    "voluntaryRegistrationReason" -> optional(text),
    "startDateChoice" -> optional(text),
    "startDateDay" -> optional(text),
    "startDateMonth" -> optional(text),
    "startDateYear" -> optional(text)
  )(VatChoiceTestSetup.apply)(VatChoiceTestSetup.unapply)

  val sicAndComplianceTestSetupMapping = mapping(
    "businessActivityDescription" -> optional(text),
    "sicCode1" -> optional(text),
    "sicCode2" -> optional(text),
    "sicCode3" -> optional(text),
    "sicCode4" -> optional(text),
    "culturalNotForProfit" -> optional(text),
    "labourCompanyProvideWorkers" -> optional(text),
    "labourWorkers" -> optional(text),
    "labourTemporaryContracts" -> optional(text),
    "labourSkilledWorkers" -> optional(text),
    "financialAdviceOrConsultancy" -> optional(text),
    "financialActAsIntermediary" -> optional(text),
    "financialChargeFees" -> optional(text),
    "financialAdditionalNonSecuritiesWork" -> optional(text),
    "financialDiscretionaryInvestment" -> optional(text),
    "financialLeaseVehiclesOrEquipment" -> optional(text),
    "financialInvestmentFundManagement" -> optional(text),
    "financialManageAdditionalFunds" ->optional(text)
  )(SicAndComplianceTestSetup.apply)(SicAndComplianceTestSetup.unapply)

  val vatTradingDetailsTestSetupMapping = mapping(
    "tradingNameChoice" -> optional(text),
    "tradingName" -> optional(text),
    "euGoods" -> optional(text),
    "applyEori" -> optional(text)
  )(VatTradingDetailsTestSetup.apply)(VatTradingDetailsTestSetup.unapply)

  val vatContactTestSetupMapping = mapping(
    "email" -> optional(text),
    "daytimePhone" -> optional(text),
    "mobile" -> optional(text),
    "website" -> optional(text)
  )(VatContactTestSetup.apply)(VatContactTestSetup.unapply)

  val vatFinancialsTestSetupMapping = mapping(
    "companyBankAccountChoice" -> optional(text),
    "companyBankAccountName" -> optional(text),
    "companyBankAccountNumber" -> optional(text),
    "sortCode" -> optional(text),
    "estimateVatTurnover" -> optional(text),
    "zeroRatedSalesChoice" -> optional(text),
    "zeroRatedTurnoverEstimate" -> optional(text),
    "vatChargeExpectancyChoice" -> optional(text),
    "vatReturnFrequency" -> optional(text),
    "accountingPeriod" -> optional(text)
  )(VatFinancialsTestSetup.apply)(VatFinancialsTestSetup.unapply)

  val form = Form(mapping(
    "vatChoice" -> vatChoiceTestSetupMapping,
    "vatTradingDetails" -> vatTradingDetailsTestSetupMapping,
    "vatContact" -> vatContactTestSetupMapping,
    "vatFinancials" -> vatFinancialsTestSetupMapping,
    "sicAndCompliance" -> sicAndComplianceTestSetupMapping
  )(TestSetup.apply)(TestSetup.unapply))

}