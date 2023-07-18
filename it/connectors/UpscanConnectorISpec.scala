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
import featuretoggle.FeatureSwitch.StubUpscan
import featuretoggle.FeatureToggleSupport
import fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.api.{AttachmentType, PrimaryIdentityEvidence}
import models.external.upscan.{InProgress, UpscanDetails, UpscanResponse}
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.test.Helpers._
import support.AppAndStubs
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException, NotFoundException, Upstream5xxResponse, SessionId}

class UpscanConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures with FeatureToggleSupport {

  val connector: UpscanConnector = app.injector.instanceOf[UpscanConnector]
  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val testReference = "testReference"
  val testHref = "testHref"

  val upscanInitiateUrl = "/upscan/v2/initiate"
  val testUpscanResponseJson: JsObject = Json.obj(
    "reference" -> testReference,
    "uploadRequest" -> Json.obj(
      "href" -> testHref,
      "fields" -> Json.obj(
        "testField1" -> "test1",
        "testField2" -> "test2"
      )
    )
  )
  val testUpscanResponse: UpscanResponse = UpscanResponse(testReference, testHref, Map("testField1" -> "test1", "testField2" -> "test2"))

  val upscanReferenceUrl = s"/vatreg/$testRegId/upscan-reference"

  val upscanFilesUrl = s"/vatreg/$testRegId/upscan-file-details"

  val upscanDetailsUrl = s"/vatreg/$testRegId/upscan-file-details/$testReference"

  val deleteAllUpscanDetailsUrl = s"/vatreg/$testRegId/upscan-file-details"

  val testUpscanDetailsJson: JsObject = Json.obj(
    "attachmentType" -> "primaryIdentityEvidence",
    "reference" -> testReference,
    "fileStatus" -> "IN_PROGRESS"
  )
  val testUpscanDetails: UpscanDetails = UpscanDetails(attachmentType = PrimaryIdentityEvidence, reference = testReference, fileStatus = InProgress)


  "upscanInitiate" must {
    "return an upscan response when the StubUpscan FS is enabled" in {
      enable(StubUpscan)
      stubPost("/register-for-vat/test-only/upscan/initiate", OK, testUpscanResponseJson.toString())
      val requestBody = Json.obj(
        "callbackUrl" -> appConfig.storeUpscanCallbackUrl,
        "successRedirect" -> s"${appConfig.hostAbsoluteUrl}${controllers.fileupload.routes.UploadingDocumentController.show.url}",
        "errorRedirect" -> s"${appConfig.hostAbsoluteUrl}${controllers.fileupload.routes.UploadDocumentController.show.url}",
        "minimumFileSize" -> 1,
        "maximumFileSize" -> 10485760)

      val response = await(connector.upscanInitiate()(HeaderCarrier(sessionId = Some(SessionId(sessionString)))))

      verify(postRequestedFor(urlEqualTo("/register-for-vat/test-only/upscan/initiate")).withRequestBody(equalToJson(requestBody.toString)))
      response mustBe testUpscanResponse
    }
    "return an UpscanResponse" in {
      disable(StubUpscan)
      stubPost(upscanInitiateUrl, OK, testUpscanResponseJson.toString())
      val requestBody = Json.obj(
        "callbackUrl" -> appConfig.storeUpscanCallbackUrl,
        "successRedirect" -> s"${appConfig.hostAbsoluteUrl}${controllers.fileupload.routes.UploadingDocumentController.show.url}",
        "errorRedirect" -> s"${appConfig.hostAbsoluteUrl}${controllers.fileupload.routes.UploadDocumentController.show.url}",
        "minimumFileSize" -> 1,
        "maximumFileSize" -> 10485760)

      val response = await(connector.upscanInitiate())

      verify(postRequestedFor(urlEqualTo(upscanInitiateUrl)).withRequestBody(equalToJson(requestBody.toString)))
      response mustBe testUpscanResponse
    }

    "return an exception if initiate gives an unexpected response" in {
      stubPost(upscanInitiateUrl, CREATED, testUpscanResponseJson.toString())

      intercept[InternalServerException](await(connector.upscanInitiate()))
    }

    "return an exception if initiate fails" in {
      stubPost(upscanInitiateUrl, INTERNAL_SERVER_ERROR, testUpscanResponseJson.toString())

      intercept[InternalServerException](await(connector.upscanInitiate()))
    }
  }

  "storeUpscanReference" must {
    "return an OK" in {
      stubPost(upscanReferenceUrl, OK, testReference)

      val response = await(connector.storeUpscanReference(testRegId, testReference, PrimaryIdentityEvidence))

      verify(postRequestedFor(urlEqualTo(upscanReferenceUrl)).withRequestBody(equalToJson(Json.obj(
        "reference" -> testReference,
        "attachmentType" -> Json.toJson[AttachmentType](PrimaryIdentityEvidence)
      ).toString())))
      response.status mustBe OK
    }
  }

  "fetchUpscanFileDetails" must {
    "return an UpscanDetails" in {
      stubGet(upscanDetailsUrl, OK, testUpscanDetailsJson.toString())

      val response = await(connector.fetchUpscanFileDetails(testRegId, testReference))

      verify(getRequestedFor(urlEqualTo(upscanDetailsUrl)))
      response mustBe testUpscanDetails
    }

    "return an exception if fetch fails" in {
      stubGet(upscanDetailsUrl, NOT_FOUND, testUpscanDetailsJson.toString())

      intercept[NotFoundException](await(connector.fetchUpscanFileDetails(testRegId, testReference)))
    }
  }

  "deleteUpscanDetails" must {
    "delete and return true" in {
      given
        .audit.writesAudit()
        .audit.writesAuditMerged()

      stubDelete(upscanDetailsUrl, NO_CONTENT, "")

      val response = await(connector.deleteUpscanDetails(testRegId, testReference))

      verify(deleteRequestedFor(urlEqualTo(upscanDetailsUrl)))
      response mustBe true
    }

    "return an exception if delete fails" in {
      given
        .audit.writesAudit()
        .audit.writesAuditMerged()

      stubDelete(upscanDetailsUrl, IM_A_TEAPOT, "")

      intercept[InternalServerException](await(connector.deleteUpscanDetails(testRegId, testReference)))
    }
  }

  "deleteAllUpscanDetails" must {
    "delete and return true" in {
      stubDelete(deleteAllUpscanDetailsUrl, NO_CONTENT, "true")

      val response = await(connector.deleteAllUpscanDetails(testRegId))

      verify(deleteRequestedFor(urlEqualTo(deleteAllUpscanDetailsUrl)))
      response mustBe true
    }

    "return an exception if delete fails" in {
      stubDelete(deleteAllUpscanDetailsUrl, IM_A_TEAPOT, "")

      intercept[InternalServerException](await(connector.deleteAllUpscanDetails(testRegId)))
    }
  }

  "fetchAllUpscanDetails" must {
    "return list of UpscanDetails" in {
      stubGet(upscanFilesUrl, OK, JsArray(List(testUpscanDetailsJson)).toString())

      val response = await(connector.fetchAllUpscanDetails(testRegId))
      verify(getRequestedFor(urlEqualTo(upscanFilesUrl)))
      response mustBe List(testUpscanDetails)
    }

    "return an exception if fetch fails" in {
      stubGet(upscanFilesUrl, INTERNAL_SERVER_ERROR, JsArray(List(testUpscanDetailsJson)).toString())
      intercept[Upstream5xxResponse](await(connector.fetchAllUpscanDetails(testRegId)))
    }
  }
}
