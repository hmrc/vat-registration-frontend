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

package features.officer.controllers

import javax.inject.Inject

import common.enums.IVResult
import config.AuthClientConnector
import connectors.KeystoreConnector
import controllers.BaseController
import features.officer.services.IVService
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.SessionProfile

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
      Future.successful(Ok(features.officer.views.html.error.timeoutIV()))
  }

  def unableToConfirmIdentity: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      Future.successful(Ok(features.officer.views.html.error.unabletoconfirmidentity()))
  }

  def failedIV: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      Future.successful(Ok(features.officer.views.html.error.failediv()))
  }

  def lockedOut: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      Future.successful(Ok(features.officer.views.html.error.lockedoutiv()))
  }

  def userAborted: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      Future.successful(Ok(features.officer.views.html.error.useraborted()))
  }

  def completedIVJourney: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivService.fetchAndSaveIVStatus map {
        _ => features.officer.controllers.routes.OfficerController.showFormerName()
      } recover {
        case e =>
          Logger.error(s"[IdentityVerificationController][completedIVJourney] an error occurred with message: ${e.getMessage}")
          controllers.callbacks.routes.SignInOutController.errorShow()
      } map Redirect
  }

  def failedIVJourney(journeyId: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivService.fetchAndSaveIVStatus map {
        case IVResult.Timeout              => features.officer.controllers.routes.IdentityVerificationController.timeoutIV()
        case IVResult.InsufficientEvidence => features.officer.controllers.routes.IdentityVerificationController.unableToConfirmIdentity()
        case IVResult.FailedIV             => features.officer.controllers.routes.IdentityVerificationController.failedIV()
        case IVResult.LockedOut            => features.officer.controllers.routes.IdentityVerificationController.lockedOut()
        case IVResult.UserAborted          => features.officer.controllers.routes.IdentityVerificationController.userAborted()
      } recover {
        case e =>
          Logger.error(s"[IdentityVerificationController][failedIVJourney] an error occurred with message: ${e.getMessage}")
          controllers.callbacks.routes.SignInOutController.errorShow()
      } map Redirect
  }
}
