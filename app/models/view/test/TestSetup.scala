/*
 * Copyright 2020 HM Revenue & Customs
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

import models.{BankAccount, FlatRateScheme, Returns, TradingDetails, TurnoverEstimates}
import play.api.libs.json.{Json, OFormat}

case class TestSetup(
                      vatContact: VatContactTestSetup,
                      sicAndCompliance: SicAndComplianceTestSetup,
                      officerHomeAddress: OfficerHomeAddressTestSetup,
                      officerPreviousAddress: OfficerPreviousAddressTestSetup,
                      lodgingOfficer: LodgingOfficerTestSetup,
                      flatRateSchemeBlock: Option[FlatRateScheme],
                      bankAccountBlock: Option[BankAccount],
                      returnsBlock: Option[Returns],
                      tradingDetailsBlock: Option[TradingDetails]
                    )


object TestSetup {
  implicit val format: OFormat[TestSetup] = Json.format[TestSetup]
}
