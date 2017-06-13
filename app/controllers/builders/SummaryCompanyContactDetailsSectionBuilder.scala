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
import cats.syntax.show._
import ScrsAddress.htmlShow._

case class SummaryCompanyContactDetailsSectionBuilder
(
  vatContact: Option[VatContact] = None,
  ppob: Option[ScrsAddress] = None
)
  extends SummarySectionBuilder {


  val businessEmailRow: SummaryRow = SummaryRow(
    "companyContactDetails.email",
    vatContact.fold("")(_.digitalContact.email),
    Some(controllers.vatContact.routes.BusinessContactDetailsController.show())
  )


  val businessDaytimePhoneNumberRow: SummaryRow = SummaryRow(
    "companyContactDetails.daytimePhone",
    vatContact.flatMap(_.digitalContact.tel).getOrElse(""),
    Some(controllers.vatContact.routes.BusinessContactDetailsController.show())
  )

  val businessMobilePhoneNumberRow: SummaryRow = SummaryRow(
    "companyContactDetails.mobile",
    vatContact.flatMap(_.digitalContact.mobile).getOrElse(""),
    Some(controllers.vatContact.routes.BusinessContactDetailsController.show())
  )


  val businessWebsiteRow: SummaryRow = SummaryRow(
    "companyContactDetails.website",
    vatContact.flatMap(_.website).getOrElse(""),
    Some(controllers.vatContact.routes.BusinessContactDetailsController.show())
  )

  val ppobRow: SummaryRow = SummaryRow(
    "companyContactDetails.ppob",
    ppob.fold("")(_.show),
    Some(controllers.ppob.routes.PpobController.show())
  )


  val section: SummarySection = SummarySection(
    id = "companyContactDetails",
    Seq(
      (businessEmailRow, true),
      (businessDaytimePhoneNumberRow, vatContact.exists(_.digitalContact.tel.isDefined)),
      (businessMobilePhoneNumberRow, vatContact.exists(_.digitalContact.mobile.isDefined)),
      (businessWebsiteRow, vatContact.exists(_.website.isDefined)),
      (ppobRow, ppob.isDefined)
    )
  )
}
