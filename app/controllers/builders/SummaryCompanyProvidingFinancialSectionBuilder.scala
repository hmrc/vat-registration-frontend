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
import org.apache.commons.lang3.StringUtils

case class SummaryCompanyProvidingFinancialSectionBuilder
(
  vatSicAndCompliance: Option[VatSicAndCompliance] = None
)
  extends SummarySectionBuilder {

/*

## Summary Page Company Providing Financial
pages.summary.companyProvidingFinancial.sectionHeading                                          = Providing financial services
pages.summary.companyProvidingFinancial.provides.advice.or.consultancy                          = Provide ''advice only'' or consultancy services
pages.summary.companyProvidingFinancial.acts.as.intermediary                                    = Act as an intermediary
pages.summary.companyProvidingFinancial.charges.fees                                            = Charge fees for introducing clients to financial service providers
pages.summary.companyProvidingFinancial.does.additional.work.when.introducing.client            = Additional work when introducing a client to a financial service provider
pages.summary.companyProvidingFinancial.provides.discretionary.investment.management            = Provide discretionary investment management services
pages.summary.companyProvidingFinancial.involved.in.leasing.vehicles.or.equipment               = Involved in leasing vehicles or equipment to customers
pages.summary.companyProvidingFinancial.provides.investment.fund.management                     = Provide investment fund management services
pages.summary.companyProvidingFinancial.manages.funds.not.included.in.this.list
 */
  val provideAdviceRow: SummaryRow = SummaryRow(
    "companyProvidingFinancial.provides.advice.or.consultancy",
    vatSicAndCompliance.flatMap( _.financialCompliance.map(_.adviceOrConsultancyOnly)).fold("app.common.no")(_ => "app.common.yes"),
    Some(controllers.vatContact.routes.BusinessContactDetailsController.show())
  )

  val companyBusinessDescriptionRow: SummaryRow = SummaryRow(
    "companyDetails.businessActivity.description",
    vatSicAndCompliance.collect {
      case VatSicAndCompliance(description, _, _, _) if StringUtils.isNotBlank(description) => description
    }.getOrElse("app.common.no"),
    Some(controllers.sicAndCompliance.routes.BusinessActivityDescriptionController.show())
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


  val section: SummarySection = SummarySection(
    id = "companyProvidingFinancial",
    Seq(
      (businessEmailRow, true),
      (businessDaytimePhoneNumberRow, vatContact.exists(_.digitalContact.tel.isDefined)),
      (businessMobilePhoneNumberRow, vatContact.exists(_.digitalContact.mobile.isDefined)),
      (businessWebsiteRow, vatContact.exists(_.website.isDefined))
    )
  )
}
