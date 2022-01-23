/*
 * Copyright 2022 HM Revenue & Customs
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

import cats.syntax.ApplicativeSyntax
import common.enums.VatRegStatus
import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors._
import models.CurrentProfile
import play.api.mvc._
import services._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.pages.Summary

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SummaryController @Inject()(val sessionService: SessionService,
                                  val authConnector: AuthClientConnector,
                                  val vrs: VatRegistrationService,
                                  val s4LService: S4LService,
                                  val summaryService: SummaryService,
                                  val nonRepudiationService: NonRepudiationService,
                                  val summaryPage: Summary)
                                 (implicit appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with ApplicativeSyntax {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          eligibilitySummary <- summaryService.getEligibilityDataSummary
          summary <- summaryService.getRegistrationSummary
          _ <- s4LService.clear
          html = summaryPage(eligibilitySummary, summary)
          _ <- nonRepudiationService.storeEncodedUserAnswers(profile.registrationId, html)
        } yield Ok(html)
  }

  def submitRegistration: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request =>
      implicit profile =>
        for {
          _ <- sessionService.cache[CurrentProfile]("CurrentProfile", profile.copy(vatRegistrationStatus = VatRegStatus.locked))
          response <- vrs.submitRegistration
          result <- submissionRedirectLocation(response)
        } yield {
          result
        }
  }

  private def submissionRedirectLocation(response: DESResponse)(implicit hc: HeaderCarrier, currentProfile: CurrentProfile): Future[Result] = {
    response match {
      case Success =>
        sessionService.cache[CurrentProfile]("CurrentProfile", currentProfile.copy(vatRegistrationStatus = VatRegStatus.submitted)).map {
          _ => Redirect(controllers.routes.ApplicationSubmissionController.show)
        }
      case SubmissionInProgress =>
        sessionService.cache[CurrentProfile]("CurrentProfile", currentProfile.copy(vatRegistrationStatus = VatRegStatus.locked)).map {
          _ => Redirect(controllers.routes.SubmissionInProgressController.show)
        }
      case AlreadySubmitted =>
        sessionService.cache[CurrentProfile]("CurrentProfile", currentProfile.copy(vatRegistrationStatus = VatRegStatus.duplicateSubmission)).map {
          _ => Redirect(controllers.routes.ErrorController.alreadySubmitted)
        }
      case SubmissionFailed =>
        sessionService.cache[CurrentProfile]("CurrentProfile", currentProfile.copy(vatRegistrationStatus = VatRegStatus.failed)).map {
          _ => Redirect(controllers.routes.ErrorController.submissionFailed)
        }
      case SubmissionFailedRetryable =>
        sessionService.cache[CurrentProfile]("CurrentProfile", currentProfile.copy(vatRegistrationStatus = VatRegStatus.failedRetryable)).map {
          _ => Redirect(controllers.routes.ErrorController.submissionRetryable)
        }
    }
  }
}