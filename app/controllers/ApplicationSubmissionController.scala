/*
 * Copyright 2021 HM Revenue & Customs
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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import models.api.IdentityEvidence
import play.api.mvc._
import services.{AttachmentsService, SessionProfile, SessionService, VatRegistrationService}
import views.html.pages.application_submission_confirmation

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationSubmissionController @Inject()(val vatRegistrationService: VatRegistrationService,
                                                val attachmentsService: AttachmentsService,
                                                val authConnector: AuthClientConnector,
                                                val sessionService: SessionService,
                                                val applicationSubmissionConfirmationView: application_submission_confirmation)
                                               (implicit appConfig: FrontendAppConfig,
                                                val executionContext: ExecutionContext,
                                                baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  private val prefixLength = 3
  private val groupSize = 4
  private val separator = " "

  def show: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request =>
      implicit profile =>
        for {
          attachmentsList <- attachmentsService.getAttachmentList(profile.registrationId)
          acknowledgementRef <- vatRegistrationService.getAckRef(profile.registrationId)
          prefix = acknowledgementRef.take(prefixLength)
          groups = acknowledgementRef.drop(prefixLength).grouped(groupSize).toList
          formattedRef = prefix +: groups mkString separator
        } yield Ok(applicationSubmissionConfirmationView(formattedRef, attachmentsList.method, attachmentsList.attachments.contains(IdentityEvidence)))
  }

  def submit: Action[AnyContent] = isAuthenticated {
    _ => Future.successful(Redirect(appConfig.feedbackUrl).withNewSession)
  }
}
