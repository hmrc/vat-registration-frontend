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

import models.view.ApplicantDetails
import models.api.ScrsAddress
import models.view.{SummaryRow, SummarySection}
import controllers.registration.applicant.{routes => applicantRoutes}

case class SummaryDirectorAddressesSectionBuilder(vatApplicantDetails: ApplicantDetails) extends SummarySectionBuilder {

  override val sectionId: String = "directorAddresses"

  val homeAddress: SummaryRow = SummaryRow(
    s"$sectionId.homeAddress",
    vatApplicantDetails.homeAddress.flatMap(_.address).map(ScrsAddress.normalisedSeq).getOrElse(Seq.empty),
    Some(applicantRoutes.HomeAddressController.show())
  )

  val currentAddressThreeYears: SummaryRow = yesNoRow(
    "currentAddressThreeYears",
    vatApplicantDetails.previousAddress.map(_.yesNo),
    applicantRoutes.PreviousAddressController.show()
  )
  val previousAddress: SummaryRow = SummaryRow(
    s"$sectionId.previousAddress",
    vatApplicantDetails.previousAddress.flatMap(_.address).map(ScrsAddress.normalisedSeq).getOrElse(Seq.empty),
    Some(applicantRoutes.PreviousAddressController.show())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (homeAddress, vatApplicantDetails.homeAddress.exists(_.address.isDefined)),
      (currentAddressThreeYears, true),
      (previousAddress, vatApplicantDetails.previousAddress.exists(_.address.isDefined))
    )
  )
}
