/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.mvc._
import services._
import uk.gov.hmrc.http.InternalServerException
import views.html.ApplicationSubmissionConfirmation
import controllers.BaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationSubmissionController @Inject()(vatRegistrationService: VatRegistrationService,
                                                applicantDetailsService: ApplicantDetailsService,
                                                transactorDetailsService: TransactorDetailsService,
                                                attachmentsService: AttachmentsService,
                                                val authConnector: AuthClientConnector,
                                                val sessionService: SessionService,
                                                applicationSubmissionConfirmationView: ApplicationSubmissionConfirmation)
                                               (implicit appConfig: FrontendAppConfig,
                                                val executionContext: ExecutionContext,
                                                baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  private val prefixLength = 3
  private val groupSize = 4
  private val separator = " "

  def show: Action[AnyContent] = isAuthenticatedAndSubmitted {
    implicit request =>
      implicit profile =>
        for {
          attachments <- attachmentsService.getAttachmentList(profile.registrationId)
          attachmentDetails <- attachmentsService.getAttachmentDetails(profile.registrationId)
          acknowledgementRef <- vatRegistrationService.getAckRef(profile.registrationId)
          prefix = acknowledgementRef.take(prefixLength)
          groups = acknowledgementRef.drop(prefixLength).grouped(groupSize).toList
          formattedRef = if(appConfig.isNewVRSConfirmJourneyEnabled) { groups mkString separator } else { prefix +: groups mkString separator }
          isTransactor <- vatRegistrationService.isTransactor

          optEmail <- if (isTransactor) {
            transactorDetailsService.getTransactorDetails.map(_.email)
          } else {
            applicantDetailsService.getApplicantDetails.map(_.contact.email)
          }
          eligibilitySubmissionData <- vatRegistrationService.getEligibilitySubmissionData
          email = optEmail.getOrElse(throw new InternalServerException("[ApplicationSubmissionController] missing user email"))
        }
        yield Ok(applicationSubmissionConfirmationView(formattedRef, attachmentDetails.flatMap(_.method), attachments.nonEmpty, email, isTransactor, eligibilitySubmissionData.registrationReason))
  }

  def submit: Action[AnyContent] = isAuthenticated {
    _ => Future.successful(Redirect(appConfig.feedbackUrl).withNewSession)
  }
}
