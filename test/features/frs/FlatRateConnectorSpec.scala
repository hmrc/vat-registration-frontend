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

package connectors {

  import fixtures.VatRegistrationFixture
  import helpers.VatRegSpec
  import models.api.VatFlatRateScheme
  import config.WSHttp
  import scala.language.postfixOps

  class FlatRateConnectorSpec extends VatRegSpec with VatRegistrationFixture {

    class Setup {
      val connector: FlatRateConnector = new RegistrationConnector {
        override val vatRegUrl: String = "tst-url"
        override val http: WSHttp = mockWSHttp
      }
    }

    "Calling updateVatFlatRateScheme" should {
      "return the correct VatResponse when the microservice completes and returns a VatFlatRateScheme model" in new Setup {
        mockHttpPATCH[VatFlatRateScheme, VatFlatRateScheme]("tst-url", validVatFlatRateScheme)
        connector.updateVatFlatRateScheme("tstID", validVatFlatRateScheme) returns validVatFlatRateScheme
      }
      "return the correct VatResponse when a Forbidden response is returned by the microservice" in new Setup {
        mockHttpFailedPATCH[VatFlatRateScheme, VatFlatRateScheme]("tst-url", forbidden)
        connector.updateVatFlatRateScheme("tstID", validVatFlatRateScheme) failedWith forbidden
      }
      "return a Not Found VatResponse when the microservice returns a NotFound response (No VatRegistration in database)" in new Setup {
        mockHttpFailedPATCH[VatFlatRateScheme, VatFlatRateScheme]("tst-url", notFound)
        connector.updateVatFlatRateScheme("tstID", validVatFlatRateScheme) failedWith notFound
      }
      "return the correct VatResponse when an Internal Server Error response is returned by the microservice" in new Setup {
        mockHttpFailedPATCH[VatFlatRateScheme, VatFlatRateScheme]("tst-url", internalServiceException)
        connector.updateVatFlatRateScheme("tstID", validVatFlatRateScheme) failedWith internalServiceException
      }
    }

  }
}
