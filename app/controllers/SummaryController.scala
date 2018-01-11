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

package controllers

import javax.inject.{Inject, Singleton}

import common.enums.VatRegStatus
import connectors.{KeystoreConnect, Success}
import controllers.builders._
import features.returns.{Returns, ReturnsService}
import features.officer.services.LodgingOfficerService
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
                                  val returnsService: ReturnsService,
                                  val keystoreConnector: KeystoreConnect,
                                  val authConnector: AuthConnector,
                                  val lodgingOfficerService: LodgingOfficerService,
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

  def getRegistrationSummary()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Summary] = {
    for {
      officer <- lodgingOfficerService.getLodgingOfficer
      summary <- vrs.getVatScheme.map(scheme => registrationToSummary(scheme.copy(lodgingOfficer = Some(officer))))
    } yield summary
  }

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
      SummaryVatDetailsSectionBuilder(
        vs.tradingDetails,
        vs.vatServiceEligibility.flatMap(_.vatEligibilityChoice),
        vs.returns,
        useEligibilityFrontend,
        profile.incorporationDate
      ).section,
      SummaryDirectorDetailsSectionBuilder(vs.lodgingOfficer.getOrElse(throw new IllegalStateException("Missing Lodging Officer data to show summary"))).section,
      SummaryDirectorAddressesSectionBuilder(vs.lodgingOfficer.getOrElse(throw new IllegalStateException("Missing Lodging Officer data to show summary"))).section,
      SummaryDoingBusinessAbroadSectionBuilder(vs.tradingDetails).section,
      SummaryCompanyContactDetailsSectionBuilder(vs.vatContact).section,
      SummaryBusinessActivitiesSectionBuilder(vs.vatSicAndCompliance).section,
      SummaryComplianceSectionBuilder(vs.vatSicAndCompliance).section,
      SummaryBusinessBankDetailsSectionBuilder(vs.bankAccount).section,
      SummaryAnnualAccountingSchemeSectionBuilder(vs.returns).section,
      SummaryTaxableSalesSectionBuilder(vs.financials,vs.turnOverEstimates).section,

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
