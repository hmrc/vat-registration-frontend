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

package models.api

import play.api.libs.json._

sealed trait VatCompliance extends Product with Serializable

case class VatComplianceCultural(notForProfit: Boolean) extends VatCompliance

object VatComplianceCultural {

  implicit val format: OFormat[VatComplianceCultural] = Json.format[VatComplianceCultural]

}

case class VatComplianceFinancial(adviceOrConsultancyOnly: Boolean,
                                  actAsIntermediary: Boolean,
                                  chargeFees: Option[Boolean] = None,
                                  additionalNonSecuritiesWork: Option[Boolean] = None,
                                  discretionaryInvestmentManagementServices: Option[Boolean] = None,
                                  vehicleOrEquipmentLeasing: Option[Boolean] = None,
                                  investmentFundManagementServices: Option[Boolean] = None,
                                  manageFundsAdditional: Option[Boolean] = None
                                 ) extends VatCompliance

object VatComplianceFinancial {

  implicit val format: OFormat[VatComplianceFinancial] = Json.format[VatComplianceFinancial]

}

case class VatComplianceLabour(labour: Boolean,
                               workers: Option[Int] = None,
                               temporaryContracts: Option[Boolean] = None,
                               skilledWorkers: Option[Boolean] = None
                              ) extends VatCompliance

object VatComplianceLabour {

  implicit val format: OFormat[VatComplianceLabour] = Json.format[VatComplianceLabour]

}
