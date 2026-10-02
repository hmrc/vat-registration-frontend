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

package controllers.fileupload

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featuretoggle.FeatureSwitch.VrsNewAttachmentJourney
import featuretoggle.FeatureToggleSupport
import models.api.{ExtraIdentityEvidence, ExtraTransactorIdentityEvidence}
import play.api.mvc.{Action, AnyContent}
import play.twirl.api.Html
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import viewmodels.UploadDocumentHintBuilder
import views.html.fileupload.{UploadDocument, UploadDocumentNewJourney, UploadIdentityEvidence}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class UploadDocumentController @Inject()(view: UploadDocument,
                                         viewNewAttachmentJourney: UploadDocumentNewJourney,
                                         viewIdentityEvidence: UploadIdentityEvidence,
                                         upscanService: UpscanService,
                                         attachmentsService: AttachmentsService,
                                         uploadDocumentHint: UploadDocumentHintBuilder,
                                         val authConnector: AuthConnector,
                                         val sessionService: SessionService
                                        )(implicit appConfig: FrontendAppConfig,
                                          val executionContext: ExecutionContext,
                                          baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with FeatureToggleSupport {

  private val extraIdentityEvidenceRequired = 2

  def show(): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        attachmentsService.getIncompleteAttachments(profile.registrationId).flatMap {
          case Nil =>
            Future.successful(Redirect(controllers.routes.TaskListController.show.url))
          case incompleteAttachments @ attachmentType :: _ =>
            upscanService.initiateUpscan(profile.registrationId, attachmentType).flatMap { upscanResponse =>
              val optErrorCode = request.getQueryString("errorCode")
              val page: Future[Html] = attachmentType match {
                case ExtraIdentityEvidence | ExtraTransactorIdentityEvidence if isEnabled(VrsNewAttachmentJourney) =>
                  val uploadedCount = extraIdentityEvidenceRequired - incompleteAttachments.count(_ == attachmentType)
                  uploadDocumentHint.identityEvidenceName(attachmentType).map { optName =>
                    viewIdentityEvidence(upscanResponse, optName, uploadedCount, optErrorCode)
                  }
                case _ =>
                  uploadDocumentHint.build(attachmentType).map { hintHtml =>
                    val uploadView = if (isEnabled(VrsNewAttachmentJourney)) viewNewAttachmentJourney.apply _ else view.apply _
                    uploadView(upscanResponse, Some(hintHtml), attachmentType, optErrorCode)
                  }
              }
              page.map(Ok(_).addingToSession("reference" -> upscanResponse.reference))
            }
        }
  }
}