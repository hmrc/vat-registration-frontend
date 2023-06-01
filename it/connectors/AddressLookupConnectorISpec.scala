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

import common.enums.AddressLookupJourneyIdentifier._
import config.{AddressLookupConfiguration, FrontendAppConfig}
import itutil.IntegrationSpecBase
import models.api.{Address, Country}
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.AddressLookupService
import support.AppAndStubs
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import play.api.mvc.Request

class AddressLookupConnectorISpec extends IntegrationSpecBase with AppAndStubs {

  implicit val req : Request[_] = this.request 
  val alfConnector: AddressLookupConnector = app.injector.instanceOf[AddressLookupConnector]
  val addressLookupService: AddressLookupService = app.injector.instanceOf[AddressLookupService]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  implicit val messages = messagesApi.preferred(Seq(Lang("en")))

  "getting an address out of Address Lookup Frontend" should {
    "obtain expected address from JSON response from ALF" when {
      "address is found in ALF" in {
        given()
          .address("addressId", "16 Coniston Court", "Holland road", "UK", "BN3 1JU").isFound

        await(alfConnector.getAddress("addressId")) mustBe Address(
          line1 = "16 Coniston Court",
          line2 = Some("Holland road"),
          country = Some(Country(Some("UK"), Some("United Kingdom"))),
          postcode = Some("BN3 1JU"),
          addressValidated = true
        )
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
    val alfConfig = app.injector.instanceOf[AddressLookupConfiguration]
    val journeyModel = alfConfig(homeAddress, Call("GET", "continueUrl"), useUkMode = false)

    "return a URL for redirecting the user off to ALF" when {
      "Location header is present" in {
        given()
          .alfeJourney.initialisedSuccessfully()

        await(alfConnector.getOnRampUrl(journeyModel)) mustBe Call("GET", "continueUrl")
      }
    }

    "throw ALFLocationHeaderNotSetException" when {
      "no Location header received from ALF" in {
        given()
          .alfeJourney.notInitialisedAsExpected()

        intercept[ALFLocationHeaderNotSetException] {
          await(alfConnector.getOnRampUrl(journeyModel))
        }
      }
    }

    "throw Upstream5xxResponse exception" when {
      "ALF fails to handle the request" in {
        given()
          .alfeJourney.failedToInitialise()

        intercept[UpstreamErrorResponse] {
          await(alfConnector.getOnRampUrl(journeyModel))
        }
      }
    }
  }
}
