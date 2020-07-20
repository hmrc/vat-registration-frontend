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

package controllers.builders

import features.sicAndCompliance.models.{CompanyProvideWorkers, SicAndCompliance, SkilledWorkers, TemporaryContracts}
import models.view.{SummaryRow, SummarySection}

case class SummaryLabourComplianceSectionBuilder(vatSicAndCompliance: Option[SicAndCompliance] = None) extends SummarySectionBuilder {

  val sectionId = "labourCompliance"

  val providingWorkersRow: SummaryRow = yesNoRow(
    "providesWorkers",
    vatSicAndCompliance.flatMap(_.companyProvideWorkers).flatMap(v => CompanyProvideWorkers.toBool(v.yesNo)),
    features.sicAndCompliance.controllers.routes.LabourComplianceController.showProvideWorkers()
  )

  val numberOfWorkersRow: SummaryRow = SummaryRow(
    s"$sectionId.numberOfWorkers",
    vatSicAndCompliance.flatMap(_.workers).fold("")(_.numberOfWorkers.toString),
    Some(features.sicAndCompliance.controllers.routes.LabourComplianceController.showWorkers())
  )

  val temporaryContractsRow: SummaryRow = yesNoRow(
    "workersOnTemporaryContracts",
    vatSicAndCompliance.flatMap(_.temporaryContracts).flatMap(v => TemporaryContracts.toBool(v.yesNo)),
    features.sicAndCompliance.controllers.routes.LabourComplianceController.showTemporaryContracts()
  )

  val skilledWorkersRow: SummaryRow = yesNoRow(
    "providesSkilledWorkers",
    vatSicAndCompliance.flatMap(_.skilledWorkers).flatMap(v => SkilledWorkers.toBool(v.yesNo)),
    features.sicAndCompliance.controllers.routes.LabourComplianceController.showSkilledWorkers()
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (providingWorkersRow, vatSicAndCompliance.flatMap(_.companyProvideWorkers).isDefined),
      (numberOfWorkersRow,vatSicAndCompliance.flatMap(_.workers).isDefined),
      (temporaryContractsRow, vatSicAndCompliance.flatMap(_.temporaryContracts).isDefined),
      (skilledWorkersRow, vatSicAndCompliance.flatMap(_.skilledWorkers).isDefined)
    ),
    vatSicAndCompliance.flatMap(_.companyProvideWorkers).isDefined
  )
}
