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

import java.time.LocalDate
import javax.inject.Inject

import controllers.builders._
import models.MonthYearModel
import models.api._
import models.view._
import play.api.mvc._
import services.{S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class SummaryController @Inject()(ds: CommonPlayDependencies)
                                 (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) {

  val dateOfIncorporation = LocalDate.of(2017,5,26)

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    for {
      summary <- getRegistrationSummary()
      _ <- s4LService.clear()
    } yield Ok(views.html.pages.summary(
      summary,
      MonthYearModel.FORMAT_DD_MMMM_Y.format(dateOfIncorporation)
    )))

  def getRegistrationSummary()(implicit hc: HeaderCarrier): Future[Summary] =
    vrs.getVatScheme().map(registrationToSummary)

  def registrationToSummary(vs: VatScheme): Summary =
    Summary(Seq(
      SummaryVatDetailsSectionBuilder(vs.tradingDetails, Some(dateOfIncorporation)).section,
      SummaryDirectorDetailsSectionBuilder(vs.lodgingOfficer).section,
      SummaryDirectorAddressesSectionBuilder(vs.lodgingOfficer).section,
      SummaryDoingBusinessAbroadSectionBuilder(vs.tradingDetails).section,
      SummaryCompanyContactDetailsSectionBuilder(vs.vatContact).section,
      SummaryBusinessActivitiesSectionBuilder(vs.vatSicAndCompliance).section,
      SummaryComplianceSectionBuilder(vs.vatSicAndCompliance).section,
      SummaryBusinessBankDetailsSectionBuilder(vs.financials).section,
      SummaryTaxableSalesSectionBuilder(vs.financials).section,
      SummaryAnnualAccountingSchemeSectionBuilder(vs.financials).section,
      SummaryServiceEligibilitySectionBuilder(vs.vatServiceEligibility).section,
      SummaryFrsSectionBuilder(vs.vatFlatRateScheme).section
    ))

}
