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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import config.FrontendAppConfig
import itFixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.api._
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import support.AppAndStubs
import uk.gov.hmrc.http.InternalServerException

class AttachmentsConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val connector: AttachmentsConnector = app.injector.instanceOf[AttachmentsConnector]
  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val testAttachmentsList: List[AttachmentType] = List[AttachmentType](IdentityEvidence, TaxRepresentativeAuthorisation, OtherAttachments)

  val testEmptyAttachmentsList: List[AttachmentType] = List[AttachmentType]()

  val attachmentUrl = "/vatreg/1/attachments"
  val incompleteAttachmentsApiUrl = "/vatreg/1/incomplete-attachments"

  val testStoreAttachmentsAttachedResponseJson: JsObject = Json.obj(
    "method" -> Some(Upload).toString,
  )
  val testStoreAttachmentsPostResponseJson: JsObject = Json.obj(
    "method" -> Some(Post).toString,
  )

  "getAttachmentList" must {
    "return an attachment list" in {
      stubGet(attachmentUrl, OK, Json.toJson(testAttachmentsList).toString())

      val response = await(connector.getAttachmentList(testRegId))

      verify(getRequestedFor(urlEqualTo(attachmentUrl)))
      response mustBe testAttachmentsList
    }

    "return an empty attachment list" in {
      stubGet(attachmentUrl, OK, Json.toJson(testEmptyAttachmentsList).toString())

      val response = await(connector.getAttachmentList(testRegId))

      verify(getRequestedFor(urlEqualTo(attachmentUrl)))
      response mustBe testEmptyAttachmentsList
    }

    "return an exception" in {
      stubGet(attachmentUrl, INTERNAL_SERVER_ERROR, Json.toJson(testEmptyAttachmentsList).toString())

      val exception = intercept[InternalServerException](await(connector.getAttachmentList(testRegId)))
      exception.getMessage must include("[AttachmentsConnector][getAttachmentList] unexpected status from backend:")
    }
  }

  "getIncompleteAttachments" must {
    "an attachment list" in {
      stubGet(incompleteAttachmentsApiUrl, OK, Json.toJson(testAttachmentsList).toString())

      val response = await(connector.getIncompleteAttachments(testRegId))

      verify(getRequestedFor(urlEqualTo(incompleteAttachmentsApiUrl)))
      response mustBe testAttachmentsList
    }

    "return an empty attachment list" in {
      stubGet(incompleteAttachmentsApiUrl, OK, Json.toJson(testEmptyAttachmentsList).toString())

      val response = await(connector.getIncompleteAttachments(testRegId))

      verify(getRequestedFor(urlEqualTo(incompleteAttachmentsApiUrl)))
      response mustBe testEmptyAttachmentsList
    }

    "return an exception" in {
      stubGet(incompleteAttachmentsApiUrl, INTERNAL_SERVER_ERROR, Json.toJson(testEmptyAttachmentsList).toString())

      val exception = intercept[InternalServerException](await(connector.getIncompleteAttachments(testRegId)))
      exception.getMessage must include("[AttachmentsConnector][getIncompleteAttachments] unexpected status from backend:")
    }
  }
}