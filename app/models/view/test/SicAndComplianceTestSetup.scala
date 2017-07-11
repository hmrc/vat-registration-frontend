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

package models.view.test

import play.api.libs.json.Json


case class SicAndComplianceTestSetup(businessActivityDescription: Option[String],
                                     sicCode1: Option[String],
                                     sicCode2: Option[String],
                                     sicCode3: Option[String],
                                     sicCode4: Option[String],
                                     culturalNotForProfit: Option[String],
                                     labourCompanyProvideWorkers: Option[String],
                                     labourWorkers: Option[String],
                                     labourTemporaryContracts: Option[String],
                                     labourSkilledWorkers: Option[String],
                                     financialAdviceOrConsultancy: Option[String],
                                     financialActAsIntermediary: Option[String],
                                     financialChargeFees: Option[String],
                                     financialAdditionalNonSecuritiesWork: Option[String],
                                     financialDiscretionaryInvestment: Option[String],
                                     financialLeaseVehiclesOrEquipment: Option[String],
                                     financialInvestmentFundManagement: Option[String],
                                     financialManageAdditionalFunds: Option[String],
                                     mainBusinessActivityId: Option[String] ,
                                     mainBusinessActivityDescription: Option[String] ,
                                     mainBusinessActivityDisplayDetails: Option[String]
                                    )

object SicAndComplianceTestSetup {
  implicit val format = Json.format[SicAndComplianceTestSetup]
}
