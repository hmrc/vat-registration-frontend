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

package services

import com.typesafe.config.ConfigFactory
import common.enums.AddressLookupJourneyIdentifier
import helpers.VatRegSpec
import models.api.ScrsAddress
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.i18n.MessagesApi
import play.api.mvc.Call

import scala.concurrent.Future

class AddressLookupServiceSpec extends VatRegSpec {

  val testAddress = ScrsAddress(line1 = "line1", line2 = "line2", postcode = Some("postcode"))

  val testService = new AddressLookupService {
    override val addressLookupConnector = mockAddressLookupConnector
    override val addressConfig = ConfigFactory.load.getConfig("address-journeys")
    override val addressLookupContinueUrl = "/test/uri"
  }

  implicit val messagesApi = app.injector.instanceOf(classOf[MessagesApi])

  "getByAddressId" should {
    "return an ScrsAddress" when {
      "given an id" in {

        when(mockAddressLookupConnector.getAddress(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(testAddress))

        val result = await(testService.getAddressById("testId")) mustBe testAddress
      }
    }
  }

  "getJourneyUrl" should {
    "return a future call" when {
      "given a journey id and a call" in {

        when(mockAddressLookupConnector.getOnRampUrl(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Call("GET", "/test/uri/continue")))

        val result = await(testService.getJourneyUrl(AddressLookupJourneyIdentifier.homeAddress, Call("", "/continue"))) mustBe Call("GET", "/test/uri/continue")
      }
    }
  }

  "buildJourneyJson" should {
    val resultHomeAddress       = testService.buildJourneyJson(Call("GET", "/continue"), AddressLookupJourneyIdentifier.homeAddress)
    val resultAddress4Years     = testService.buildJourneyJson(Call("GET", "/continue-1"), AddressLookupJourneyIdentifier.addressThreeYearsOrLess)
    val resultBusinessActivites = testService.buildJourneyJson(Call("GET", "/continue-1"), AddressLookupJourneyIdentifier.businessActivities)

    "return an AddressJourneyBuilder and validate common items" in {

      resultHomeAddress.showPhaseBanner mustBe true
      resultAddress4Years.showPhaseBanner mustBe true
      resultBusinessActivites.showPhaseBanner mustBe true

      resultHomeAddress.ukMode mustBe false
      resultAddress4Years.ukMode mustBe false
      resultBusinessActivites.ukMode mustBe true
    }
  }
}
