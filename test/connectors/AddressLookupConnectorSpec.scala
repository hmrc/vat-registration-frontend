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

import fixtures.{AddressLookupConstants, VatRegistrationFixture}
import models.api.Address
import play.api.http.HeaderNames.LOCATION
import play.api.http.HttpVerbs.GET
import play.api.http.Status.{OK, PERMANENT_REDIRECT}
import play.api.libs.json.JsObject
import play.api.mvc.Call
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}

class AddressLookupConnectorSpec extends VatRegSpec with VatRegistrationFixture {

  class Setup {
    val connector: AddressLookupConnector = new AddressLookupConnector(
      mockHttpClient,
      mockServicesConfig
    ) {
      override lazy val addressLookupFrontendUrl: String = "tst-url"
    }

    val dummyUrl = "test-url"
    val redirectUrl = "redirect-url"
    val testAddress = Address(line1 = "line1", line2 = "line2", postcode = Some("postcode"), addressValidated = true)
  }

  "getAddress" should {
    "return an ScrsAddress successfully" in new Setup {
      mockHttpGET[Address]("tst-url", testAddress)
      connector.getAddress("id") returns testAddress
    }

    "return the correct response when an Internal Server Error occurs" in new Setup {
      mockHttpFailedGET[Address](dummyUrl, internalServiceException)
      connector.getAddress("id") failedWith classOf[InternalServerException]
    }
  }


  "getOnRampUrl" should {
    "return a valid call to be used in a redirect" in new Setup {
      val successfulResponse = HttpResponse(PERMANENT_REDIRECT, responseHeaders = Map(LOCATION -> Seq(redirectUrl)))
      mockHttpPOST[JsObject, HttpResponse](dummyUrl, successfulResponse)

      connector.getOnRampUrl(AddressLookupConstants.testAlfConfig) returns Call(GET, redirectUrl)
    }

    "throw an exception when address lookup service does not respond with redirect location" in new Setup {
      val badResponse = HttpResponse(OK, responseHeaders = Map.empty)
      mockHttpPOST[JsObject, HttpResponse](dummyUrl, badResponse)

      connector.getOnRampUrl(AddressLookupConstants.testAlfConfig) failedWith classOf[ALFLocationHeaderNotSetException]
    }
  }
}
