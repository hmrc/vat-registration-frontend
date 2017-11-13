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

package controllers.iv

import javax.inject.Inject

import common.enums.IVResult
import connectors.IdentityVerificationConnector
import controllers.{CommonPlayDependencies, VatRegistrationController}
import features.iv.services.IdentityVerificationService
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Call}
import services.{CommonService, CurrentProfileService, SessionProfile}

import scala.concurrent.Future

class IdentityVerificationController @Inject()(ds: CommonPlayDependencies,
                                               ivConnector: IdentityVerificationConnector,
                                               ivService: IdentityVerificationService,
                                               cpService:CurrentProfileService)
  extends VatRegistrationController(ds) with CommonService with SessionProfile {

  def redirectToIV: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivService.setupAndGetIVJourneyURL.map { ivUrl =>
            Redirect(ivUrl)
          }
        }.recover{case e:Exception => Redirect(controllers.callbacks.routes.SignInOutController.errorShow())}
  }

  def timeoutIV: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(features.iv.views.html.error.timeoutIV()))
        }
  }

  def unableToConfirmIdentity: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(features.iv.views.html.error.unabletoconfirmidentity()))
        }
  }

  def failedIV: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(features.iv.views.html.error.failediv()))
        }

  }

  def lockedOut: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(features.iv.views.html.error.lockedoutiv()))
        }
  }

  def userAborted: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(features.iv.views.html.error.useraborted()))
        }
  }

  def completedIVJourney: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivService.getJourneyIdAndJourneyOutcome.flatMap { res =>
            ivService.setIvStatus(res).flatMap {
              case Some(_) => cpService.updateIVStatusInCurrentProfile(true).flatMap(_ =>
                Future.successful(Redirect(controllers.vatLodgingOfficer.routes.FormerNameController.show())))
              case _ => Future.successful(InternalServerError)
            }
          }.recover{case _ => InternalServerError(views.html.pages.error.restart())}
        }
  }

  def failedIVJourney(journeyId: String): Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivService.getJourneyIdAndJourneyOutcome.flatMap { res => {
            ivService.setIvStatus(res).map {
              case Some(IVResult.Timeout) => Redirect(controllers.iv.routes.IdentityVerificationController.timeoutIV())
              case Some(IVResult.InsufficientEvidence) => Redirect(controllers.iv.routes.IdentityVerificationController.unableToConfirmIdentity())
              case Some(IVResult.FailedIV) => Redirect(controllers.iv.routes.IdentityVerificationController.failedIV())
              case Some(IVResult.LockedOut) => Redirect(controllers.iv.routes.IdentityVerificationController.lockedOut())
              case Some(IVResult.UserAborted) => Redirect(controllers.iv.routes.IdentityVerificationController.userAborted())
              case _ => Redirect(controllers.callbacks.routes.SignInOutController.errorShow())
            }
          }
            }
          }.recover{case _ => Redirect(controllers.callbacks.routes.SignInOutController.errorShow())}
        }
}
