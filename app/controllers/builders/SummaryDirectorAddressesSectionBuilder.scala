/*
 * Copyright 2019 HM Revenue & Customs
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

import features.officer.models.view.LodgingOfficer
import models.api.ScrsAddress
import models.view.{SummaryRow, SummarySection}

case class SummaryDirectorAddressesSectionBuilder(vatLodgingOfficer: LodgingOfficer) extends SummarySectionBuilder {

  override val sectionId: String = "directorAddresses"

  val homeAddress: SummaryRow = SummaryRow(
    s"$sectionId.homeAddress",
    vatLodgingOfficer.homeAddress.flatMap(_.address).map(ScrsAddress.normalisedSeq).getOrElse(Seq.empty),
    Some(features.officer.controllers.routes.OfficerController.showHomeAddress())
  )

  val currentAddressThreeYears: SummaryRow = yesNoRow(
    "currentAddressThreeYears",
    vatLodgingOfficer.previousAddress.map(_.yesNo),
    features.officer.controllers.routes.OfficerController.showPreviousAddress()
  )
  val previousAddress: SummaryRow = SummaryRow(
    s"$sectionId.previousAddress",
    vatLodgingOfficer.previousAddress.flatMap(_.address).map(ScrsAddress.normalisedSeq).getOrElse(Seq.empty),
    Some(features.officer.controllers.routes.OfficerController.changePreviousAddress())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (homeAddress, vatLodgingOfficer.homeAddress.exists(_.address.isDefined)),
      (currentAddressThreeYears, true),
      (previousAddress, vatLodgingOfficer.previousAddress.exists(_.address.isDefined))
    )
  )
}
