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

import models.view.{ApplicantDetails, SummaryRow, SummarySection}
import controllers.registration.applicant.{routes => applicantRoutes}

case class SummaryDirectorDetailsSectionBuilder(vatApplicantDetails: ApplicantDetails) extends SummarySectionBuilder {

  override val sectionId: String = "directorDetails"

  val formerName: SummaryRow = SummaryRow(
    s"$sectionId.formerName",
    vatApplicantDetails.formerName.flatMap(_.formerName).getOrElse(s"pages.summary.$sectionId.noFormerName"),
    Some(applicantRoutes.FormerNameController.show())
  )
  val formerNameDate: SummaryRow = SummaryRow(
    s"$sectionId.formerNameDate",
    vatApplicantDetails.formerNameDate.map(_.date.format(presentationFormatter)).getOrElse(""),
    Some(applicantRoutes.FormerNameController.show())
  )

  val email: SummaryRow = SummaryRow(
    s"$sectionId.email",
    vatApplicantDetails.contactDetails.flatMap(_.email).getOrElse(""),
    Some(applicantRoutes.ContactDetailsController.show())
  )

  val daytimePhone: SummaryRow = SummaryRow(
    s"$sectionId.daytimePhone",
    vatApplicantDetails.contactDetails.flatMap(_.tel).getOrElse(""),
    Some(applicantRoutes.ContactDetailsController.show())
  )

  val mobile: SummaryRow = SummaryRow(
    s"$sectionId.mobile",
    vatApplicantDetails.contactDetails.flatMap(_.mobile).getOrElse(""),
    Some(applicantRoutes.ContactDetailsController.show())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (formerName, true),
      (formerNameDate, vatApplicantDetails.formerName.exists(_.yesNo)),
      (email, vatApplicantDetails.contactDetails.flatMap(_.email).isDefined),
      (daytimePhone, vatApplicantDetails.contactDetails.flatMap(_.tel).isDefined),
      (mobile, vatApplicantDetails.contactDetails.flatMap(_.mobile).isDefined)
    )
  )
}
