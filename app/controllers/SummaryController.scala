/*
 * Copyright 2020 HM Revenue & Customs
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
import config.AuthClientConnector
import connectors._
import javax.inject.{Inject, Singleton}
import models.CurrentProfile
import play.api.i18n.MessagesApi
import play.api.mvc._
import services._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

@Singleton
class SummaryController @Inject()(val keystoreConnector: KeystoreConnector,
                                  val authConnector: AuthClientConnector,
                                  val vrs: VatRegistrationService,
                                  val s4LService: S4LService,
                                  val messagesApi: MessagesApi,
                                  val summaryService: SummaryService)
  extends BaseController with SessionProfile with ApplicativeSyntax {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        ivPassedCheck {
          for {
            eligibilitySummary <- summaryService.getEligibilityDataSummary
            summary <- summaryService.getRegistrationSummary
            _ <- s4LService.clear
          } yield Ok(views.html.pages.summary(eligibilitySummary, summary))
        }
  }

  def submitRegistration: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request =>
      implicit profile =>
        invalidSubmissionGuard() {
          for {
            _ <- keystoreConnector.cache[CurrentProfile]("CurrentProfile", profile.copy(vatRegistrationStatus = VatRegStatus.locked))
            response <- vrs.submitRegistration()
            result <- submissionRedirectLocation(response)
          } yield {
            result
          }
        }
  }

  private def submissionRedirectLocation(response: DESResponse)(implicit hc: HeaderCarrier, currentProfile: CurrentProfile): Future[Result] = {
    response match {
      case Success => keystoreConnector.cache[CurrentProfile]("CurrentProfile", currentProfile.copy(vatRegistrationStatus = VatRegStatus.held)) map {
        _ => Redirect(controllers.routes.ApplicationSubmissionController.show())
      }
      case SubmissionFailed => Future.successful(Redirect(controllers.routes.ErrorController.submissionFailed()))
      case SubmissionFailedRetryable => Future.successful(Redirect(controllers.routes.ErrorController.submissionRetryable()))
    }
  }


  private[controllers] def invalidSubmissionGuard()(f: => Future[Result])(implicit hc: HeaderCarrier, profile: CurrentProfile) = {
    vrs.getStatus(profile.registrationId) flatMap {
      case VatRegStatus.draft | VatRegStatus.locked => f
      case _ => Future.successful(InternalServerError)
    }
  }
}
