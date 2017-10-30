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
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, SessionProfile}

import scala.concurrent.Future

class IdentityVerificationController @Inject()(ds: CommonPlayDependencies,
                                               ivConnector: IdentityVerificationConnector)
  extends VatRegistrationController(ds) with CommonService with SessionProfile {

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

  def failedIv = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(features.iv.views.html.error.failediv()))
        }

  }

  def lockedOutIv = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(features.iv.views.html.error.lockedoutiv()))
        }
  }

  def userAborted = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(features.iv.views.html.error.useraborted()))
        }
  }

  def completedIVJourney: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Redirect(controllers.vatLodgingOfficer.routes.FormerNameController.show()))
        }
  }

  def failedIVJourney(journeyId: String): Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          ivConnector.getJourneyOutcome(journeyId) map {
            case IVResult.Timeout => Redirect(controllers.iv.routes.IdentityVerificationController.timeoutIV())
            case IVResult.InsufficientEvidence => Redirect(controllers.iv.routes.IdentityVerificationController.unableToConfirmIdentity())
          }
        }
  }
}
