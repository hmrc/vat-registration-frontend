/*
 * Copyright 2024 HM Revenue & Customs
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

import itFixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.api.SicCode
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.Helpers._
import support.AppAndStubs
import uk.gov.hmrc.http.UpstreamErrorResponse

class ICLConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val connector: ICLConnector = app.injector.instanceOf[ICLConnector]
  val iclJourneySetupUrl = "/internal/initialise-journey"
  val iclGetResultUrl = "/test-url"

  val testJourneySetupJson: JsObject = Json.obj(
    "redirectUrl" -> "/test",
    "journeySetupDetails" -> Json.obj(
      "customMessages" -> Json.obj(
        "summary" -> Json.obj(),
        "summaryCy" -> Json.obj()
      ),
      "sicCodes" -> Json.arr("1234" -> "desc")
    )
  )

  val testSicCode: SicCode = SicCode(sicCodeId, sicCodeDesc, sicCodeDescCy)

  val testGetResultJson: JsObject = Json.obj(
    "sicCodes" -> Json.arr(
      Json.toJson(testSicCode)
    )
  )

  val testIclResponseJson: JsObject = Json.obj("link" -> "/test")

  "iclSetup" must {
    "return json containing a redirect link" in new Setup {
      stubPost(iclJourneySetupUrl, OK, testIclResponseJson.toString())

      val res: JsValue = await(connector.iclSetup(testJourneySetupJson))

      res mustBe testIclResponseJson
    }
    "throw an exception if ICL returns an unexpected status" in new Setup {
      stubPost(iclJourneySetupUrl, INTERNAL_SERVER_ERROR, "")

      intercept[UpstreamErrorResponse] {
        await(connector.iclSetup(testJourneySetupJson))
      }
    }
  }

  "iclGetResult" must {
    "return the selected SIC codes" in new Setup {
      stubGet(iclGetResultUrl, OK, testGetResultJson.toString())

      val res: JsValue = await(connector.iclGetResult(iclGetResultUrl))

      res mustBe testGetResultJson
    }
    "throw an exception if ICL returns an unexpected status" in new Setup {
      stubGet(iclGetResultUrl, INTERNAL_SERVER_ERROR, testGetResultJson.toString())

      intercept[UpstreamErrorResponse] {
        await(connector.iclGetResult(iclGetResultUrl))
      }
    }
  }

}
