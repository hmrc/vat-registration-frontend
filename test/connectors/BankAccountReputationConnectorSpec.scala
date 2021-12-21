/*
 * Copyright 2022 HM Revenue & Customs
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

import config.FrontendAppConfig
import models.BankAccountDetails
import org.mockito.Mockito._
import org.mockito._
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.JsValue
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.Upstream5xxResponse

import scala.concurrent.Future

class BankAccountReputationConnectorSpec extends VatRegSpec {

  class Setup {
    val appConfig = app.injector.instanceOf[FrontendAppConfig]
    val connector: BankAccountReputationConnector = new BankAccountReputationConnector(
      mockHttpClient,
      appConfig
    )
  }

  "bankAccountModulusCheck" should {
    val bankDetails = BankAccountDetails("testName", "12-34-56", "12345678")

    "return a valid JSON value" in new Setup {
      mockHttpPOST[BankAccountDetails, JsValue]("/v2/validateBankDetails", validBankCheckJsonResponse)

      connector.validateBankDetails(bankDetails) returns validBankCheckJsonResponse
    }

    "throw an exception" in new Setup {
      when(mockHttpClient.POST[BankAccountDetails, JsValue](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
        (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(Upstream5xxResponse(INTERNAL_SERVER_ERROR.toString, 500, 500)))

      connector.validateBankDetails(bankDetails) failedWith classOf[Upstream5xxResponse]
    }
  }
}
