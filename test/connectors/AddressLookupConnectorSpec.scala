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

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.ScrsAddress
import play.api.http.HeaderNames.LOCATION
import play.api.http.HttpVerbs.GET
import play.api.http.Status.{OK, PERMANENT_REDIRECT}
import play.api.libs.json.JsObject
import play.api.mvc.Call
import config.WSHttp
import models.external.addresslookup._
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}

class AddressLookupConnectorSpec extends VatRegSpec with VatRegistrationFixture {

  class Setup {
    val connector = new AddressLookupConnector {
      override val addressLookupFrontendUrl: String = "tst-url"
      override val http: WSHttp = mockWSHttp
    }

    val dummyUrl = "test-url"
    val redirectUrl = "redirect-url"
    val testAddress = ScrsAddress(line1 = "line1", line2 = "line2", postcode = Some("postcode"))

    val testLookupPage = LookupPage(
      title = "testTitle",
      heading = "testHeading",
      filterLabel = "testFilterLable",
      submitLabel = "testSubmitLabel",
      manualAddressLinkText = "linkText"
    )

    val testSelectPage = SelectPage(
      title = "testTitle",
      heading = "testHeading",
      proposalListLimit = 20,
      showSearchAgainLink = true,
      searchAgainLinkText = Some("testLink"),
      editAddressLinkText = "testLink"
    )

    val testEditPage = EditPage(
      title = "testTitle",
      heading = "testHeading",
      line1Label = "L1",
      line2Label = "L2",
      line3Label = "L3",
      postcodeLabel = "testLabel",
      countryLabel = "testLabel",
      submitLabel = "testLabel",
      showSearchAgainLink = true,
      searchAgainLinkText = Some("testLink")
    )

    val testConfirmPage = ConfirmPage(
      title = "testTitle",
      heading = "testHeading",
      showSubHeadingAndInfo = true,
      submitLabel = "testLabel",
      showSearchAgainLink = true,
      showChangeLink = true,
      changeLinkText = Some("testLink")
    )

    val testJourneySetup = AddressJourneyBuilder(
      continueUrl = "/test/continue",
      homeNavHref = "/test/home-nav",
      navTitle = "Test nav title",
      showPhaseBanner = true,
      alphaPhase = false,
      phaseBannerHtml = "<p>Test</p>",
      includeHMRCBranding = false,
      showBackButtons = false,
      deskProServiceName = "testServiceName",
      ukMode = true,
      lookupPage = testLookupPage,
      selectPage = testSelectPage,
      editPage = testEditPage,
      confirmPage = testConfirmPage
    )
  }



  "getAddress" should {
    "return an ScrsAddress successfully" in new Setup {
      mockHttpGET[ScrsAddress]("tst-url", testAddress)
      connector.getAddress("id") returns testAddress
    }

    "return the correct response when an Internal Server Error occurs" in new Setup {
      mockHttpFailedGET[ScrsAddress](dummyUrl, internalServiceException)
      connector.getAddress("id") failedWith classOf[InternalServerException]
    }
  }


  "getOnRampUrl" should {

    "return a valid call to be used in a redirect" in new Setup {
      val successfulResponse = HttpResponse(PERMANENT_REDIRECT, responseHeaders = Map(LOCATION -> Seq(redirectUrl)))
      mockHttpPOST[JsObject, HttpResponse](dummyUrl, successfulResponse)

      connector.getOnRampUrl(testJourneySetup) returns Call(GET, redirectUrl)
    }

    "throw an exception when address lookup service does not respond with redirect location" in new Setup {
      val badResponse = HttpResponse(OK, responseHeaders = Map.empty)
      mockHttpPOST[JsObject, HttpResponse](dummyUrl, badResponse)

      connector.getOnRampUrl(testJourneySetup) failedWith classOf[ALFLocationHeaderNotSetException]
    }
  }
}
