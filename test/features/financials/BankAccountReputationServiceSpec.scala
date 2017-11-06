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

package services

import helpers.{S4LMockSugar, VatRegSpec}
import org.mockito.Matchers
import org.mockito.Mockito._

import scala.concurrent.Future

class BankAccountReputationServiceSpec extends VatRegSpec with S4LMockSugar {

  class Setup {
    val service = new BankAccountReputationService(mockBankAccountReputationConnector)
  }

  "Calling bankDetailsModulusCheck" should {
    "return true when accountNumberWithSortCodeIsValid is true" in new Setup {
      when(mockBankAccountReputationConnector.bankAccountModulusCheck(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(validBankCheckJsonResponse))
      service.bankDetailsModulusCheck(validBankAccountDetailsForModulusCheck) returns true
    }

    "return false when accountNumberWithSortCodeIsValid is false" in new Setup {
      when(mockBankAccountReputationConnector.bankAccountModulusCheck(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(invalidBankCheckJsonResponse))
      service.bankDetailsModulusCheck(validBankAccountDetailsForModulusCheck) returns false
    }
  }
}