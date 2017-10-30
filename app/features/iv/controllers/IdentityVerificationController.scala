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

package controllers

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import services.{CommonService, SessionProfile}

import scala.concurrent.Future

class IdentityVerificationController @Inject()(ds: CommonPlayDependencies)
  extends VatRegistrationController(ds) with CommonService with SessionProfile {

  def timeoutIV = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(features.iv.views.html.error.timeoutIV()))
        }
  }

  def unableToConfirmIdentity = authorised.async {
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

}