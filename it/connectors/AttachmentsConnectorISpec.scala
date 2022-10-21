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
import fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.api._
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import support.AppAndStubs

class AttachmentsConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val connector: AttachmentsConnector = app.injector.instanceOf[AttachmentsConnector]
  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val testAttachmentsList: List[AttachmentType] = List[AttachmentType](IdentityEvidence, TaxRepresentativeAuthorisation, OtherAttachments)

  val testEmptyAttachmentsList: List[AttachmentType] = List[AttachmentType]()

  val attachmentUrl = "/vatreg/1/attachments"

  val testStoreAttachmentsAttachedResponseJson: JsObject = Json.obj(
    "method" -> Some(Attached).toString,
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
  }
}