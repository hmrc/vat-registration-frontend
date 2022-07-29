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

package services

import connectors.mocks.MockRegistrationApiConnector
import models.{BankAccount, BankAccountDetails, BeingSetup, OverseasBankDetails, S4LKey}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.Assertion
import testHelpers.VatSpec
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class BankAccountDetailsServiceSpec extends VatSpec with MockRegistrationApiConnector {

  val mockBankAccountRepService: BankAccountReputationService = mock[BankAccountReputationService]

  trait Setup {
    val service: BankAccountDetailsService = new BankAccountDetailsService(
      mockRegistrationApiConnector,
      mockS4LService,
      mockBankAccountRepService
    )
  }

  val bankAccountS4LKey: S4LKey[BankAccount] = BankAccount.s4lKey

  "saveHasCompanyBankAccount" should {

    def verifyBankAccountPatch(service: BankAccountDetailsService, hasBankAccount: Boolean, bankAccount: BankAccount): Assertion = {
      when(mockS4LService.fetchAndGet(eqTo(bankAccountS4LKey), any(), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockS4LService.clearKey(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      mockGetSection[BankAccount](testRegId, Some(bankAccount))

      mockReplaceSection[BankAccount](testRegId, bankAccount)

      val result: BankAccount = await(service.saveHasCompanyBankAccount(hasBankAccount))

      verify(mockS4LService, never()).save(eqTo(bankAccount))(any(), any(), any(), any())
      result mustBe bankAccount
    }

    def verifyIncompleteBankAccount(service: BankAccountDetailsService, hasBankAccount: Boolean) = {
      val incompleteBankAccount: BankAccount = BankAccount(hasBankAccount, None, None, None)

      when(mockS4LService.fetchAndGet(eqTo(bankAccountS4LKey), any(), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockS4LService.save(eqTo(incompleteBankAccount))(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      mockGetSection[BankAccount](testRegId, None)

      await(service.saveHasCompanyBankAccount(hasBankAccount)) mustBe incompleteBankAccount
    }

    "patch and return an already persisted completed ProvidedBankAccount" in new Setup {
      val existingProvidedBankAccountState: BankAccount =
        BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None, None)

      verifyBankAccountPatch(service, hasBankAccount = true, existingProvidedBankAccountState)
    }

    "patch and return an already persisted completed NoBankAccount with a reason" in new Setup {
      val existingNoBankAccountState: BankAccount =
        BankAccount(isProvided = false, None, None, Some(BeingSetup))

      verifyBankAccountPatch(service, hasBankAccount = false, existingNoBankAccountState)
    }

    "return a new blank BankAccount model if no previous bank account state available" in new Setup {
      verifyIncompleteBankAccount(service, hasBankAccount = false)
      verifyIncompleteBankAccount(service, hasBankAccount = true)
    }
  }

  "fetchBankAccountDetails" should {

    val bankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None, None)
    val overseasBankAccount = BankAccount(isProvided = true, None, Some(OverseasBankDetails("testName", "123456", "12345678")), None)

    "return a BankAccount if one is found in save 4 later" in new Setup {
      when(mockS4LService.fetchAndGet(eqTo(bankAccountS4LKey), any(), any(), any()))
        .thenReturn(Future.successful(Some(bankAccount)))

      val result: Option[BankAccount] = await(service.fetchBankAccountDetails)

      result mustBe Some(bankAccount)
    }

    "return a BankAccount if one is found in save 4 later and the account is from overseas" in new Setup {
      when(mockS4LService.fetchAndGet(eqTo(bankAccountS4LKey), any(), any(), any()))
        .thenReturn(Future.successful(Some(overseasBankAccount)))

      val result: Option[BankAccount] = await(service.fetchBankAccountDetails)

      result mustBe Some(overseasBankAccount)
    }

    "return a BankAccount if one isn't found in save 4 later but one is found in the backend" in new Setup {
      when(mockS4LService.fetchAndGet(eqTo(bankAccountS4LKey), any(), any(), any()))
        .thenReturn(Future.successful(None))

      mockGetSection[BankAccount](testRegId, Some(bankAccount))

      val result: Option[BankAccount] = await(service.fetchBankAccountDetails)

      result mustBe Some(bankAccount)
    }

    "return None if a BankAccount isn't found in save 4 later or the backend" in new Setup {
      when(mockS4LService.fetchAndGet(eqTo(bankAccountS4LKey), any(), any(), any()))
        .thenReturn(Future.successful(None))

      mockGetSection[BankAccount](testRegId, None)

      val result: Option[BankAccount] = await(service.fetchBankAccountDetails)

      result mustBe None
    }
  }

  "saveBankAccountDetails" should {

    "return a BankAccount and save to the backend and save4later if it is full" in new Setup {
      val fullBankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None, None)

      mockReplaceSection[BankAccount](testRegId, fullBankAccount)

      when(mockS4LService.clearKey(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result: BankAccount = await(service.saveBankAccountDetails(fullBankAccount))
      result mustBe fullBankAccount

      verify(mockS4LService, never()).save(eqTo(fullBankAccount))(any(), any(), any(), any())
      verify(mockRegistrationApiConnector, times(1)).replaceSection[BankAccount](eqTo(currentProfile.registrationId), any[BankAccount](), any())(any(), any(), any())
    }

    "return a BankAccount and save to save 4 later if it is incomplete" in new Setup {
      val incompleteBankAccount = BankAccount(isProvided = true, None, None, None)

      when(mockS4LService.save(eqTo(incompleteBankAccount))(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result: BankAccount = await(service.saveBankAccountDetails(incompleteBankAccount))
      result mustBe incompleteBankAccount

      verify(mockS4LService, times(1)).save(any())(any(), any(), any(), any())
    }
  }

  "bankAccountBlockCompleted" should {

    "return a complete bank account when the supplied bank account has an account and has supplied account details" in new Setup {
      val bankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None, None)

      val result: Completion[BankAccount] = service.bankAccountBlockCompleted(bankAccount)
      result mustBe Complete(bankAccount)
    }

    "return a complete bank account when the supplied bank account case class does not have a bank account" in new Setup {
      val bankAccount = BankAccount(isProvided = false, None, None, Some(BeingSetup))

      val result: Completion[BankAccount] = service.bankAccountBlockCompleted(bankAccount)
      result mustBe Complete(bankAccount)
    }

    "return a complete bank account with the account details removed when the supplied bank account case class " +
      "does not have a bank account but has account details" in new Setup {
      val bankAccount = BankAccount(isProvided = false, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None, Some(BeingSetup))

      val result: Completion[BankAccount] = service.bankAccountBlockCompleted(bankAccount)

      val expectedBankAccount = BankAccount(isProvided = false, None, None, Some(BeingSetup))
      result mustBe Complete(expectedBankAccount)
    }

    "return a complete bank account with the account details and reason removed when overseas" in new Setup {
      val bankAccount = BankAccount(
        isProvided = true,
        Some(BankAccountDetails("testName", "testCode", "testAccNumber")),
        Some(OverseasBankDetails("testName", "1234567890", "123456")),
        Some(BeingSetup)
      )

      val result: Completion[BankAccount] = service.bankAccountBlockCompleted(bankAccount)

      val expectedBankAccount = BankAccount(isProvided = true, None, Some(OverseasBankDetails("testName", "1234567890", "123456")), None)
      result mustBe Complete(expectedBankAccount)
    }

    "return an incomplete bank account when the supplied bank account has an account but no account details" in new Setup {
      val bankAccount = BankAccount(isProvided = true, None, None, None)

      val result: Completion[BankAccount] = service.bankAccountBlockCompleted(bankAccount)

      result mustBe Incomplete(bankAccount)
    }
  }
}
