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

import javax.inject.{Inject, Singleton}

import connectors.{KeystoreConnect, Success}
import common.enums.VatRegStatus
import controllers.builders._
import models.api._
import models.view._
import models.{CurrentProfile, MonthYearModel}
import play.api.mvc._
import services._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.VATRegFeatureSwitches

import scala.concurrent.Future

@Singleton
class SummaryController @Inject()(ds: CommonPlayDependencies,
                                  vatRegFeatureSwitch: VATRegFeatureSwitches,
                                  vrs: RegistrationService,
                                  val keystoreConnector: KeystoreConnect,
                                  val authConnector: AuthConnector,
                                  implicit val s4LService: S4LService) extends VatRegistrationController(ds) with SessionProfile {

  def useEligibilityFrontend: Boolean = !vatRegFeatureSwitch.disableEligibilityFrontend.enabled

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              summary <- getRegistrationSummary()
              _       <- s4LService.clear
              dateOfIncorporation = profile.incorporationDate.fold("")(_.format(MonthYearModel.FORMAT_DD_MMMM_Y))
            } yield Ok(views.html.pages.summary(summary, dateOfIncorporation))
          }
        }
  }

  def getRegistrationSummary()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Summary] = vrs.getVatScheme.map(registrationToSummary)

  def submitRegistration: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
      withCurrentProfile { implicit profile =>
        ivPassedCheck {
          invalidSubmissionGuard() {
            vrs.submitRegistration() map {
              case Success => Redirect(controllers.routes.ApplicationSubmissionController.show())
            }
          }
        }
      }
  }

  def registrationToSummary(vs: VatScheme)(implicit profile : CurrentProfile): Summary = {
    Summary(Seq(
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

  private[controllers] def invalidSubmissionGuard()(f: => Future[Result])(implicit hc: HeaderCarrier, profile: CurrentProfile) = {
    vrs.getStatus(profile.registrationId) flatMap {
      case VatRegStatus.draft => f
      case _ => Future.successful(InternalServerError)
    }
  }
}
