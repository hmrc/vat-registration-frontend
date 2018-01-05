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

package controllers.builders

import features.officers.controllers.routes
import models.api._
import models.view.{SummaryRow, SummarySection}

case class SummaryDirectorAddressesSectionBuilder(vatLodgingOfficer: Option[VatLodgingOfficer] = None)
  extends SummarySectionBuilder {

  override val sectionId: String = "directorAddresses"

  val homeAddress: SummaryRow = SummaryRow(
    s"$sectionId.homeAddress",
    vatLodgingOfficer.flatMap( vlo => vlo.currentAddress.map(a => ScrsAddress.normalisedSeq(a))).getOrElse(Seq.empty),
    Some(routes.OfficerHomeAddressController.show())
  )

  val currentAddressThreeYears: SummaryRow = yesNoRow(
    "currentAddressThreeYears",
    vatLodgingOfficer.flatMap(_.currentOrPreviousAddress.map(_.currentAddressThreeYears)),
    features.officers.controllers.routes.PreviousAddressController.show()
  )
  val previousAddress: SummaryRow = SummaryRow(
    s"$sectionId.previousAddress",
    vatLodgingOfficer.flatMap(_.currentOrPreviousAddress).map(_.previousAddress).collect {
      case Some(address) => ScrsAddress.normalisedSeq(address)
    }.getOrElse(Seq.empty),
    Some(features.officers.controllers.routes.PreviousAddressController.changeAddress())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (homeAddress, vatLodgingOfficer.exists(_.currentAddress.isDefined)),
      (currentAddressThreeYears, true),
      (previousAddress, vatLodgingOfficer.exists(_.currentOrPreviousAddress.exists(_.previousAddress.isDefined)))
    )
  )
}
