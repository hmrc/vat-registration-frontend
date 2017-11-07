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
import models.{CurrentProfile, MonthYearModel}
import models.api._
import models.view._
import play.api.mvc._
import services.{CommonService, S4LService, SessionProfile, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.VATRegFeatureSwitch

import scala.concurrent.Future

class SummaryController @Inject()(ds: CommonPlayDependencies, vatRegFeatureSwitch: VATRegFeatureSwitch)
                                 (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with CommonService with SessionProfile {

  def useEligibilityFrontend: Boolean = !vatRegFeatureSwitch.disableEligibilityFrontend.enabled

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          for {
            summary             <- getRegistrationSummary()
            _                   <- s4LService.clear()
            dateOfIncorporation = profile.incorporationDate.fold("")(_.format(MonthYearModel.FORMAT_DD_MMMM_Y))
          } yield Ok(views.html.pages.summary(
            summary,
            dateOfIncorporation
          ))
        }
  }

  def getRegistrationSummary()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Summary] =
    vrs.getVatScheme().map(registrationToSummary)

  def registrationToSummary(vs: VatScheme)(implicit profile : CurrentProfile): Summary =
    Summary(Seq(
      SummaryServiceEligibilitySectionBuilder(vs.vatServiceEligibility, useEligibilityFrontend).section,
      SummaryVatDetailsSectionBuilder(vs.tradingDetails, vs.vatServiceEligibility.flatMap(_.vatEligibilityChoice), useEligibilityFrontend, profile.incorporationDate).section,
      SummaryDirectorDetailsSectionBuilder(vs.lodgingOfficer).section,
      SummaryDirectorAddressesSectionBuilder(vs.lodgingOfficer).section,
      SummaryDoingBusinessAbroadSectionBuilder(vs.tradingDetails).section,
      SummaryCompanyContactDetailsSectionBuilder(vs.vatContact).section,
      SummaryBusinessActivitiesSectionBuilder(vs.vatSicAndCompliance).section,
      SummaryComplianceSectionBuilder(vs.vatSicAndCompliance).section,
      SummaryBusinessBankDetailsSectionBuilder(vs.financials).section,
      SummaryTaxableSalesSectionBuilder(vs.financials).section,
      SummaryAnnualAccountingSchemeSectionBuilder(vs.financials).section,
      SummaryFrsSectionBuilder(vs.vatFlatRateScheme).section
    ))



}
