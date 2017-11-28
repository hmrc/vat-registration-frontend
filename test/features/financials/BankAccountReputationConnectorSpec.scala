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

import helpers.VatRegSpec
import models.view.vatFinancials.vatBankAccount.ModulusCheckAccount
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.JsValue
import config.WSHttp

import scala.concurrent.Future
import uk.gov.hmrc.http.Upstream5xxResponse

class BankAccountReputationConnectorSpec extends VatRegSpec {

  class Setup {

    val connector = new BankAccountReputationConnect {
      override val bankAccountReputationUrl: String = "test-url"
      override val http: WSHttp = mockWSHttp
    }
  }

  "bankAccountModulusCheck" should {
    "return a valid JSON value" in new Setup{
      mockHttpPOST[ModulusCheckAccount, JsValue](connector.bankAccountReputationUrl, validBankCheckJsonResponse)
      connector.bankAccountModulusCheck(validBankAccountDetailsForModulusCheck) returns validBankCheckJsonResponse
    }

    "throw an exception" in new Setup{
      when(mockWSHttp.POST[ModulusCheckAccount, JsValue](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
        (ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(Upstream5xxResponse(INTERNAL_SERVER_ERROR.toString,500,500)))

      connector.bankAccountModulusCheck(validBankAccountDetailsForModulusCheck) failedWith classOf[Upstream5xxResponse]
    }
  }
}