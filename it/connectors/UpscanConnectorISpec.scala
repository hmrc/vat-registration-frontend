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
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.external.upscan.{InProgress, UpscanDetails, UpscanResponse}
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers._
import support.AppAndStubs
import uk.gov.hmrc.http.{InternalServerException, NotFoundException, Upstream5xxResponse}

class UpscanConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

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

  val upscanDetailsUrl = s"/vatreg/$testRegId/upscan-file-details/$testReference"
  val testUpscanDetailsJson: JsObject = Json.obj(
    "reference" -> testReference,
    "fileStatus" -> "IN_PROGRESS"
  )
  val testUpscanDetails: UpscanDetails = UpscanDetails(reference = testReference, fileStatus = InProgress)


  "upscanInitiate" must {
    "return an UpscanResponse" in {
      stubPost(upscanInitiateUrl, OK, testUpscanResponseJson.toString())
      val requestBody = Json.obj(
        "callbackUrl" -> appConfig.storeUpscanCallbackUrl,
        "success_action_redirect" -> controllers.test.routes.FileUploadController.callbackCheck().url,
        "minimumFileSize" -> 0,
        "maximumFileSize" -> 10485760,
        "expectedContentType" -> "text/plain")

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

      intercept[Upstream5xxResponse](await(connector.upscanInitiate()))
    }
  }

  "storeUpscanReference" must {
    "return an OK" in {
      stubPost(upscanReferenceUrl, OK, testReference)

      val response = await(connector.storeUpscanReference(testRegId, testReference))

      verify(postRequestedFor(urlEqualTo(upscanReferenceUrl)).withRequestBody(equalToJson(JsString(testReference).toString())))
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
}
