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

package features.bankAccountDetails.services

import features.bankAccountDetails.connectors.BankAccountReputationConnectorImpl
import features.bankAccountDetails.models.BankAccountDetails
import helpers.{S4LMockSugar, VatRegSpec}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._

import scala.concurrent.Future

class BankAccountReputationServiceImplSpec extends VatRegSpec with S4LMockSugar {

  class Setup {
    val service: BankAccountReputationService = new BankAccountReputationService{
      override val bankAccountReputationConnector: BankAccountReputationConnectorImpl = mockBankAccountReputationConnector
    }
  }

  "Calling bankDetailsModulusCheck" should {

    val bankDetails = BankAccountDetails("testName", "12-34-56", "12345678")

    "return true when the json returns a true" in new Setup {
      when(mockBankAccountReputationConnector.bankAccountDetailsModulusCheck(any())(any()))
        .thenReturn(Future.successful(validBankCheckJsonResponse))

      service.bankAccountDetailsModulusCheck(bankDetails) returns true
    }

    "return false when the json returns a false" in new Setup {
      when(mockBankAccountReputationConnector.bankAccountDetailsModulusCheck(any())(any()))
        .thenReturn(Future.successful(invalidBankCheckJsonResponse))

      service.bankAccountDetailsModulusCheck(bankDetails) returns false
    }
  }
}
