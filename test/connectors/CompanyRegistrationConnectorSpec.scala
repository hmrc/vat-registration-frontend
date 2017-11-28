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

import config.WSHttp
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.NotFoundException

import scala.concurrent.Future

class CompanyRegistrationConnectorSpec extends VatRegSpec with VatRegistrationFixture {

  class Setup {
    val connector = new CompanyRegistrationConnect {
      override val companyRegistrationUrl: String = "tst-url"
      override val companyRegistrationUri: String = "tst-url"
      override val http: WSHttp = mockWSHttp
    }
  }

  "Calling getCompanyRegistrationDetails" should {
    "return a CoHoCompanyProfile successfully" in new Setup {
      mockHttpGET[JsValue]("tst-url", Json.toJson(validCoHoProfile))
      await(connector.getTransactionId("id")) mustBe validCoHoProfile.transactionId
    }
    "return the correct response when an Internal Server Error occurs" in new Setup {
      when(mockWSHttp.GET[JsValue](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new NotFoundException(NOT_FOUND.toString)))

      intercept[NotFoundException](await(connector.getTransactionId("id")))
    }
  }

}

