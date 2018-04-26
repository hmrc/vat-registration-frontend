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

import javax.inject.Inject

import cats.syntax.ApplicativeSyntax
import common.enums.VatRegStatus
import config.AuthClientConnector
import connectors._
import controllers.builders._
import features.frs.services.FlatRateService
import features.officer.services.LodgingOfficerService
import features.sicAndCompliance.services.SicAndComplianceService
import models.CurrentProfile
import models.api._
import models.view._
import play.api.i18n.MessagesApi
import play.api.mvc._
import services._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class SummaryControllerImpl @Inject()(val keystoreConnector: KeystoreConnect,
                                      val authConnector: AuthClientConnector,
                                      val vrs: RegistrationService,
                                      val lodgingOfficerService: LodgingOfficerService,
                                      val sicSrv: SicAndComplianceService,
                                      val s4LService: S4LService,
                                      val messagesApi: MessagesApi,
                                      val flatRateService: FlatRateService,
                                      val configConnector: ConfigConnector) extends SummaryController

trait SummaryController extends BaseController with SessionProfile with ApplicativeSyntax {
  val vrs: RegistrationService
  val lodgingOfficerService: LodgingOfficerService
  val sicSrv: SicAndComplianceService
  val s4LService: S4LService
  val flatRateService: FlatRateService
  val configConnector: ConfigConnector

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          summary <- getRegistrationSummary()
          _       <- s4LService.clear
        } yield Ok(views.html.pages.summary(summary))
      }
  }

  def getRegistrationSummary()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Summary] = {
    for {
      officer <- lodgingOfficerService.getLodgingOfficer
      sac     <- sicSrv.getSicAndCompliance
      taxableThreshold <- vrs.getTaxableThreshold()
      summary <- vrs.getVatScheme.map(scheme => registrationToSummary(scheme.copy(lodgingOfficer = Some(officer), sicAndCompliance = Some(sac)), taxableThreshold))
    } yield summary
  }

  def submitRegistration: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        invalidSubmissionGuard() {
          for {
            _        <- keystoreConnector.cache[CurrentProfile]("CurrentProfile", profile.copy(vatRegistrationStatus = VatRegStatus.locked))
            response <- vrs.submitRegistration()
            result   <- submissionRedirectLocation(response)
          } yield {
            result
          }
        }
      }
  }

  private def submissionRedirectLocation(response: DESResponse)(implicit hc : HeaderCarrier, currentProfile: CurrentProfile): Future[Result] = {
    response match {
      case Success => keystoreConnector.cache[CurrentProfile]("CurrentProfile", currentProfile.copy(vatRegistrationStatus = VatRegStatus.held)) map {
        _ => Redirect(controllers.routes.ApplicationSubmissionController.show())
      }
      case SubmissionFailed => Future.successful(Redirect(controllers.routes.ErrorController.submissionFailed()))
      case SubmissionFailedRetryable => Future.successful(Redirect(controllers.routes.ErrorController.submissionRetryable()))
    }
  }

  def registrationToSummary(vs: VatScheme, taxableThreshold: String)(implicit profile : CurrentProfile): Summary = {
    Summary(Seq(
      SummaryVatDetailsSectionBuilder(
        vs.tradingDetails,
        vs.threshold,
        vs.returns,
        profile.incorporationDate,
        taxableThreshold
      ).section,
      SummaryDirectorDetailsSectionBuilder(vs.lodgingOfficer.getOrElse(throw new IllegalStateException("Missing Lodging Officer data to show summary"))).section,
      SummaryDirectorAddressesSectionBuilder(vs.lodgingOfficer.getOrElse(throw new IllegalStateException("Missing Lodging Officer data to show summary"))).section,
      SummaryDoingBusinessAbroadSectionBuilder(vs.tradingDetails).section,
      SummaryBusinessActivitiesSectionBuilder(vs.sicAndCompliance).section,
      SummaryComplianceSectionBuilder(vs.sicAndCompliance).section,
      SummaryCompanyContactDetailsSectionBuilder(vs.businessContact).section,
      SummaryBusinessBankDetailsSectionBuilder(vs.bankAccount).section,
      SummaryAnnualAccountingSchemeSectionBuilder(vs.returns).section,
      SummaryTaxableSalesSectionBuilder(vs.turnOverEstimates).section,
      SummaryFrsSectionBuilder(
        vs.flatRateScheme,
        vs.flatRateScheme.flatMap(_.estimateTotalSales.map(v => flatRateService.applyPercentRoundUp(v))),
        vs.flatRateScheme.flatMap(_.categoryOfBusiness.filter(_.nonEmpty).map(frsId => configConnector.getBusinessTypeDetails(frsId)._1)),
        vs.turnOverEstimates
      ).section
    ))
  }

  private[controllers] def invalidSubmissionGuard()(f: => Future[Result])(implicit hc: HeaderCarrier, profile: CurrentProfile) = {
    vrs.getStatus(profile.registrationId) flatMap {
      case VatRegStatus.draft => f
      case _ => Future.successful(InternalServerError)
    }
  }
}
