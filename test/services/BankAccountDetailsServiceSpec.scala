/*
 * Copyright 2026 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.mocks.MockRegistrationApiConnector
import featuretoggle.FeatureSwitch.UseNewBarsVerify
import featuretoggle.FeatureToggleSupport.{disable, enable}
import models.{BankAccount, BankAccountDetails, BeingSetupOrNameChange}
import models.api.{BankAccountDetailsStatus, IndeterminateStatus, InvalidStatus, ValidStatus}
import models.bars.BankAccountType
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.{Assertion, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Request
import play.api.test.FakeRequest
import testHelpers.VatSpec

import scala.concurrent.Future

class BankAccountDetailsServiceSpec extends VatSpec with GuiceOneAppPerSuite with MockRegistrationApiConnector {

  val mockBankAccountRepService: BankAccountReputationService = mock[BankAccountReputationService]
  val mockBarsService: BarsService                            = mock[BarsService]

  trait Setup {
    val service: BankAccountDetailsService = new BankAccountDetailsService(
      mockRegistrationApiConnector,
      mockBankAccountRepService,
      mockBarsService
    )
  }

  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  implicit val request: Request[_]          = FakeRequest()

  "getBankAccountDetails" should {

    val bankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None)

    "return a BankAccount" in new Setup {
      mockGetSection[BankAccount](testRegId, Some(bankAccount))

      val result: Option[BankAccount] = await(service.getBankAccount)

      result mustBe Some(bankAccount)
    }

    "return None if a BankAccount isn't found" in new Setup {
      mockGetSection[BankAccount](testRegId, None)

      val result: Option[BankAccount] = await(service.getBankAccount)

      result mustBe None
    }
  }

  "saveBankAccount" should {
    "return a BankAccount and save to the backend" in new Setup {
      val fullBankAccount: BankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None)

      mockReplaceSection[BankAccount](testRegId, fullBankAccount)

      val result: BankAccount = await(service.saveBankAccount(fullBankAccount))
      result mustBe fullBankAccount

      verify(mockRegistrationApiConnector, times(1))
        .replaceSection[BankAccount](eqTo(currentProfile.registrationId), any[BankAccount](), any())(any(), any(), any(), any())
    }
  }

  "saveHasCompanyBankAccount" should {
    "patch and return an already persisted completed ProvidedBankAccount" in new Setup {
      val existingProvidedBankAccountState: BankAccount =
        BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None, Some(BankAccountType.Business))

      mockGetSection[BankAccount](testRegId, Some(existingProvidedBankAccountState))
      mockReplaceSection[BankAccount](testRegId, existingProvidedBankAccountState)

      val result: BankAccount = await(service.saveHasCompanyBankAccount(hasBankAccount = true))

      result mustBe existingProvidedBankAccountState
    }

    "patch and return an already persisted completed NoBankAccount with a reason" in new Setup {
      val existingNoBankAccountState: BankAccount =
        BankAccount(isProvided = false, None, Some(BeingSetupOrNameChange), None)

      mockGetSection[BankAccount](testRegId, Some(existingNoBankAccountState))
      mockReplaceSection[BankAccount](testRegId, existingNoBankAccountState)

      val result: BankAccount = await(service.saveHasCompanyBankAccount(hasBankAccount = false))

      result mustBe existingNoBankAccountState
    }

    "return a new blank BankAccount model if the user changes answer" in new Setup {
      val existingNoBankAccountState: BankAccount =
        BankAccount(isProvided = false, None, Some(BeingSetupOrNameChange), None)
      val clearedBankAccount: BankAccount =
        BankAccount(isProvided = true, None, None, None)

      mockGetSection[BankAccount](testRegId, Some(existingNoBankAccountState))
      mockReplaceSection[BankAccount](testRegId, clearedBankAccount)

      val result: BankAccount = await(service.saveHasCompanyBankAccount(hasBankAccount = true))

      result mustBe clearedBankAccount
    }

    "return a new blank BankAccount model if no previous bank account state available" in new Setup {
      def verifyIncompleteBankAccount(service: BankAccountDetailsService, hasBankAccount: Boolean): Assertion = {
        val incompleteBankAccount: BankAccount = BankAccount(hasBankAccount, None, None, None)

        mockGetSection[BankAccount](testRegId, None)
        mockReplaceSection[BankAccount](testRegId, incompleteBankAccount)

        await(service.saveHasCompanyBankAccount(hasBankAccount)) mustBe incompleteBankAccount
      }

      verifyIncompleteBankAccount(service, hasBankAccount = false)
      verifyIncompleteBankAccount(service, hasBankAccount = true)
    }
  }

  "selectBarsEndpoint" should {

    "call barsService when UseNewBarsVerify is enabled" in new Setup {
      enable(UseNewBarsVerify)
      val bankAccountDetails = BankAccountDetails("testName", "12345678", "123456", None)
      val bankAccountType = Some(BankAccountType.Business)

      when(mockBarsService.verifyBankDetails(any(), any())(any()))
        .thenReturn(Future.successful(ValidStatus))

      val result: BankAccountDetailsStatus = await(service.selectBarsEndpoint(bankAccountDetails, bankAccountType))

      result mustBe ValidStatus
      verify(mockBarsService, times(1)).verifyBankDetails(eqTo(bankAccountDetails), eqTo(BankAccountType.Business))(any())
      verifyNoInteractions(mockBankAccountRepService)
      reset(mockBarsService)
    }

    "call bankAccountRepService when UseNewBarsVerify is disabled" in new Setup {
      disable(UseNewBarsVerify)
      val bankAccountDetails = BankAccountDetails("testName", "12345678", "123456", None)

      when(mockBankAccountRepService.validateBankDetails(any())(any(), any()))
        .thenReturn(Future.successful(ValidStatus))

      val result: BankAccountDetailsStatus = await(service.selectBarsEndpoint(bankAccountDetails, None))

      result mustBe ValidStatus
      verify(mockBankAccountRepService, times(1)).validateBankDetails(eqTo(bankAccountDetails))(any(), any())
      verifyNoInteractions(mockBarsService)
      reset(mockBankAccountRepService)

    }

    "throw IllegalStateException when UseNewBarsVerify is enabled but bankAccountType is None" in new Setup {
      enable(UseNewBarsVerify)
      val bankAccountDetails = BankAccountDetails("testName", "12345678", "123456", None)

      intercept[IllegalStateException] {
        await(service.selectBarsEndpoint(bankAccountDetails, None))
      }.getMessage mustBe "bankAccountType is required when UseNewBarsVerify is enabled"

      verifyNoInteractions(mockBarsService)
      verifyNoInteractions(mockBankAccountRepService)
      reset(mockBarsService)
    }

    "return IndeterminateStatus when UseNewBarsVerify is enabled and BARS returns indeterminate" in new Setup {
      enable(UseNewBarsVerify)
      val bankAccountDetails = BankAccountDetails("testName", "12345678", "123456", None)
      val bankAccountType = Some(BankAccountType.Personal)

      when(mockBarsService.verifyBankDetails(any(), any())(any()))
        .thenReturn(Future.successful(IndeterminateStatus))

      val result: BankAccountDetailsStatus = await(service.selectBarsEndpoint(bankAccountDetails, bankAccountType))

      result mustBe IndeterminateStatus
      reset(mockBarsService)
    }

    "return InvalidStatus when UseNewBarsVerify is disabled and bankAccountRepService returns invalid" in new Setup {
      disable(UseNewBarsVerify)
      val bankAccountDetails = BankAccountDetails("testName", "12345678", "123456", None)

      when(mockBankAccountRepService.validateBankDetails(any())(any(), any()))
        .thenReturn(Future.successful(InvalidStatus))

      val result: BankAccountDetailsStatus = await(service.selectBarsEndpoint(bankAccountDetails, None))

      result mustBe InvalidStatus
      reset(mockBankAccountRepService)
    }
  }

  "saveNoUkBankAccountDetails" should {
    "save a BankAccount reason and remove bank account details" in new Setup {
      val existing: BankAccount = BankAccount(isProvided = true, None, None, Some(BankAccountType.Business))
      val expected: BankAccount = BankAccount(isProvided = false, None, Some(BeingSetupOrNameChange), None)

      mockGetSection[BankAccount](testRegId, Some(existing))
      mockReplaceSection[BankAccount](testRegId, expected)

      val result: BankAccount = await(service.saveNoUkBankAccountDetails(BeingSetupOrNameChange))

      result mustBe expected
    }
  }

  "saveBankAccountType" should {
    "update bankAccountType on an existing BankAccount with no BankAccountType" in new Setup {
      val existing: BankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None, None)
      val expected: BankAccount = existing.copy(bankAccountType = Some(BankAccountType.Business))

      mockGetSection[BankAccount](testRegId, Some(existing))
      mockReplaceSection[BankAccount](testRegId, expected)

      val result: BankAccount = await(service.saveBankAccountType(BankAccountType.Business))

      result mustBe expected
    }

    "update bankAccountType to Personal on an existing Business BankAccount" in new Setup {
      val existing: BankAccount =
        BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None, Some(BankAccountType.Business))
      val expected: BankAccount = existing.copy(bankAccountType = Some(BankAccountType.Personal))

      mockGetSection[BankAccount](testRegId, Some(existing))
      mockReplaceSection[BankAccount](testRegId, expected)

      val result: BankAccount = await(service.saveBankAccountType(BankAccountType.Personal))

      result mustBe expected
    }

    "create a new BankAccount with bankAccountType if none exists" in new Setup {
      val expected: BankAccount = BankAccount(isProvided = true, None, None, Some(BankAccountType.Business))

      mockGetSection[BankAccount](testRegId, None)
      mockReplaceSection[BankAccount](testRegId, expected)

      val result: BankAccount = await(service.saveBankAccountType(BankAccountType.Business))

      result mustBe expected
    }
  }
}
