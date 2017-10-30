/*
 * Copyright 2017 HM Revenue & Customs
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

import common.enums.IVResult
import helpers.VatRegSpec
import play.api.libs.json.{JsResultException, JsValue, Json}
import uk.gov.hmrc.play.http.ws.WSHttp

class IdentityVerificationConnectorSpec extends VatRegSpec {
  class Setup {
    val connector = new IdentityVerificationConnector {
      override val brdsUrl: String = "tst-url"
      override val brdsUri: String = "tst-url"
      override val http: WSHttp = mockWSHttp
    }
  }

  "Calling getJourneyOutcome" should {
    val jsonValidResultSuccess = Json.parse(
      """
        | {
        |   "result": "Success",
        |   "token": "aaaa-bbbb-ccc-aaaa"
        | }
      """.stripMargin)

    val jsonValidResultFailedIV = Json.parse(
      """
        | {
        |   "result": "FailedIV",
        |   "token": "aaaa-bbbb-ccc-aaaa"
        | }
      """.stripMargin)

    val jsonInvalidResult = Json.parse(
      """
        | {
        |   "result": "Blabla",
        |   "token": "aaaa-bbbb-ccc-aaaa"
        | }
      """.stripMargin)

    "return a Success when 200 valid response is returned with valid result" in new Setup {
      mockHttpGET[JsValue]("test-url", jsonValidResultSuccess)
      connector.getJourneyOutcome("testJourneyId") returns IVResult.Success
    }

    "return a FailedIV when 200 valid response is returned with valid result" in new Setup {
      mockHttpGET[JsValue]("test-url", jsonValidResultFailedIV)
      connector.getJourneyOutcome("testJourneyId") returns IVResult.FailedIV
    }

    "return an exception when 200 valid response is returned with invalid result" in new Setup {
      mockHttpGET[JsValue]("test-url", jsonInvalidResult)
      connector.getJourneyOutcome("testJourneyId") failedWith classOf[JsResultException]
    }

    "return an exception when the Http call returns an exception" in new Setup {
      mockHttpFailedGET[JsValue]("test-url", internalServiceException)
      connector.getJourneyOutcome("testJourneyId") failedWith internalServiceException
    }
  }
}
