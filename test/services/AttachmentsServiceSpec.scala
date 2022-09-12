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
import services.AttachmentsService.{Supply1614AAnswer, Supply1614HAnswer, SupplySupportingDocumentsAnswer}
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
    "send the given attachment method and proxy the response from the connector" in {
      val updatedAttachments = Attachments(method = Some(Post))
      mockGetSection[Attachments](testRegId, None)
      mockReplaceSection[Attachments](testRegId, updatedAttachments)

      val res = await(Service.storeAttachmentDetails(testRegId, Post))
      res mustBe updatedAttachments
    }

    "send the 'false' Supply 1614A Answer" in {
      val presentAttachments = Attachments(method = Some(Post))
      val updatedAttachments = presentAttachments.copy(supplyVat1614a = Some(false))
      mockGetSection[Attachments](testRegId, Some(presentAttachments))
      mockReplaceSection[Attachments](testRegId, updatedAttachments)

      val res = await(Service.storeAttachmentDetails(testRegId, Supply1614AAnswer(false)))
      res mustBe updatedAttachments
    }

    "send the 'true' Supply 1614A Answer and remove supplyVat1614h" in {
      val presentAttachments = Attachments(method = Some(Post), supplyVat1614a = Some(false), supplyVat1614h = Some(false))
      val updatedAttachments = presentAttachments.copy(supplyVat1614a = Some(true), supplyVat1614h = None)
      mockGetSection[Attachments](testRegId, Some(presentAttachments))
      mockReplaceSection[Attachments](testRegId, updatedAttachments)

      val res = await(Service.storeAttachmentDetails(testRegId, Supply1614AAnswer(true)))
      res mustBe updatedAttachments
    }

    "send the 'false' Supply 1614H Answer" in {
      val presentAttachments = Attachments(method = Some(Post), supplyVat1614a = Some(false))
      val updatedAttachments = presentAttachments.copy(supplyVat1614h = Some(false))
      mockGetSection[Attachments](testRegId, Some(presentAttachments))
      mockReplaceSection[Attachments](testRegId, updatedAttachments)

      val res = await(Service.storeAttachmentDetails(testRegId, Supply1614HAnswer(false)))
      res mustBe updatedAttachments
    }

    "send the 'true' Supply 1614H Answer" in {
      val presentAttachments = Attachments(method = Some(Post), supplyVat1614a = Some(false))
      val updatedAttachments = presentAttachments.copy(supplyVat1614h = Some(true))
      mockGetSection[Attachments](testRegId, Some(presentAttachments))
      mockReplaceSection[Attachments](testRegId, updatedAttachments)

      val res = await(Service.storeAttachmentDetails(testRegId, Supply1614HAnswer(true)))
      res mustBe updatedAttachments
    }

    "send the 'false' Supply Supporting Documents Answer" in {
      val presentAttachments = Attachments(method = Some(Post), supplyVat1614a = Some(false), supplyVat1614h = Some(false))
      val updatedAttachments = presentAttachments.copy(supplySupportingDocuments = Some(false))
      mockGetSection[Attachments](testRegId, Some(presentAttachments))
      mockReplaceSection[Attachments](testRegId, updatedAttachments)

      val res = await(Service.storeAttachmentDetails(testRegId, SupplySupportingDocumentsAnswer(false)))
      res mustBe updatedAttachments
    }

    "send the 'true' Supply Supporting Documents Answer" in {
      val presentAttachments = Attachments(method = Some(Post), supplyVat1614a = Some(false), supplyVat1614h = Some(true))
      val updatedAttachments = presentAttachments.copy(supplySupportingDocuments = Some(true))
      mockGetSection[Attachments](testRegId, Some(presentAttachments))
      mockReplaceSection[Attachments](testRegId, updatedAttachments)

      val res = await(Service.storeAttachmentDetails(testRegId, SupplySupportingDocumentsAnswer(true)))
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
