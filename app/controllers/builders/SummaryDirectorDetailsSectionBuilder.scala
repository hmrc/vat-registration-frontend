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
import models.view.{SummaryRow, SummarySection}

case class SummaryDirectorDetailsSectionBuilder(vatLodgingOfficer: LodgingOfficer) extends SummarySectionBuilder {

  override val sectionId: String = "directorDetails"

  val formerName: SummaryRow = SummaryRow(
    s"$sectionId.formerName",
    vatLodgingOfficer.formerName.flatMap(_.formerName).getOrElse(s"pages.summary.$sectionId.noFormerName"),
    Some(features.officer.controllers.routes.OfficerController.showFormerName())
  )
  val formerNameDate: SummaryRow = SummaryRow(
    s"$sectionId.formerNameDate",
    vatLodgingOfficer.formerNameDate.map(_.date.format(presentationFormatter)).getOrElse(""),
    Some(features.officer.controllers.routes.OfficerController.showFormerNameDate())
  )

  val dob: SummaryRow = SummaryRow(
    s"$sectionId.dob",
    vatLodgingOfficer.securityQuestions.map(_.dob.format(presentationFormatter)).getOrElse(""),
    Some(features.officer.controllers.routes.OfficerController.showSecurityQuestions())
  )

  val email: SummaryRow = SummaryRow(
    s"$sectionId.email",
    vatLodgingOfficer.contactDetails.flatMap(_.email).getOrElse(""),
    Some(features.officer.controllers.routes.OfficerController.showContactDetails())
  )

  val daytimePhone: SummaryRow = SummaryRow(
    s"$sectionId.daytimePhone",
    vatLodgingOfficer.contactDetails.flatMap(_.daytimePhone).getOrElse(""),
    Some(features.officer.controllers.routes.OfficerController.showContactDetails())
  )

  val mobile: SummaryRow = SummaryRow(
    s"$sectionId.mobile",
    vatLodgingOfficer.contactDetails.flatMap(_.mobile).getOrElse(""),
    Some(features.officer.controllers.routes.OfficerController.showContactDetails())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (formerName, true),
      (formerNameDate, vatLodgingOfficer.formerName.exists(_.yesNo)),
      (dob, true),
      (email, vatLodgingOfficer.contactDetails.flatMap(_.email).isDefined),
      (daytimePhone, vatLodgingOfficer.contactDetails.flatMap(_.daytimePhone).isDefined),
      (mobile, vatLodgingOfficer.contactDetails.flatMap(_.mobile).isDefined)
    )
  )
}
