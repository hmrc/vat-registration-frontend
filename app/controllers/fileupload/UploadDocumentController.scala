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

package controllers.fileupload

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featuretoggle.FeatureSwitch.VrsNewAttachmentJourney
import featuretoggle.FeatureToggleSupport
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import viewmodels.UploadDocumentHintBuilder
import views.html.fileupload.UploadDocument
import views.html.attachments.UploadDocumentsNewJourney

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import viewmodels.tasklist.{TLNotStarted, TaskListSectionRow}


@Singleton
class UploadDocumentController @Inject()(view: UploadDocument,
                                         newView: UploadDocumentsNewJourney,
                                         upscanService: UpscanService,
                                         attachmentsService: AttachmentsService,
                                         uploadDocumentHint: UploadDocumentHintBuilder,
                                         val authConnector: AuthConnector,
                                         val sessionService: SessionService
                                        )(implicit appConfig: FrontendAppConfig,
                                          val executionContext: ExecutionContext,
                                          baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with FeatureToggleSupport {

  def show(): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        attachmentsService.getIncompleteAttachments(profile.registrationId).flatMap {
          case Nil =>
            Future.successful(Redirect(controllers.routes.TaskListController.show.url))
          case list =>
            upscanService.initiateUpscan(profile.registrationId, list.head).flatMap { upscanResponse =>
              uploadDocumentHint.build(list.head).map { hintHtml =>
                val optErrorCode = request.queryString.get("errorCode").flatMap(_.headOption)
                  Ok(view(upscanResponse, Some(hintHtml), list.head, optErrorCode)).addingToSession("reference" -> upscanResponse.reference)
            }
            }
        }
  }
}