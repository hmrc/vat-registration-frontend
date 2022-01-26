/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.registration.attachments

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import models.api.{IdentityEvidence, VAT2}
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile, SessionService}
import views.html.attachments.MultipleDocumentsRequired

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class MultipleDocumentsRequiredController @Inject()(val authConnector: AuthClientConnector,
                                                    val sessionService: SessionService,
                                                    attachmentsService: AttachmentsService,
                                                    multipleDocumentsRequiredPage: MultipleDocumentsRequired)
                                                   (implicit appConfig: FrontendAppConfig,
                                                    val executionContext: ExecutionContext,
                                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        attachmentsService.getAttachmentList(profile.registrationId).map { attachmentInfo =>
          Ok(multipleDocumentsRequiredPage(attachmentInfo.attachments))
        }
  }

}
