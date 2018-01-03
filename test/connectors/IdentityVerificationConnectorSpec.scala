/*
 * Copyright 2018 HM Revenue & Customs
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
import features.iv.models.{IVSetup, UserData}
import helpers.VatRegSpec
import play.api.libs.json.{JsObject, JsResultException, JsValue, Json}
import config.WSHttp
import org.mockito.Mockito._

class IdentityVerificationConnectorSpec extends VatRegSpec {
  class Setup {
    val connector = new IVConnector {
      override val ivProxyUrl = "tst-url"
      override val ivProxyUri = "tst-url"
      override val ivBase = "tst-url"
      override def useIvStub = true
      override val brdsUrl: String = "tst-url"
      override val brdsUri: String = "tst-url"
      override val ivFeUrl:String = "iv"
      override val http: WSHttp = mockWSHttp
    }
  }


  "Calling getJourneyOutcome" should {
    when(mockVATFeatureSwitch.useIvStub).thenReturn(enabledFeatureSwitch)
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
  "setupIVJourney" should{
    "return link with ivfe base url attached to it" in new Setup {
      mockHttpPOST[IVSetup,JsValue]("test-url",JsObject(Map("link" -> Json.toJson("/myLink"),"journeyLink" -> Json.toJson("foo"))).as[JsValue])
      val res =  await(connector.setupIVJourney(IVSetup("origin","completion","fail",1,UserData("foo","bar","fudge","wizz"))))
      res mustBe JsObject(Map("link" -> Json.toJson("iv/myLink"),"journeyLink" -> Json.toJson("foo"))).as[JsValue]
    }
  }
}
