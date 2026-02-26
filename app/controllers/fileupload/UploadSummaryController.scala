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
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import viewmodels.tasklist.UploadDocumentsTaskList
import views.html.attachments.UploadDocumentsNewJourney

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UploadSummaryController @Inject()(view: UploadDocumentsNewJourney,
                                        upscanService: UpscanService,
                                        attachmentsService: AttachmentsService,
                                        vatRegistrationService: VatRegistrationService,
                                        taskList: UploadDocumentsTaskList,
                                        val authConnector: AuthConnector,
                                        val sessionService: SessionService
                                       )(implicit appConfig: FrontendAppConfig,
                                         val executionContext: ExecutionContext,
                                         baseControllerComponents: BaseControllerComponents)
extends BaseController with SessionProfile {

  def show(): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile => {
      for {
        vatScheme <- vatRegistrationService.getVatScheme
        attachments <- attachmentsService.getAttachmentList(profile.registrationId)
        upscanDetails <- upscanService.fetchAllUpscanDetails(profile.registrationId)
      } yield {
        val rows = attachments.map(attachmentType => taskList.attachmentRow(attachmentType, upscanDetails).build(vatScheme))
        Ok(view(rows: _*))
      }
    }
  }

}
