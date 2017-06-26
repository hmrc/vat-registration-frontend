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

package controllers

import javax.inject.Inject

import controllers.builders._
import models.api._
import models.view._
import play.api.mvc._
import services.{S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class SummaryController @Inject()(ds: CommonPlayDependencies)
                                 (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    for {
      _ <- vrs.submitVatScheme()
      summary <- getRegistrationSummary()
      _ <- s4LService.clear()
    } yield Ok(views.html.pages.summary(summary)))

  def getRegistrationSummary()(implicit hc: HeaderCarrier): Future[Summary] =
    vrs.getVatScheme().map(registrationToSummary)

  def complianceSection(vs: VatScheme): SummarySection =
    List(
      vs.vatSicAndCompliance.flatMap(_.culturalCompliance),
      vs.vatSicAndCompliance.flatMap(_.financialCompliance),
      vs.vatSicAndCompliance.flatMap(_.labourCompliance)
    ).flatten.map {
      case c: VatComplianceCultural => SummaryCulturalComplianceSectionBuilder(vs.vatSicAndCompliance).section
      case c: VatComplianceFinancial => SummaryFinancialComplianceSectionBuilder(vs.vatSicAndCompliance).section
      case c: VatComplianceLabour => SummaryLabourComplianceSectionBuilder(vs.vatSicAndCompliance).section
    }.headOption.getOrElse(SummarySection(id = "none", rows = Seq(), display = false))

  def registrationToSummary(vs: VatScheme): Summary =
    Summary(Seq(
      SummaryVatDetailsSectionBuilder(vs.tradingDetails).section,
      SummaryDirectorDetailsSectionBuilder(vs.lodgingOfficer).section,
      SummaryDirectorAddressesSectionBuilder(vs.lodgingOfficer).section,
      SummaryDoingBusinessAbroadSectionBuilder(vs.tradingDetails).section,
      SummaryCompanyContactDetailsSectionBuilder(vs.vatContact, vs.ppob).section,
      SummaryBusinessActivitiesSectionBuilder(vs.vatSicAndCompliance).section,
      complianceSection(vs),
      SummaryBusinessBankDetailsSectionBuilder(vs.financials).section,
      SummaryTaxableSalesSectionBuilder(vs.financials).section,
      SummaryAnnualAccountingSchemeSectionBuilder(vs.financials).section,
      SummaryServiceEligibilitySectionBuilder(vs.vatServiceEligibility).section,
      SummaryFrsSectionBuilder(vs.vatFlatRateSchemeAnswers).section
    ))

}
