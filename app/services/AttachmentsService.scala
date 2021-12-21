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

import connectors.AttachmentsConnector
import models.api.{AttachmentMethod, Attachments}
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AttachmentsService @Inject()(val attachmentsConnector: AttachmentsConnector)
                                  (implicit ec: ExecutionContext) {

  def getAttachmentList(regId: String)(implicit hc: HeaderCarrier): Future[Attachments] =
    attachmentsConnector.getAttachmentList(regId)

  def storeAttachmentDetails(regId: String, method: AttachmentMethod)(implicit hc: HeaderCarrier): Future[JsValue] =
    attachmentsConnector.storeAttachmentDetails(regId, method)

}