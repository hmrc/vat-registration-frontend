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

package connectors.mocks

import connectors.AttachmentsConnector
import models.api.AttachmentType
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Future
import play.api.mvc.Request

trait MockAttachmentsConnector extends MockitoSugar {
  self: Suite =>

  val mockAttachmentsConnector: AttachmentsConnector = mock[AttachmentsConnector]

  def mockGetAttachmentsList(regId: String)(response: Future[List[AttachmentType]]): OngoingStubbing[Future[List[AttachmentType]]] =
    when(mockAttachmentsConnector.getAttachmentList(ArgumentMatchers.eq(regId))(ArgumentMatchers.any(), ArgumentMatchers.any[Request[_]]))
      .thenReturn(response)

  def mockGetIncompleteAttachments(regId: String)(response: Future[List[AttachmentType]]): OngoingStubbing[Future[List[AttachmentType]]] =
    when(mockAttachmentsConnector.getIncompleteAttachments(ArgumentMatchers.eq(regId))(ArgumentMatchers.any(), ArgumentMatchers.any[Request[_]]))
      .thenReturn(response)
}
