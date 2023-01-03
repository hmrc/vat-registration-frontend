/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.mocks.MockRegistrationApiConnector
import models.{BankAccount, BankAccountDetails, BeingSetupOrNameChange}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.Assertion
import testHelpers.VatSpec

class BankAccountDetailsServiceSpec extends VatSpec with MockRegistrationApiConnector {

  val mockBankAccountRepService: BankAccountReputationService = mock[BankAccountReputationService]

  trait Setup {
    val service: BankAccountDetailsService = new BankAccountDetailsService(
      mockRegistrationApiConnector,
      mockBankAccountRepService
    )
  }

  "fetchBankAccountDetails" should {

    val bankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None)

    "return a BankAccount" in new Setup {
      mockGetSection[BankAccount](testRegId, Some(bankAccount))

      val result: Option[BankAccount] = await(service.fetchBankAccountDetails)

      result mustBe Some(bankAccount)
    }

    "return None if a BankAccount isn't found" in new Setup {
      mockGetSection[BankAccount](testRegId, None)

      val result: Option[BankAccount] = await(service.fetchBankAccountDetails)

      result mustBe None
    }
  }

  "saveBankAccountDetails" should {
    "return a BankAccount and save to the backend" in new Setup {
      val fullBankAccount: BankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None)

      mockReplaceSection[BankAccount](testRegId, fullBankAccount)

      val result: BankAccount = await(service.saveBankAccountDetails(fullBankAccount))
      result mustBe fullBankAccount

      verify(mockRegistrationApiConnector, times(1)).replaceSection[BankAccount](eqTo(currentProfile.registrationId), any[BankAccount](), any())(any(), any(), any())
    }
  }

  "saveHasCompanyBankAccount" should {
    "patch and return an already persisted completed ProvidedBankAccount" in new Setup {
      val existingProvidedBankAccountState: BankAccount =
        BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None)

      mockGetSection[BankAccount](testRegId, Some(existingProvidedBankAccountState))
      mockReplaceSection[BankAccount](testRegId, existingProvidedBankAccountState)

      val result: BankAccount = await(service.saveHasCompanyBankAccount(hasBankAccount = true))

      result mustBe existingProvidedBankAccountState
    }

    "patch and return an already persisted completed NoBankAccount with a reason" in new Setup {
      val existingNoBankAccountState: BankAccount =
        BankAccount(isProvided = false, None, Some(BeingSetupOrNameChange))

      mockGetSection[BankAccount](testRegId, Some(existingNoBankAccountState))
      mockReplaceSection[BankAccount](testRegId, existingNoBankAccountState)

      val result: BankAccount = await(service.saveHasCompanyBankAccount(hasBankAccount = false))

      result mustBe existingNoBankAccountState
    }

    "return a new blank BankAccount model if the user changes answer" in new Setup {
      val existingNoBankAccountState: BankAccount =
        BankAccount(isProvided = false, None, Some(BeingSetupOrNameChange))
      val clearedBankAccount: BankAccount =
        BankAccount(isProvided = true, None, None)

      mockGetSection[BankAccount](testRegId, Some(existingNoBankAccountState))
      mockReplaceSection[BankAccount](testRegId, clearedBankAccount)

      val result: BankAccount = await(service.saveHasCompanyBankAccount(hasBankAccount = true))

      result mustBe clearedBankAccount
    }

    "return a new blank BankAccount model if no previous bank account state available" in new Setup {
      def verifyIncompleteBankAccount(service: BankAccountDetailsService, hasBankAccount: Boolean): Assertion = {
        val incompleteBankAccount: BankAccount = BankAccount(hasBankAccount, None, None)

        mockGetSection[BankAccount](testRegId, None)

        mockReplaceSection[BankAccount](testRegId, incompleteBankAccount)

        await(service.saveHasCompanyBankAccount(hasBankAccount)) mustBe incompleteBankAccount
      }

      verifyIncompleteBankAccount(service, hasBankAccount = false)
      verifyIncompleteBankAccount(service, hasBankAccount = true)
    }
  }

}
