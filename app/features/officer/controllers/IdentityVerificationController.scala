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

import javax.inject.{Inject, Singleton}

import common.enums.IVResult
import connectors.{IVConnector, KeystoreConnect}
import controllers.{CommonPlayDependencies, VatRegistrationController}
import features.officer.services.IVService
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import services.{CurrentProfileSrv, SessionProfile}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

@Singleton
class IdentityVerificationController @Inject()(ds: CommonPlayDependencies,
                                               ivConnector: IVConnector,
                                               ivService: IVService,
                                               cpService: CurrentProfileSrv,
                                               val authConnector: AuthConnector,
                                               val keystoreConnector: KeystoreConnect) extends VatRegistrationController(ds) with SessionProfile {

  def redirectToIV: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivService.setupAndGetIVJourneyURL.map { ivUrl =>
            Redirect(ivUrl)
          }
        }.recover{case e: Exception =>
          Logger.error(s"[IdentityVerificationController][redirectToIV] an error occurred while redirecting to IV with message: ${e.getMessage}")
          Redirect(controllers.callbacks.routes.SignInOutController.errorShow())}
  }

  def timeoutIV: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(features.officer.views.html.error.timeoutIV()))
        }
  }

  def unableToConfirmIdentity: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(features.officer.views.html.error.unabletoconfirmidentity()))
        }
  }

  def failedIV: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(features.officer.views.html.error.failediv()))
        }

  }

  def lockedOut: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(features.officer.views.html.error.lockedoutiv()))
        }
  }

  def userAborted: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(features.officer.views.html.error.useraborted()))
        }
  }

  def completedIVJourney: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivService.getJourneyIdAndJourneyOutcome.flatMap { res =>
            ivService.setIvStatus(res).flatMap {
              case Some(_) => cpService.updateIVStatusInCurrentProfile(Some(true)).flatMap(_ =>
                Future.successful(Redirect(features.officer.controllers.routes.OfficerController.showFormerName())))
              case _ => Future.successful(InternalServerError)
            }
          }.recover{case e =>
            Logger.error(s"[IdentityVerificationController][completedIVJourney] an error occurred with message: ${e.getMessage}")
            InternalServerError(views.html.pages.error.restart())
          }
        }
  }

  def failedIVJourney(journeyId: String): Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivService.getJourneyIdAndJourneyOutcome.flatMap { res => {
            ivService.setIvStatus(res).map {
              case Some(IVResult.Timeout)              => Redirect(features.officer.controllers.routes.IdentityVerificationController.timeoutIV())
              case Some(IVResult.InsufficientEvidence) => Redirect(features.officer.controllers.routes.IdentityVerificationController.unableToConfirmIdentity())
              case Some(IVResult.FailedIV)             => Redirect(features.officer.controllers.routes.IdentityVerificationController.failedIV())
              case Some(IVResult.LockedOut)            => Redirect(features.officer.controllers.routes.IdentityVerificationController.lockedOut())
              case Some(IVResult.UserAborted)          => Redirect(features.officer.controllers.routes.IdentityVerificationController.userAborted())
              case _                                   => Redirect(controllers.callbacks.routes.SignInOutController.errorShow())
            }
          }
            }
          }.recover{case _ => Redirect(controllers.callbacks.routes.SignInOutController.errorShow())}
        }
}
