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

import models.AddressLookupJourneyId
import models.api.ScrsAddress
import play.api.mvc.Call
import support.AppAndStubs
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException, Upstream5xxResponse}
import uk.gov.hmrc.play.test.UnitSpec

class AddressLookupConnectorISpec extends UnitSpec with AppAndStubs {

  def alfConnector: AddressLookupConnect = app.injector.instanceOf(classOf[AddressLookupConnect])

  "getting an address out of Address Lookup Frontend" should {

    "obtain expected address from JSON response from ALF" when {

      "address is found in ALF" in {
        given()
          .address("addressId", "16 Coniston Court", "Holland road", "United Kingdom", "BN3 1JU").isFound

        await(alfConnector.getAddress("addressId")) shouldBe ScrsAddress(
          line1 = "16 Coniston court",
          line2 = "Holland road",
          country = Some("United Kingdom"),
          postcode = Some("BN3 1JU"))
      }
    }

    "throw a NotFoundException" when {
      "address is not found in ALF" in {
        given()
          .address("addressId", "16 Coniston Court", "Holland road", "United Kingdom", "BN3 1JU").isNotFound

        intercept[NotFoundException] {
          await(alfConnector.getAddress("addressId"))
        }
      }
    }
  }


  "initialising ALF journey" should {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val journeyId: AddressLookupJourneyId = AddressLookupJourneyId("journeyId")

    "return a URL for redirecting the user off to ALF" when {
      "Location header is present" in {
        given()
          .journey("journeyId")
          .initialisedSuccessfully()

        await(alfConnector.getOnRampUrl(Call("GET", "/"))) shouldBe Call("GET", "continueUrl")
      }
    }

    "throw ALFLocationHeaderNotSetException" when {
      "no Location header received from ALF" in {
        given()
          .journey("journeyId")
          .notInitialisedAsExpected()

        intercept[ALFLocationHeaderNotSetException] {
          await(alfConnector.getOnRampUrl(Call("GET", "/")))
        }
      }
    }

    "throw Upstream5xxResponse exception" when {
      "ALF fails to handle the request" in {
        given()
          .journey("journeyId")
          .failedToInitialise()

        intercept[Upstream5xxResponse] {
          await(alfConnector.getOnRampUrl(Call("GET", "/")))
        }
      }
    }

  }

}
