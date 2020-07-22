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

import models.BusinessContact
import models.api.ScrsAddress.htmlShow._
import models.api._
import models.view.{SummaryRow, SummarySection}

case class SummaryCompanyContactDetailsSectionBuilder(businessContact: Option[BusinessContact] = None) extends SummarySectionBuilder {
  override val sectionId: String = "companyContactDetails"

  val businessEmailRow: SummaryRow = SummaryRow(
    s"$sectionId.email",
    businessContact.fold("")(_.companyContactDetails.get.email),
    Some(controllers.routes.BusinessContactDetailsController.showCompanyContactDetails())
  )

  val businessDaytimePhoneNumberRow: SummaryRow = SummaryRow(
    s"$sectionId.daytimePhone",
    businessContact.fold("")(_.companyContactDetails.get.phoneNumber.getOrElse("")),
    Some(controllers.routes.BusinessContactDetailsController.showCompanyContactDetails())
  )

  val businessMobilePhoneNumberRow: SummaryRow = SummaryRow(
    s"$sectionId.mobile",
    businessContact.fold("")(_.companyContactDetails.get.mobileNumber.getOrElse("")),
    Some(controllers.routes.BusinessContactDetailsController.showCompanyContactDetails())
  )


  val businessWebsiteRow: SummaryRow = SummaryRow(
    s"$sectionId.website",
    businessContact.fold("")(_.companyContactDetails.get.websiteAddress.getOrElse("")),
    Some(controllers.routes.BusinessContactDetailsController.showCompanyContactDetails())
  )

  val ppobRow: SummaryRow = SummaryRow(
    s"$sectionId.ppob",
    businessContact.map(bc => ScrsAddress.normalisedSeq(bc.ppobAddress.get)).getOrElse(Seq()),
    Some(controllers.routes.BusinessContactDetailsController.showPPOB())
  )


  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (businessEmailRow, true),
      (businessDaytimePhoneNumberRow, businessContact.exists(_.companyContactDetails.exists(_.phoneNumber.isDefined))),
      (businessMobilePhoneNumberRow, businessContact.exists(_.companyContactDetails.exists(_.mobileNumber.isDefined))),
      (businessWebsiteRow, businessContact.exists(_.companyContactDetails.exists(_.websiteAddress.isDefined))),
      (ppobRow, true)
    )
  )
}
