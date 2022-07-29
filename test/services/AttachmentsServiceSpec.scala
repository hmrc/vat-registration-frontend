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

import connectors.mocks.{MockAttachmentsConnector, MockRegistrationApiConnector}
import models.api.{AttachmentType, Attachments, IdentityEvidence, Post}
import testHelpers.VatRegSpec

import scala.concurrent.Future

class AttachmentsServiceSpec extends VatRegSpec with MockAttachmentsConnector with MockRegistrationApiConnector {

  object Service extends AttachmentsService(mockAttachmentsConnector, mockRegistrationApiConnector)

  "getAttachmentList" when {
    "the backend doesn't contain any attachments" should {
      "proxy through the response from the connector" in {
        val emptyAttachmentList = List()
        mockGetAttachmentsList(testRegId)(Future.successful(emptyAttachmentList))

        val res = await(Service.getAttachmentList(testRegId))

        res mustBe emptyAttachmentList
      }
    }
    "the backend contains attachments" should {
      "proxy through the response from the connector" in {
        val fullAttachmentList = List(IdentityEvidence)

        mockGetAttachmentsList(testRegId)(Future.successful(fullAttachmentList))

        val res = await(Service.getAttachmentList(testRegId))

        res mustBe fullAttachmentList
      }
    }
  }

  "storeAttachmentDetails" should {
    "send the given value and proxy the response from the connector" in {
      val updatedAttachments = Attachments(method = Some(Post))
      mockReplaceSection[Attachments](testRegId, updatedAttachments)

      val res = await(Service.storeAttachmentDetails(testRegId, Post))
      res mustBe updatedAttachments
    }
  }

  "getIncompleteAttachments" when {
    "the backend doesn't contain any incomplete attachments" should {
      "proxy through the response from the connector" in {
        mockGetIncompleteAttachments(testRegId)(Future.successful(List.empty[AttachmentType]))
        val res = await(Service.getIncompleteAttachments(testRegId))

        res mustBe empty
      }
    }
    "the backend contains full attachment details" should {
      "proxy through the response from the connector" in {
        val fullAttachmentList = List(IdentityEvidence)
        mockGetIncompleteAttachments(testRegId)(Future.successful(fullAttachmentList))
        val res = await(Service.getIncompleteAttachments(testRegId))

        res mustBe fullAttachmentList
      }
    }
  }

  "getAttachmentDetails" when {
    "the backend doesn't contain any attachment details" should {
      "proxy through the response from the connector" in {
        mockGetSection[Attachments](testRegId, Some(Attachments()))

        val res = await(Service.getAttachmentDetails(testRegId))

        res.flatMap(_.method) mustBe None
      }
    }
    "the backend contains full attachment details" should {
      "proxy through the response from the connector" in {
        mockGetSection[Attachments](testRegId, Some(Attachments(method = Some(Post))))

        val res = await(Service.getAttachmentDetails(testRegId))

        res.flatMap(_.method) mustBe Some(Post)
      }
    }
  }
}
