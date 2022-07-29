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

package connectors

import config.FrontendAppConfig
import models.api._
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}

class AttachmentsConnectorSpec extends VatRegSpec {

  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val connector = new AttachmentsConnector(mockHttpClient, appConfig)

  val testAttachmentList: List[AttachmentType] = List[AttachmentType](IdentityEvidence, TaxRepresentativeAuthorisation, OtherAttachments)

  val testEmptyAttachmentList: List[AttachmentType] = List[AttachmentType]()

  val testStoreAttachmentsOtherResponseJson: JsObject = Json.obj(
    "method" -> Some(Other).toString,
  )
  val testStoreAttachmentsAttachedResponseJson: JsObject = Json.obj(
    "method" -> Some(Attached).toString,
  )
  val testStoreAttachmentsPostResponseJson: JsObject = Json.obj(
    "method" -> Some(Post).toString,
  )

  "getAttachments" should {
    "return a list of attachments" in {
      mockHttpGET(appConfig.attachmentsApiUrl(testRegId), HttpResponse(OK, Json.toJson(testAttachmentList).toString))

      val result = connector.getAttachmentList(testRegId)

      await(result) mustBe testAttachmentList
    }

    "return an empty list of attachments" in {
      mockHttpGET(appConfig.attachmentsApiUrl(testRegId), HttpResponse(OK, Json.toJson(testEmptyAttachmentList).toString()))

      val result = connector.getAttachmentList(testRegId)

      await(result) mustBe testEmptyAttachmentList
    }

    "throw an exception" in {
      mockHttpGET(appConfig.attachmentsApiUrl(testRegId), HttpResponse(INTERNAL_SERVER_ERROR, ""))

      val result = connector.getAttachmentList(testRegId)

      intercept[InternalServerException](await(result)).message mustBe "[AttachmentsConnector][getAttachmentList] unexpected status from backend: 500"
    }
  }

  "getIncompleteAttachments" should {
    "return a list of incomplete attachments" in {
      mockHttpGET(appConfig.incompleteAttachmentsApiUrl(testRegId), HttpResponse(OK, Json.toJson(testAttachmentList).toString))

      val result = connector.getIncompleteAttachments(testRegId)

      await(result) mustBe testAttachmentList
    }

    "return an empty list of incomplete attachments" in {
      mockHttpGET(appConfig.incompleteAttachmentsApiUrl(testRegId), HttpResponse(OK, Json.toJson(List.empty[AttachmentType]).toString()))

      val result = connector.getIncompleteAttachments(testRegId)

      await(result) mustBe empty
    }

    "throw an exception" in {
      mockHttpGET(appConfig.incompleteAttachmentsApiUrl(testRegId), HttpResponse(INTERNAL_SERVER_ERROR, ""))

      val result = connector.getIncompleteAttachments(testRegId)

      intercept[InternalServerException](await(result)).message mustBe "[AttachmentsConnector][getIncompleteAttachments] unexpected status from backend: 500"
    }
  }
}
