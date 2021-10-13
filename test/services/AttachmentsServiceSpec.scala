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

package services

import connectors.mocks.MockAttachmentsConnector
import models.api.{Attachments, IdentityEvidence, Post}
import play.api.libs.json.Json
import testHelpers.VatRegSpec

import scala.concurrent.Future

class AttachmentsServiceSpec extends VatRegSpec with MockAttachmentsConnector {

  object Service extends AttachmentsService(mockAttachmentsConnector)

  "getAttachmentList" when {
    "the backend doesn't contain any attachment details" should {
      "proxy through the response from the connector" in {
        val emptyAttachmentList = Attachments(None, List())
        mockGetAttachmentsList(testRegId)(Future.successful(emptyAttachmentList))

        val res = await(Service.getAttachmentList(testRegId))

        res mustBe emptyAttachmentList
      }
    }
    "the backend contains full attachment details" should {
      "proxy through the response from the connector" in {
        val fullAttachmentList = Attachments(Some(Post), List(IdentityEvidence))
        mockGetAttachmentsList(testRegId)(Future.successful(fullAttachmentList))

        val res = await(Service.getAttachmentList(testRegId))

        res mustBe fullAttachmentList
      }
    }
  }

  "storeAttachmentDetails" should {
    "send the given value and proxy the response from the connector" in {
      val fullAttachmentList = Attachments(Some(Post), List(IdentityEvidence))
      val responseJson = Json.toJson(fullAttachmentList)

      mockStoreAttachmentDetails(testRegId, Post)(Future.successful(responseJson))

      val res = await(Service.storeAttachmentDetails(testRegId, Post))

      res mustBe responseJson
    }
  }

}
