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

import config.FrontendAppConfig
import models.api.{AttachmentType, IdentityEvidence, OtherAttachments, TaxRepresentativeAuthorisation}
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers._
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}

class AttachmentsConnectorSpec extends VatRegSpec {

  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val connector = new AttachmentsConnector(mockHttpClient, appConfig)

  val testAttachments: List[AttachmentType] = List[AttachmentType](IdentityEvidence, TaxRepresentativeAuthorisation, OtherAttachments)

  "getAttachments" should {
    "return a list of attachments" in {
      mockHttpGET(appConfig.attachmentsApiUrl(testRegId), HttpResponse(OK, Json.toJson(testAttachments).toString))

      val result = connector.getAttachmentList(testRegId)

      await(result) mustBe testAttachments
    }

    "return an empty list of attachments" in {
      mockHttpGET(appConfig.attachmentsApiUrl(testRegId), HttpResponse(OK, Json.toJson(List[AttachmentType]()).toString()))

      val result = connector.getAttachmentList(testRegId)

      await(result) mustBe Nil
    }

    "throw an exception" in {
      mockHttpGET(appConfig.attachmentsApiUrl(testRegId), HttpResponse(INTERNAL_SERVER_ERROR, ""))

      val result = connector.getAttachmentList(testRegId)

      intercept[InternalServerException](await(result)).message mustBe "[AttachmentsConnector][getAttachmentList] unexpected status from backend: 500"
    }
  }

}