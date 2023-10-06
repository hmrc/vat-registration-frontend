/*
 * Copyright 2023 HM Revenue & Customs
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
import viewmodels.tasklist.{AttachmentsTaskList, TaskListSections}
import views.html.Summary

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SummaryController @Inject()(val sessionService: SessionService,
                                  val authConnector: AuthClientConnector,
                                  val vrs: VatRegistrationService,
                                  val summaryService: SummaryService,
                                  val nonRepudiationService: NonRepudiationService,
                                  val summaryPage: Summary,
                                  businessService: BusinessService,
                                  attachmentsService: AttachmentsService
                                 )
                                 (implicit appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with ApplicativeSyntax {

  def show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      vrs.getVatScheme.flatMap { vatScheme =>
        AttachmentsTaskList.attachmentsRequiredRow(attachmentsService, businessService).flatMap { attachmentsRequiredRow =>
        if (TaskListSections.allComplete(vatScheme, businessService, attachmentsRequiredRow)) {
          infoLog(s"[SummaryController][show] - The TaskListSections are all complete, loading the summary page")
          for {
            accordion <- summaryService.getSummaryData
            html = summaryPage(accordion)
            _ <- nonRepudiationService.storeEncodedUserAnswers(profile.registrationId, html)
          } yield Ok(html)
        } else {
          infoLog(s"[SummaryController][show] - The TaskListSections are not all complete, redirecting to the application progress page")
          Future.successful(Redirect(controllers.routes.TaskListController.show))
        }
      }
      }
  }


  def submitRegistration: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request =>
      implicit profile =>
        vrs.getVatScheme.flatMap { vatScheme =>
          AttachmentsTaskList.attachmentsRequiredRow(attachmentsService, businessService).flatMap { attachmentsRequiredRow =>
          if (TaskListSections.allComplete(vatScheme, businessService, attachmentsRequiredRow)) {
            infoLog(s"[SummaryController][submitRegistration] - The TaskListSections are all complete")
            for {
              _ <- sessionService.cache[CurrentProfile]("CurrentProfile", profile.copy(vatRegistrationStatus = VatRegStatus.locked))
              response <- vrs.submitRegistration
              result <- submissionRedirectLocation(response)
            } yield {
              result
            }
          } else {
            infoLog(s"[SummaryController][submitRegistration] - The TaskListSections are not all complete, redirecting to the application progress page")
            Future.successful(Redirect(controllers.routes.TaskListController.show))
          }
        }
        }
  }

  private def submissionRedirectLocation(response: DESResponse)(implicit hc: HeaderCarrier, currentProfile: CurrentProfile, request: Request[_]): Future[Result] = {
    response match {
      case Success =>
        sessionService.cache[CurrentProfile]("CurrentProfile", currentProfile.copy(vatRegistrationStatus = VatRegStatus.submitted)).map {
          _ =>
            infoLog(s"[SummaryController][submissionRedirectLocation][Success] - The application was successfully submitted, redirecting to the application submitted page")
            Redirect(controllers.routes.ApplicationSubmissionController.show)
        }
      case SubmissionInProgress =>
        sessionService.cache[CurrentProfile]("CurrentProfile", currentProfile.copy(vatRegistrationStatus = VatRegStatus.locked)).map {
          _ =>
            infoLog(s"[SummaryController][submissionRedirectLocation][SubmissionInProgress] - The application submission is in progress")
            Redirect(controllers.routes.SubmissionInProgressController.show)
        }
      case AlreadySubmitted =>
        sessionService.cache[CurrentProfile]("CurrentProfile", currentProfile.copy(vatRegistrationStatus = VatRegStatus.duplicateSubmission)).map {
          _ =>
            infoLog(s"[SummaryController][submissionRedirectLocation][AlreadySubmitted] - The application has already been submitted")
            Redirect(controllers.errors.routes.ErrorController.alreadySubmitted)
        }
      case SubmissionFailed =>
        sessionService.cache[CurrentProfile]("CurrentProfile", currentProfile.copy(vatRegistrationStatus = VatRegStatus.failed)).map {
          _ =>
            infoLog(s"[SummaryController][submissionRedirectLocation][SubmissionFailed] - The application submission has failed")
            Redirect(controllers.errors.routes.ErrorController.submissionFailed)
        }
      case SubmissionFailedRetryable =>
        sessionService.cache[CurrentProfile]("CurrentProfile", currentProfile.copy(vatRegistrationStatus = VatRegStatus.failedRetryable)).map {
          _ =>
            infoLog(s"[SummaryController][submissionRedirectLocation][SubmissionFailedRetryable] - The application submission has failed, redirecting to the retry-submission page")
            Redirect(controllers.errors.routes.ErrorController.submissionRetryable)
        }
      case Contact =>
        sessionService.cache[CurrentProfile]("CurrentProfile", currentProfile.copy(vatRegistrationStatus = VatRegStatus.contact)).map {
          _ =>
            infoLog(s"[SummaryController][submissionRedirectLocation][Contact] - The application submission has failed, redirecting to the contact page")
            Redirect(controllers.errors.routes.ErrorController.contact)
        }
    }
  }
}