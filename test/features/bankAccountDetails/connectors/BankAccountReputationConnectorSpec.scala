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

package features.bankAccountDetails.connectors

import config.WSHttp
import features.bankAccountDetails.models.BankAccountDetails
import helpers.VatRegSpec
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.Upstream5xxResponse
import org.mockito.Mockito._
import org.mockito._

import scala.concurrent.Future

class BankAccountReputationConnectorSpec extends VatRegSpec {

  class Setup {

    val connector = new BankAccountReputationConnector {
      override val bankAccountReputationUrl: String = "test-url"
      override val http: WSHttp = mockWSHttp
    }
  }

  "bankAccountModulusCheck" should {

    val bankDetails = BankAccountDetails("testName", "12-34-56", "12345678")

    "return a valid JSON value" in new Setup{
      mockHttpPOST[BankAccountDetails, JsValue](connector.bankAccountReputationUrl, validBankCheckJsonResponse)

      connector.bankAccountDetailsModulusCheck(bankDetails) returns validBankCheckJsonResponse
    }

    "throw an exception" in new Setup{
      when(mockWSHttp.POST[BankAccountDetails, JsValue](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
        (ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(Upstream5xxResponse(INTERNAL_SERVER_ERROR.toString,500,500)))

      connector.bankAccountDetailsModulusCheck(bankDetails) failedWith classOf[Upstream5xxResponse]
    }
  }
}
