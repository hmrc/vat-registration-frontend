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

package fixtures

import models.S4LVatSicAndCompliance
import models.api.{VatComplianceCultural, VatSicAndCompliance}
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.{BusinessActivityDescription, MainBusinessActivityView}

trait SicAndComplianceFixtures {
  self: VatRegistrationFixture =>

  val validSicAndCompliance = VatSicAndCompliance(
    businessDescription = testBusinessActivityDescription,
    culturalCompliance = Some(VatComplianceCultural(notForProfit = false)),
    labourCompliance = None,
    financialCompliance = None,
    mainBusinessActivity = sicCode
  )

  val s4LVatSicAndCompliance = S4LVatSicAndCompliance(
    description = Some(BusinessActivityDescription(testBusinessActivityDescription)),
    mainBusinessActivity = Some(MainBusinessActivityView(sicCode.id, Some(sicCode))),
    notForProfit = Some(NotForProfit(NotForProfit.NOT_PROFIT_NO)),
    companyProvideWorkers = None,
    workers = None,
    temporaryContracts = None,
    skilledWorkers = None,
    adviceOrConsultancy = None,
    actAsIntermediary = None,
    chargeFees = None,
    leaseVehicles = None,
    additionalNonSecuritiesWork = None,
    discretionaryInvestmentManagementServices = None,
    investmentFundManagement = None,
    manageAdditionalFunds = None
  )
}
