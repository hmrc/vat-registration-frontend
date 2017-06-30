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

import common.StringMasking._
import models.api._
import models.view.{SummaryRow, SummarySection}

case class SummaryBusinessBankDetailsSectionBuilder(vatFinancials: Option[VatFinancials] = None)
  extends SummarySectionBuilder {

  override val sectionId: String = "bankDetails"

  val companyBankAccountRow: SummaryRow = SummaryRow(
    s"$sectionId.companyBankAccount",
    vatFinancials.flatMap(_.bankAccount).fold("app.common.no")(_ => "app.common.yes"),
    Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show())
  )

  val companyBankAccountNameRow: SummaryRow = SummaryRow(
    s"$sectionId.companyBankAccount.name",
    vatFinancials.flatMap(_.bankAccount).fold("app.common.no")(_.accountName),
    Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show())
  )

  val companyBankAccountNumberRow: SummaryRow = SummaryRow(
    s"$sectionId.companyBankAccount.number",
    vatFinancials.flatMap(_.bankAccount).fold("app.common.no")(_.accountNumber.mask(4)),
    Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show())
  )

  val companyBankAccountSortCodeRow: SummaryRow = SummaryRow(
    s"$sectionId.companyBankAccount.sortCode",
    vatFinancials.flatMap(_.bankAccount).fold("app.common.no")(_.accountSortCode),
    Some(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountDetailsController.show())
  )


  val section: SummarySection = SummarySection(
    sectionId,
    Seq(
      (companyBankAccountRow, true),
      (companyBankAccountNameRow, vatFinancials.flatMap(_.bankAccount).isDefined),
      (companyBankAccountNumberRow, vatFinancials.flatMap(_.bankAccount).isDefined),
      (companyBankAccountSortCodeRow, vatFinancials.flatMap(_.bankAccount).isDefined)
    )
  )
}
