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

case class SummaryDirectorDetailsSectionBuilder(vatLodgingOfficer: Option[VatLodgingOfficer] = None)
  extends SummarySectionBuilder {

  val completionCapacity: SummaryRow = SummaryRow(
    "directorDetails.completionCapacity",
    vatLodgingOfficer.map(_.name.asLabel).getOrElse(""),
    Some(controllers.vatLodgingOfficer.routes.CompletionCapacityController.show())
  )

  val dob: SummaryRow = SummaryRow(
    "directorDetails.dob",
    vatLodgingOfficer.map(_.dob.format(presentationFormatter)).getOrElse(""),
    Some(controllers.vatLodgingOfficer.routes.OfficerDateOfBirthController.show())
  )

  val nino: SummaryRow = SummaryRow(
    "directorDetails.nino",
    vatLodgingOfficer.map(_.nino).getOrElse(""),
    Some(controllers.vatLodgingOfficer.routes.OfficerNinoController.show())
  )

  val email: SummaryRow = SummaryRow(
    "directorDetails.email",
    vatLodgingOfficer.flatMap(_.contact.email).getOrElse(""),
    Some(controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.show())
  )

  val daytimePhone: SummaryRow = SummaryRow(
    "directorDetails.daytimePhone",
    vatLodgingOfficer.flatMap(_.contact.tel).getOrElse(""),
    Some(controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.show())
  )

  val mobile: SummaryRow = SummaryRow(
    "directorDetails.mobile",
    vatLodgingOfficer.flatMap(_.contact.mobile).getOrElse(""),
    Some(controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.show())
  )

  val section: SummarySection = SummarySection(
    id = "directorDetails",
    Seq(
      (completionCapacity, true),
      (dob, true),
      (nino, true),
      (email, vatLodgingOfficer.exists(_.contact.email.isDefined)),
      (daytimePhone, vatLodgingOfficer.exists(_.contact.tel.isDefined)),
      (mobile, vatLodgingOfficer.exists(_.contact.mobile.isDefined))
    )
  )
}
