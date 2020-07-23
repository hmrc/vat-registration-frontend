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

import config.AuthClientConnector
import connectors.KeystoreConnector
import javax.inject.Inject
import models.IVResult
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{IVService, SessionProfile}

import scala.concurrent.Future

class IdentityVerificationControllerImpl @Inject()(val ivService: IVService,
                                                   val messagesApi: MessagesApi,
                                                   val authConnector: AuthClientConnector,
                                                   val keystoreConnector: KeystoreConnector) extends IdentityVerificationController

trait IdentityVerificationController extends BaseController with SessionProfile {

  val ivService: IVService

  def redirectToIV: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivService.setupAndGetIVJourneyURL
        .map(Redirect(_))
        .recover{
          case e: Exception =>
          Logger.error(s"[IdentityVerificationController][redirectToIV] an error occurred while redirecting to IV with message: ${e.getMessage}")
          Redirect(controllers.callbacks.routes.SignInOutController.errorShow())
        }
  }

  def timeoutIV: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      Future.successful(Ok(views.html.error.timeoutIV()))
  }

  def unableToConfirmIdentity: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      Future.successful(Ok(views.html.error.unabletoconfirmidentity()))
  }

  def failedIV: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      Future.successful(Ok(views.html.error.failediv()))
  }

  def lockedOut: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      Future.successful(Ok(views.html.error.lockedoutiv()))
  }

  def userAborted: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      Future.successful(Ok(views.html.error.useraborted()))
  }

  def completedIVJourney: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivService.fetchAndSaveIVStatus map {
        _ => controllers.routes.OfficerController.showFormerName()
      } recover {
        case e =>
          Logger.error(s"[IdentityVerificationController][completedIVJourney] an error occurred with message: ${e.getMessage}")
          controllers.callbacks.routes.SignInOutController.errorShow()
      } map Redirect
  }

  def failedIVJourney(journeyId: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivService.fetchAndSaveIVStatus map {
        case IVResult.Timeout              => controllers.routes.IdentityVerificationController.timeoutIV()
        case IVResult.InsufficientEvidence => controllers.routes.IdentityVerificationController.unableToConfirmIdentity()
        case IVResult.FailedIV             => controllers.routes.IdentityVerificationController.failedIV()
        case IVResult.LockedOut            => controllers.routes.IdentityVerificationController.lockedOut()
        case IVResult.UserAborted          => controllers.routes.IdentityVerificationController.userAborted()
      } recover {
        case e =>
          Logger.error(s"[IdentityVerificationController][failedIVJourney] an error occurred with message: ${e.getMessage}")
          controllers.callbacks.routes.SignInOutController.errorShow()
      } map Redirect
  }
}
