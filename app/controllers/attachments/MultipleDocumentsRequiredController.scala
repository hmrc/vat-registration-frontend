/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.attachments

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featuretoggle.FeatureSwitch.VrsNewAttachmentJourney
import featuretoggle.FeatureToggleSupport
import play.api.mvc.{Action, AnyContent}
import services._
import views.html.attachments.{AdditionalDocuments, MultipleDocumentsRequired}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MultipleDocumentsRequiredController @Inject()(val authConnector: AuthClientConnector,
                                                    val sessionService: SessionService,
                                                    attachmentsService: AttachmentsService,
                                                    vatRegistrationService: VatRegistrationService,
                                                    applicantDetailsService: ApplicantDetailsService,
                                                    transactorDetailsService: TransactorDetailsService,
                                                    multipleDocumentsRequiredPage: MultipleDocumentsRequired,
                                                    addtionalDocumentsPage: AdditionalDocuments)
                                                   (implicit appConfig: FrontendAppConfig,
                                                    val executionContext: ExecutionContext,
                                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with FeatureToggleSupport {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          attachments <- attachmentsService.getAttachmentList(profile.registrationId)
          isTransactor <- vatRegistrationService.isTransactor
          applicantName <- if (isTransactor) {
            applicantDetailsService.getApplicantDetails.map(_.personalDetails.map(_.fullName))
          } else {
            Future.successful(None)
          }
          transactorName <- if (isTransactor) {
            transactorDetailsService.getTransactorDetails.map(_.personalDetails.map(_.fullName))
          } else {
            Future.successful(None)
          }
        } yield {
          Ok(
          if (isEnabled(VrsNewAttachmentJourney)) { addtionalDocumentsPage(attachments, applicantName, transactorName) }
          else { multipleDocumentsRequiredPage(attachments, applicantName, transactorName) }
        )
        }

  }

}
