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

package services

import connectors.{AttachmentsConnector, RegistrationApiConnector}
import models.api.{AttachmentMethod, AttachmentType, Attachments}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AttachmentsService @Inject()(val attachmentsConnector: AttachmentsConnector,
                                   registrationApiConnector: RegistrationApiConnector
                                  )(implicit ec: ExecutionContext) {

  def getAttachmentDetails(regId: String)(implicit hc: HeaderCarrier): Future[Option[Attachments]] =
    registrationApiConnector.getSection[Attachments](regId)

  def storeAttachmentDetails(regId: String, method: AttachmentMethod)(implicit hc: HeaderCarrier): Future[Attachments] =
    registrationApiConnector.replaceSection[Attachments](regId, Attachments(method = Some(method)))

  def getAttachmentList(regId: String)(implicit hc: HeaderCarrier): Future[List[AttachmentType]] =
    attachmentsConnector.getAttachmentList(regId)

  def getIncompleteAttachments(regId: String)(implicit hc: HeaderCarrier): Future[List[AttachmentType]] =
    attachmentsConnector.getIncompleteAttachments(regId)
}