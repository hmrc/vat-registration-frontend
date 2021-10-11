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

package connectors.mocks

import connectors.AttachmentsConnector
import models.api.{AttachmentMethod, Attachments}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsValue

import scala.concurrent.Future

trait MockAttachmentsConnector extends MockitoSugar {
  self: Suite =>

  val mockAttachmentsConnector = mock[AttachmentsConnector]

  def mockGetAttachmentsList(regId: String)(response: Future[Attachments]): OngoingStubbing[Future[Attachments]] =
    when(mockAttachmentsConnector.getAttachmentList(ArgumentMatchers.eq(regId))(ArgumentMatchers.any()))
      .thenReturn(response)

  def mockStoreAttachmentDetails(regId: String, method: AttachmentMethod)(response: Future[JsValue]): OngoingStubbing[Future[JsValue]] =
    when(mockAttachmentsConnector.storeAttachmentDetails(
      ArgumentMatchers.eq(regId),
      ArgumentMatchers.eq(method)
    )(ArgumentMatchers.any())).thenReturn(response)

}
