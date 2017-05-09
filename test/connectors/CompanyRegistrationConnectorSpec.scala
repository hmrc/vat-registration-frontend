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
import models.external.CoHoCompanyProfile
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.ws.WSHttp

class CompanyRegistrationConnectorSpec extends VatRegSpec with VatRegistrationFixture {

  class Setup {
    val connector = new CompanyRegistrationConnector {
      override val companyRegistrationUrl: String = "tst-url"
      override val companyRegistrationUri: String = "tst-url"
      override val http: WSHttp = mockWSHttp
    }
  }

  implicit val hc = HeaderCarrier()

  "Calling getCompanyRegistrationDetails" should {
    "return a CoHoCompanyProfile successfully" in new Setup {
      mockHttpGET[CoHoCompanyProfile]("tst-url", validCoHoProfile)
      connector.getCompanyRegistrationDetails("id").value returns Some(validCoHoProfile)
    }
    "return the correct response when an Internal Server Error occurs" in new Setup {
      mockHttpFailedGET[CoHoCompanyProfile]("test-url", internalServiceException)
      connector.getCompanyRegistrationDetails("id").value returns None
    }
  }

}

