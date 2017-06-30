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

case class SummaryLabourComplianceSectionBuilder
(
  vatSicAndCompliance: Option[VatSicAndCompliance] = None
)
  extends SummarySectionBuilder {

  val sectionId = "labourCompliance"

  val labourCompliance = vatSicAndCompliance.flatMap(_.labourCompliance)

  val providingWorkersRow: SummaryRow = yesNoRow(
    "providesWorkers",
    labourCompliance.map(_.labour),
    controllers.sicAndCompliance.labour.routes.CompanyProvideWorkersController.show()
  )

  val numberOfWorkersRow: SummaryRow = SummaryRow(
    s"$sectionId.numberOfWorkers",
    labourCompliance.flatMap(_.workers).getOrElse(0).toString,
    Some(controllers.sicAndCompliance.labour.routes.WorkersController.show())
  )

  val temporaryContractsRow: SummaryRow = yesNoRow(
    "workersOnTemporaryContracts",
    labourCompliance.flatMap(_.temporaryContracts),
    controllers.sicAndCompliance.labour.routes.TemporaryContractsController.show()
  )

  val skilledWorkersRow: SummaryRow = yesNoRow(
    "providesSkilledWorkers",
    labourCompliance.flatMap(_.skilledWorkers),
    controllers.sicAndCompliance.labour.routes.SkilledWorkersController.show()
  )


  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (providingWorkersRow, true),
      (numberOfWorkersRow, labourCompliance.flatMap(_.workers).exists(_ > 0)),
      (temporaryContractsRow, labourCompliance.flatMap(_.temporaryContracts).isDefined),
      (skilledWorkersRow, labourCompliance.flatMap(_.skilledWorkers).isDefined)
    ),
    vatSicAndCompliance.flatMap(_.labourCompliance).isDefined
  )
}
