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

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.external.CoHoRegisteredOfficeAddress
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.ws.WSHttp

class IncorporationInformationConnectorSpec extends VatRegSpec with VatRegistrationFixture {

  class Setup {
    val connector = new IncorporationInformationConnector {
      override val incorpInfoUrl: String = "tst-url"
      override val incorpInfoUri: String = "tst-url"
      override val http: WSHttp = mockWSHttp
    }

    val testAddress = CoHoRegisteredOfficeAddress(
      "premises", "addressLine1", None,
      "locality", None, None, None, None
    )
  }

  implicit val hc = HeaderCarrier()

  "Calling getRegisteredOfficeAddress" should {
    "return a CoHoRegisteredOfficeAddress successfully" in new Setup {
      mockHttpGET[CoHoRegisteredOfficeAddress]("tst-url", testAddress)
      connector.getRegisteredOfficeAddress("id") returnsSome testAddress
    }

    "return the correct response when an Internal Server Error occurs" in new Setup {
      mockHttpFailedGET[CoHoRegisteredOfficeAddress]("test-url", internalServiceException)
      connector.getRegisteredOfficeAddress("id").returnsNone
    }
  }

}

