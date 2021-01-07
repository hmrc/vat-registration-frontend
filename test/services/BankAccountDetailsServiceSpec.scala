/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{BankAccount, BankAccountDetails, BeingSetup, NoUKBankAccount, S4LKey}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import testHelpers.VatSpec
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class BankAccountDetailsServiceSpec extends VatSpec {

  val mockBankAccountRepService: BankAccountReputationService = mock[BankAccountReputationService]

  trait Setup {
    val service: BankAccountDetailsService = new BankAccountDetailsService(
      mockVatRegistrationConnector,
      mockS4LService,
      mockBankAccountRepService
    )
  }

  val bankAccountS4LKey: S4LKey[BankAccount] = S4LKey.bankAccountKey

  "fetchBankAccountDetails" should {

    val bankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None)

    "return a BankAccount if one is found in save 4 later" in new Setup {
      when(mockS4LService.fetchAndGetNoAux(eqTo(bankAccountS4LKey))(any(), any(), any()))
        .thenReturn(Future.successful(Some(bankAccount)))

      val result: Option[BankAccount] = await(service.fetchBankAccountDetails)

      result mustBe Some(bankAccount)
    }

    "return a BankAccount if one isn't found in save 4 later but one is found in the backend" in new Setup {
      when(mockS4LService.fetchAndGetNoAux(eqTo(bankAccountS4LKey))(any(), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationConnector.getBankAccount(eqTo(currentProfile.registrationId))(any()))
        .thenReturn(Future.successful(Some(bankAccount)))

      val result: Option[BankAccount] = await(service.fetchBankAccountDetails)

      result mustBe Some(bankAccount)
    }

    "return None if a BankAccount isn't found in save 4 later or the backend" in new Setup {
      when(mockS4LService.fetchAndGetNoAux(eqTo(bankAccountS4LKey))(any(), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationConnector.getBankAccount(eqTo(currentProfile.registrationId))(any()))
        .thenReturn(Future.successful(None))

      val result: Option[BankAccount] = await(service.fetchBankAccountDetails)

      result mustBe None
    }
  }

  "saveBankAccountDetails" should {

    "return a BankAccount and save to the backend and save4later if it is full" in new Setup {
      val fullBankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None)

      when(mockVatRegistrationConnector.patchBankAccount(eqTo(currentProfile.registrationId), eqTo(fullBankAccount))(any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      when(mockS4LService.saveNoAux(eqTo(fullBankAccount), eqTo(bankAccountS4LKey))(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result: BankAccount = await(service.saveBankAccountDetails(fullBankAccount))
      result mustBe fullBankAccount

      verify(mockVatRegistrationConnector, times(1)).patchBankAccount(any(), any())(any())
    }

    "return a BankAccount and save to save 4 later if it is incomplete" in new Setup {
      val incompleteBankAccount = BankAccount(isProvided = true, None, None)

      when(mockS4LService.saveNoAux(eqTo(incompleteBankAccount), eqTo(bankAccountS4LKey))(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result: BankAccount = await(service.saveBankAccountDetails(incompleteBankAccount))
      result mustBe incompleteBankAccount

      verify(mockS4LService, times(1)).saveNoAux(any(), any())(any(), any(), any())
    }
  }

  "bankAccountBlockCompleted" should {

    "return a complete bank account when the supplied bank account has an account and has supplied account details" in new Setup {
      val bankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None)

      val result: Completion[BankAccount] = service.bankAccountBlockCompleted(bankAccount)
      result mustBe Complete(bankAccount)
    }

    "return a complete bank account when the supplied bank account case class does not have a bank account" in new Setup {
      val bankAccount = BankAccount(isProvided = false, None, Some(BeingSetup))

      val result: Completion[BankAccount] = service.bankAccountBlockCompleted(bankAccount)
      result mustBe Complete(bankAccount)
    }

    "return a complete bank account with the account details removed when the supplied bank account case class " +
      "does not have a bank account but has account details" in new Setup {
      val bankAccount = BankAccount(isProvided = false, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), Some(BeingSetup))

      val result: Completion[BankAccount] = service.bankAccountBlockCompleted(bankAccount)

      val expectedBankAccount = BankAccount(isProvided = false, None, Some(BeingSetup))
      result mustBe Complete(expectedBankAccount)
    }

    "return an incomplete bank account when the supplied bank account has an account but no account details" in new Setup {
      val bankAccount = BankAccount(isProvided = true, None, None)

      val result: Completion[BankAccount] = service.bankAccountBlockCompleted(bankAccount)

      result mustBe Incomplete(bankAccount)
    }
  }
}
