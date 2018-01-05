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

case class SummaryDirectorDetailsSectionBuilder(vatLodgingOfficer: Option[VatLodgingOfficer] = None)
  extends SummarySectionBuilder {

  override val sectionId: String = "directorDetails"

  val completionCapacity: SummaryRow = SummaryRow(
    s"$sectionId.completionCapacity",
    vatLodgingOfficer.flatMap(_.name.map(_.asLabel)).getOrElse(""),
    Some(routes.CompletionCapacityController.show())
  )


  val formerName: SummaryRow = SummaryRow(
    s"$sectionId.formerName",
    vatLodgingOfficer.flatMap(_.changeOfName.map(_.nameHasChanged)).collect {
      case true => vatLodgingOfficer.flatMap(
        _.changeOfName
          .flatMap(_.formerName
            .map(_.formerName))).getOrElse("")
    }.getOrElse(s"pages.summary.$sectionId.noFormerName"),
    Some(features.officers.controllers.routes.FormerNameController.show())
  )
  val formerNameDate: SummaryRow = SummaryRow(
    s"$sectionId.formerNameDate",
    vatLodgingOfficer.flatMap(_.changeOfName.map(_.nameHasChanged)).collect {
      case true => vatLodgingOfficer.flatMap(
        _.changeOfName
          .flatMap(_.formerName
            .flatMap(_.dateOfNameChange
              .map(_.format(presentationFormatter))))).getOrElse("")
    }.getOrElse(""),
    Some(features.officers.controllers.routes.FormerNameDateController.show())
  )

  val dob: SummaryRow = SummaryRow(
    s"$sectionId.dob",
    vatLodgingOfficer.flatMap(_.dob.map(_.format(presentationFormatter))).getOrElse(""),
    Some(features.officers.controllers.routes.OfficerSecurityQuestionsController.show())
  )

  val nino: SummaryRow = SummaryRow(
    s"$sectionId.nino",
    vatLodgingOfficer.flatMap(_.nino).getOrElse(""),
    Some(features.officers.controllers.routes.OfficerSecurityQuestionsController.show())
  )

  val email: SummaryRow = SummaryRow(
    s"$sectionId.email",
    vatLodgingOfficer.flatMap(_.contact.flatMap(_.email)).getOrElse(""),
    Some(features.officers.controllers.routes.OfficerContactDetailsController.show())
  )

  val daytimePhone: SummaryRow = SummaryRow(
    s"$sectionId.daytimePhone",
    vatLodgingOfficer.flatMap(_.contact.flatMap(_.tel)).getOrElse(""),
    Some(features.officers.controllers.routes.OfficerContactDetailsController.show())
  )

  val mobile: SummaryRow = SummaryRow(
    s"$sectionId.mobile",
    vatLodgingOfficer.flatMap(_.contact.flatMap(_.mobile)).getOrElse(""),
    Some(features.officers.controllers.routes.OfficerContactDetailsController.show())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (completionCapacity, true),
      (formerName, true),
      (formerNameDate,
        vatLodgingOfficer.flatMap(
          _.changeOfName).map(_.nameHasChanged).getOrElse(false)),
      (dob, true),
      (nino, true),
      (email, vatLodgingOfficer.flatMap(_.contact.map(_.email.isDefined)).getOrElse(false)),
      (daytimePhone, vatLodgingOfficer.flatMap(_.contact.map(_.tel.isDefined)).getOrElse(false)),
      (mobile, vatLodgingOfficer.flatMap(_.contact.map(_.mobile.isDefined)).getOrElse(false))
    )
  )
}
