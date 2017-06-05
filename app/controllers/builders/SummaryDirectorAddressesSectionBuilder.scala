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

case class SummaryDirectorAddressesSectionBuilder
(
  vatLodgingOfficer: Option[VatLodgingOfficer] = None
)
  extends SummarySectionBuilder {

  import cats.syntax.show._
  import ScrsAddress.htmlShow._

  val homeAddress: SummaryRow = SummaryRow(
    "directorAddresses.homeAddress",
    vatLodgingOfficer.map(_.currentAddress.show).getOrElse(""),
    Some(controllers.vatLodgingOfficer.routes.OfficerHomeAddressController.show())
  )

  val currentAddressThreeYears: SummaryRow = SummaryRow(
    "directorAddresses.currentAddressThreeYears",
    vatLodgingOfficer.map(_.currentOrPreviousAddress.currentAddressThreeYears).collect {
      case true => "app.common.yes"
    }.getOrElse("app.common.no"),
    Some(controllers.vatLodgingOfficer.routes.PreviousAddressController.show())
  )

  val previousAddress: SummaryRow = SummaryRow(
    "directorAddresses.previousAddress",
    vatLodgingOfficer.map(_.currentOrPreviousAddress.previousAddress).collect {
      case Some(address) => address.show
    }.getOrElse(""),
    Some(controllers.vatLodgingOfficer.routes.PreviousAddressController.changeAddress())
  )

  val section: SummarySection = SummarySection(
    id = "directorAddresses",
    Seq(
      (homeAddress, true),
      (currentAddressThreeYears, true),
      (previousAddress, vatLodgingOfficer.exists(_.currentOrPreviousAddress.previousAddress.isDefined))
    )
  )
}
