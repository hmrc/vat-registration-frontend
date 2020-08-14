/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.{JsObject, Json}
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class ICLConnectorSpec extends VatRegSpec {

  class Setup {
    val jsResponse = Json.obj("link" -> "exampleaddress.co.uk")
    val successfulResponse = HttpResponse(200, Some(jsResponse))

    val testConnector: ICLConnector = new ICLConnector(
      mockWSHttp,
      mockServicesConfig
    ) {
      override val IClInitialiseUrl: String = "example.url"
      override val IClFEinternal: String = "example.url2"
    }
  }

  "ICLSetup" should {
    "return a JSObject" in new Setup {
      mockHttpPOST[JsObject, HttpResponse]("", successfulResponse)
      val res = await(testConnector.iclSetup(Json.parse("{}").as[JsObject]))
      res mustBe jsResponse
    }
    "return an exception" in new Setup {
      val exception = new Exception
      mockHttpFailedPOST[JsObject, HttpResponse]("", exception)
      intercept[Exception](await(testConnector.iclSetup(Json.parse("{}").as[JsObject])))
    }
  }
  "ICLGetResult" should {
    "return a JSObject" in new Setup {
      mockHttpGET[HttpResponse]("", Future.successful(HttpResponse(200, Some(iclMultipleResults))))
      val res = await(testConnector.iclGetResult(""))
      res mustBe iclMultipleResults
    }
    "return an Exception" in new Setup {
      mockHttpGET[HttpResponse]("", Future.failed(new Exception))
      intercept[Exception](await(testConnector.iclGetResult("")))
    }
  }
}
