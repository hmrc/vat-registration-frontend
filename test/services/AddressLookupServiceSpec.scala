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

package services

import common.enums.AddressLookupJourneyIdentifier
import config.{AddressLookupConfiguration, FrontendAppConfig}
import fixtures.AddressLookupConstants
import models.api.Address
import models.external.addresslookup.AddressLookupConfigurationModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.Call
import testHelpers.VatRegSpec

import scala.concurrent.Future

class AddressLookupServiceSpec extends VatRegSpec {

  implicit val appConfig = app.injector.instanceOf[FrontendAppConfig]

  object Config extends AddressLookupConfiguration {
    override def apply(journeyId: AddressLookupJourneyIdentifier.Value, continueRoute: Call): AddressLookupConfigurationModel =
      AddressLookupConstants.testAlfConfig
  }

  object Service extends AddressLookupService(mockAddressLookupConnector, Config)

  val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages = messagesApi.preferred(Seq(Lang("en")))

  "getByAddressId" should {
    "return an ScrsAddress" when {
      "given an id" in {

        when(mockAddressLookupConnector.getAddress(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(testAddress))

        await(Service.getAddressById("testId")) mustBe testAddress
      }
    }
  }

  "getJourneyUrl" should {
    "return a future call" when {
      "given a journey id and a call" in {
        when(mockAddressLookupConnector.getOnRampUrl(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Call("GET", "/test/uri/continue")))

        await(Service.getJourneyUrl(AddressLookupJourneyIdentifier.homeAddress, Call("", "/continue"))) mustBe Call("GET", "/test/uri/continue")
      }
    }
  }

}
