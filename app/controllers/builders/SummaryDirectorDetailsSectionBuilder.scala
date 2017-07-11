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

  override val sectionId: String = "directorDetails"

  val completionCapacity: SummaryRow = SummaryRow(
    s"$sectionId.completionCapacity",
    vatLodgingOfficer.map(_.name.asLabel).getOrElse(""),
    Some(controllers.vatLodgingOfficer.routes.CompletionCapacityController.show())
  )


  val formerName: SummaryRow = SummaryRow(
    s"$sectionId.formerName",
    vatLodgingOfficer.map(_.changeOfName.nameHasChanged).collect {
      case true => vatLodgingOfficer.flatMap(_.changeOfName.formerName).map(_.formerName).getOrElse("")
    }.getOrElse(s"pages.summary.$sectionId.noFormerName"),
    Some(controllers.vatLodgingOfficer.routes.FormerNameController.show())
  )

  val dob: SummaryRow = SummaryRow(
    s"$sectionId.dob",
    vatLodgingOfficer.map(_.dob.format(presentationFormatter)).getOrElse(""),
    Some(controllers.vatLodgingOfficer.routes.OfficerDateOfBirthController.show())
  )

  val nino: SummaryRow = SummaryRow(
    s"$sectionId.nino",
    vatLodgingOfficer.map(_.nino).getOrElse(""),
    Some(controllers.vatLodgingOfficer.routes.OfficerNinoController.show())
  )

  val email: SummaryRow = SummaryRow(
    s"$sectionId.email",
    vatLodgingOfficer.flatMap(_.contact.email).getOrElse(""),
    Some(controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.show())
  )

  val daytimePhone: SummaryRow = SummaryRow(
    s"$sectionId.daytimePhone",
    vatLodgingOfficer.flatMap(_.contact.tel).getOrElse(""),
    Some(controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.show())
  )

  val mobile: SummaryRow = SummaryRow(
    s"$sectionId.mobile",
    vatLodgingOfficer.flatMap(_.contact.mobile).getOrElse(""),
    Some(controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.show())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (completionCapacity, true),
      (formerName, true),
      (dob, true),
      (nino, true),
      (email, vatLodgingOfficer.exists(_.contact.email.isDefined)),
      (daytimePhone, vatLodgingOfficer.exists(_.contact.tel.isDefined)),
      (mobile, vatLodgingOfficer.exists(_.contact.mobile.isDefined))
    )
  )
}
