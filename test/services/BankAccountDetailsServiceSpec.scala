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
import featuretoggle.FeatureToggleSupport
import models._
import models.api.{IndeterminateStatus, InvalidStatus, ValidStatus}
import models.bars.BankAccountType.{Business, Personal}
import models.bars._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Request
import play.api.test.FakeRequest
import services.LockService.lockoutReason
import testHelpers.VatSpec

import scala.concurrent.Future

class BankAccountDetailsServiceSpec
    extends VatSpec
    with FeatureToggleSupport
    with GuiceOneAppPerSuite
    with BeforeAndAfterEach
    with MockRegistrationApiConnector {

  val mockBarsService: BarsService           = mock[BarsService]
  val mockLockService: LockService           = mock[LockService]
  val mockBarsAuditService: BarsAuditService = mock[BarsAuditService]

  trait Setup {
    val service: BankAccountDetailsService = new BankAccountDetailsService(
      mockRegistrationApiConnector,
      mockBarsService,
      mockLockService,
      mockBarsAuditService
    )

    def stubAudit(): Unit =
      when(mockBarsAuditService.sendBarsAuditEvent(any(), any(), any(), any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))
  }

  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  implicit val request: Request[_]          = FakeRequest()

  "getBankAccount" should {

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

      verifyReplaceSectionIsCalled(testRegId, fullBankAccount)
    }
  }

  "saveAnswerForHasCompanyBankAccountPage" when {
    Seq(true, false).foreach { submittedAnswer =>
      s"submitted answer is '$submittedAnswer" should {
        "save the submitted answer and return the new model when there is no saved data" in new Setup {
          private val modelToSave = BankAccount(isProvided = submittedAnswer, details = None, reason = None, bankAccountType = None)

          mockGetSection[BankAccount](testRegId, None)
          mockReplaceSection[BankAccount](testRegId, modelToSave)

          val result: BankAccount = await(service.saveAnswerCanProvideBankAccountDetailsPage(canProvideBankAccountDetails = submittedAnswer))

          result mustBe modelToSave
          verifyReplaceSectionIsCalled(testRegId, modelToSave)
        }

        "return the existing data without saving when submitted answer matches existing data" in new Setup {
          private val existingData =
            BankAccount(isProvided = submittedAnswer, details = None, reason = Some(DontWantToProvide), bankAccountType = None)
          private val modelToSave = BankAccount(isProvided = submittedAnswer, details = None, reason = None, bankAccountType = None)

          mockGetSection[BankAccount](testRegId, Some(existingData))
          mockReplaceSection[BankAccount](testRegId, modelToSave)

          val result: BankAccount = await(service.saveAnswerCanProvideBankAccountDetailsPage(canProvideBankAccountDetails = submittedAnswer))

          result mustBe existingData
          verifyReplaceSectionIsNotCalled(testRegId, modelToSave)
        }
      }
    }

    "submitted answer is 'true' and existing data was 'false'" should {
      "save new data and delete the existing reason" in new Setup {
        private val existingData = BankAccount(isProvided = false, details = None, reason = Some(DontWantToProvide), bankAccountType = None)
        private val modelToSave  = BankAccount(isProvided = true, details = None, reason = None, bankAccountType = None)

        mockGetSection[BankAccount](testRegId, Some(existingData))
        mockReplaceSection[BankAccount](testRegId, modelToSave)

        val result: BankAccount = await(service.saveAnswerCanProvideBankAccountDetailsPage(canProvideBankAccountDetails = true))

        result mustBe modelToSave
        verifyReplaceSectionIsCalled(testRegId, modelToSave)
      }
    }

    "submitted answer is 'false' and existing data was 'true'" should {
      "save new data and delete old bank details" when {
        "details were valid" in new Setup {
          private val existingData = BankAccount(
            isProvided = true,
            details = Some(BankAccountDetails("n", "n", "s", None, Some(ValidStatus))),
            reason = None,
            bankAccountType = Some(Business))
          private val modelToSave = existingData.copy(isProvided = false, details = None, bankAccountType = None)

          mockGetSection[BankAccount](testRegId, Some(existingData))
          mockReplaceSection[BankAccount](testRegId, modelToSave)

          val result: BankAccount = await(service.saveAnswerCanProvideBankAccountDetailsPage(canProvideBankAccountDetails = false))

          result mustBe modelToSave
          verifyReplaceSectionIsCalled(testRegId, modelToSave)
        }

        "details were indeterminate" in new Setup {
          private val existingData = BankAccount(
            isProvided = true,
            details = Some(BankAccountDetails("n", "n", "s", None, Some(IndeterminateStatus))),
            reason = None,
            bankAccountType = Some(Business))
          private val modelToSave = existingData.copy(isProvided = false, details = None, bankAccountType = None)

          mockGetSection[BankAccount](testRegId, Some(existingData))
          mockReplaceSection[BankAccount](testRegId, modelToSave)

          val result: BankAccount = await(service.saveAnswerCanProvideBankAccountDetailsPage(canProvideBankAccountDetails = false))

          result mustBe modelToSave
          verifyReplaceSectionIsCalled(testRegId, modelToSave)
        }

        "save new data details had not been BARS checked so have no status" in new Setup {
          private val existingData = BankAccount(
            isProvided = true,
            details = Some(BankAccountDetails("n", "n", "s", None, status = None)),
            reason = None,
            bankAccountType = Some(Business))
          private val modelToSave = existingData.copy(isProvided = false, details = None, bankAccountType = None)

          mockGetSection[BankAccount](testRegId, Some(existingData))
          mockReplaceSection[BankAccount](testRegId, modelToSave)

          val result: BankAccount = await(service.saveAnswerCanProvideBankAccountDetailsPage(canProvideBankAccountDetails = false))

          result mustBe modelToSave
          verifyReplaceSectionIsCalled(testRegId, modelToSave)
        }
      }

      "save new data and do NOT delete old bank details" when {
        "details were invalid" in new Setup {
          private val existingData = BankAccount(
            isProvided = true,
            details = Some(BankAccountDetails("n", "n", "s", None, Some(InvalidStatus))),
            reason = Some(NameChange),
            bankAccountType = Some(Business))
          private val modelToSave = existingData.copy(isProvided = false)

          mockGetSection[BankAccount](testRegId, Some(existingData))
          mockReplaceSection[BankAccount](testRegId, modelToSave)

          val result: BankAccount = await(service.saveAnswerCanProvideBankAccountDetailsPage(canProvideBankAccountDetails = false))

          result mustBe modelToSave
          verifyReplaceSectionIsCalled(testRegId, modelToSave)
        }
      }

      "save new data and delete old 'isProvided' value when that is all that has been entered" in new Setup {
        private val existingData = BankAccount(isProvided = true, details = None, reason = None, bankAccountType = None)
        private val modelToSave  = existingData.copy(isProvided = false)

        mockGetSection[BankAccount](testRegId, Some(existingData))
        mockReplaceSection[BankAccount](testRegId, modelToSave)

        val result: BankAccount = await(service.saveAnswerCanProvideBankAccountDetailsPage(canProvideBankAccountDetails = false))

        result mustBe modelToSave
        verifyReplaceSectionIsCalled(testRegId, modelToSave)
      }

      "save new data and delete old 'isProvided' and 'bankAccountType' values when bank account details have not been entered" in new Setup {
        private val existingData = BankAccount(isProvided = true, details = None, reason = None, bankAccountType = Some(Personal))
        private val modelToSave  = existingData.copy(isProvided = false, bankAccountType = None)

        mockGetSection[BankAccount](testRegId, Some(existingData))
        mockReplaceSection[BankAccount](testRegId, modelToSave)

        val result: BankAccount = await(service.saveAnswerCanProvideBankAccountDetailsPage(canProvideBankAccountDetails = false))

        result mustBe modelToSave
        verifyReplaceSectionIsCalled(testRegId, modelToSave)
      }
    }
  }

  "saveAnswerForBankAccountNotProvidedPage" when {
    "update any existing data with the new answer" when {
      "there is existing data" in new Setup {
        private val existingData = BankAccount(
          isProvided = false,
          details = Some(BankAccountDetails("n", "n", "s", None, Some(InvalidStatus))),
          reason = Some(OverseasAccount),
          bankAccountType = None)
        private val expectedModelToBeSavedAndReturned = BankAccount(
          isProvided = false,
          details = Some(BankAccountDetails("n", "n", "s", None, Some(InvalidStatus))),
          reason = Some(AccountNotInBusinessName),
          bankAccountType = None)

        mockGetSection[BankAccount](testRegId, Some(existingData))
        mockReplaceSection[BankAccount](testRegId, expectedModelToBeSavedAndReturned)

        val result: BankAccount = await(service.saveAnswerForBankAccountNotProvidedPage(reason = AccountNotInBusinessName))

        result mustBe expectedModelToBeSavedAndReturned
        verifyReplaceSectionIsCalled(testRegId, expectedModelToBeSavedAndReturned)
      }

      "there is NO existing data" in new Setup {
        private val expectedModelToBeSavedAndReturned =
          BankAccount(isProvided = false, details = None, reason = Some(AccountNotInBusinessName), bankAccountType = None)

        mockGetSection[BankAccount](testRegId, None)
        mockReplaceSection[BankAccount](testRegId, expectedModelToBeSavedAndReturned)

        val result: BankAccount = await(service.saveAnswerForBankAccountNotProvidedPage(reason = AccountNotInBusinessName))

        result mustBe expectedModelToBeSavedAndReturned
        verifyReplaceSectionIsCalled(testRegId, expectedModelToBeSavedAndReturned)
      }
    }
  }

  "saveAnswerForBankAccountTypePage" should {
    "create and save new details if none already exist" in new Setup {
      val expected: BankAccount = BankAccount(isProvided = true, None, None, Some(BankAccountType.Business))

      mockGetSection[BankAccount](testRegId, None)
      mockReplaceSection[BankAccount](testRegId, expected)

      val result: BankAccount = await(service.saveAnswerForBankAccountTypePage(BankAccountType.Business))

      result mustBe expected
      verifyReplaceSectionIsCalled(testRegId, expected)
    }

    "update existing model and save answer" in new Setup {
      val existing: BankAccount = BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None, None)
      val expected: BankAccount = existing.copy(bankAccountType = Some(BankAccountType.Business))

      mockGetSection[BankAccount](testRegId, Some(existing))
      mockReplaceSection[BankAccount](testRegId, expected)

      val result: BankAccount = await(service.saveAnswerForBankAccountTypePage(BankAccountType.Business))

      result mustBe expected
      verifyReplaceSectionIsCalled(testRegId, expected)
    }

    "update existing model and override an existing answer data to save new answer" in new Setup {
      val existing: BankAccount =
        BankAccount(isProvided = true, Some(BankAccountDetails("testName", "testCode", "testAccNumber")), None, Some(BankAccountType.Business))
      val expected: BankAccount = existing.copy(bankAccountType = Some(BankAccountType.Personal))

      mockGetSection[BankAccount](testRegId, Some(existing))
      mockReplaceSection[BankAccount](testRegId, expected)

      val result: BankAccount = await(service.saveAnswerForBankAccountTypePage(BankAccountType.Personal))

      result mustBe expected
      verifyReplaceSectionIsCalled(testRegId, expected)
    }
  }

  "saveAnswersForBankAccountDetailsPage" when {
    "able to fetch existing details from the backend" should {
      "add the bank details to the existing data and save" in new Setup {
        val bankAccountDetails: BankAccountDetails = BankAccountDetails("name", "number", "sortCode")
        val existing: BankAccount                  = BankAccount(isProvided = true, None, None, Some(BankAccountType.Business))
        val expected: BankAccount                  = existing.copy(details = Some(bankAccountDetails))

        mockGetSection[BankAccount](testRegId, Some(existing))
        mockReplaceSection[BankAccount](testRegId, expected)

        val result: Either[Unit, BankAccount] = await(service.saveAnswersForBankAccountDetailsPage(bankAccountDetails))

        result mustBe Right(expected)
        verifyReplaceSectionIsCalled(testRegId, expected)
      }
    }

    "unable to fetch any existing details from the backend" should {
      "return a Left" in new Setup {
        val bankAccountDetails: BankAccountDetails = BankAccountDetails("name", "number", "sortCode")
        val expected: BankAccount                  = BankAccount(isProvided = true, Some(bankAccountDetails), None, Some(BankAccountType.Business))

        mockGetSection[BankAccount](testRegId, None)

        val result: Either[Unit, BankAccount] = await(service.saveAnswersForBankAccountDetailsPage(bankAccountDetails))

        result mustBe Left(())
        verifyReplaceSectionIsNotCalled(testRegId, expected)
      }
    }
  }

  "verifyAndSaveBankAccountDetails" should {

    val bankAccountDetails = BankAccountDetails("testName", "12345678", "123456", None)
    val bankAccountType    = BankAccountType.Business
    val barsResponseMock   = mock[BarsVerificationResponse]
    when(barsResponseMock.accountExists).thenReturn(BarsResponse.Yes)
    when(barsResponseMock.nameMatches).thenReturn(BarsResponse.Yes)
    val mockBarsResponse = Some(barsResponseMock)

    def verifyAuditSent(): Unit =
      verify(mockBarsAuditService, times(1)).sendBarsAuditEvent(any(), any(), any(), any(), any(), any(), any())(any(), any(), any())

    "save details and return BarsSuccess when verification passes with ValidStatus" in new Setup {
      stubAudit()

      private val detailsToBeSaved =
        BankAccount(isProvided = true, Some(bankAccountDetails.copy(status = Some(ValidStatus))), None, Some(bankAccountType))
      when(mockBarsService.verifyBankDetails(any(), any())(any()))
        .thenReturn(Future.successful(BarsResponseAndVerificationStatus(ValidStatus, mockBarsResponse)))
      when(mockLockService.getBarsAttemptsUsed(any()))
        .thenReturn(Future.successful(1))
      mockReplaceSection[BankAccount](testRegId, detailsToBeSaved)

      val result: BarsVerificationOutcome = await(service.verifyAndSaveBankAccountDetails(bankAccountDetails, bankAccountType, optReason = None))

      result mustBe BarsSuccess
      verify(mockLockService, times(1)).getBarsAttemptsUsed(eqTo(testRegId))
      verifyReplaceSectionIsCalled(testRegId, detailsToBeSaved)
      verifyAuditSent()
      reset(mockBarsService, mockLockService, mockBarsAuditService)
    }

    "save details and return BarsSuccess when verification passes with IndeterminateStatus" in new Setup {
      stubAudit()

      private val detailsToBeSaved =
        BankAccount(isProvided = true, Some(bankAccountDetails.copy(status = Some(IndeterminateStatus))), None, Some(bankAccountType))
      when(mockBarsService.verifyBankDetails(any(), any())(any()))
        .thenReturn(Future.successful(BarsResponseAndVerificationStatus(IndeterminateStatus, mockBarsResponse)))
      when(mockLockService.getBarsAttemptsUsed(any()))
        .thenReturn(Future.successful(1))
      mockReplaceSection[BankAccount](testRegId, detailsToBeSaved)

      val result: BarsVerificationOutcome = await(service.verifyAndSaveBankAccountDetails(bankAccountDetails, bankAccountType, optReason = None))

      result mustBe BarsSuccess
      verify(mockLockService, times(1)).getBarsAttemptsUsed(eqTo(testRegId))
      verifyReplaceSectionIsCalled(testRegId, detailsToBeSaved)
      verifyAuditSent()
      reset(mockBarsService, mockLockService, mockBarsAuditService)
    }

    "save details and return BarsFailedNotLocked when verification fails and user is not locked" in new Setup {
      stubAudit()

      private val detailsToBeSaved =
        BankAccount(isProvided = true, Some(bankAccountDetails.copy(status = Some(InvalidStatus))), None, Some(bankAccountType))
      when(mockBarsService.verifyBankDetails(any(), any())(any()))
        .thenReturn(Future.successful(BarsResponseAndVerificationStatus(InvalidStatus, mockBarsResponse)))
      when(mockLockService.incrementBarsAttemptsAndReturnNewFailedCount(any()))
        .thenReturn(Future.successful(1))
      mockReplaceSection[BankAccount](testRegId, detailsToBeSaved)

      val result: BarsVerificationOutcome = await(service.verifyAndSaveBankAccountDetails(bankAccountDetails, bankAccountType, optReason = None))

      result mustBe BarsFailedNotLocked
      verify(mockLockService, times(1)).incrementBarsAttemptsAndReturnNewFailedCount(eqTo(testRegId))
      verifyReplaceSectionIsCalled(testRegId, detailsToBeSaved)
      verifyAuditSent()
      reset(mockBarsService, mockLockService, mockBarsAuditService)
    }

    "save details and return BarsLockedOut when verification fails and user is locked" in new Setup {
      stubAudit()

      private val detailsToBeSaved =
        BankAccount(isProvided = true, Some(bankAccountDetails.copy(status = Some(InvalidStatus))), Some(lockoutReason), Some(bankAccountType))
      when(mockBarsService.verifyBankDetails(any(), any())(any()))
        .thenReturn(Future.successful(BarsResponseAndVerificationStatus(InvalidStatus, mockBarsResponse)))
      when(mockLockService.incrementBarsAttemptsAndReturnNewFailedCount(any()))
        .thenReturn(Future.successful(3))
      mockReplaceSection[BankAccount](testRegId, detailsToBeSaved)

      val result: BarsVerificationOutcome =
        await(service.verifyAndSaveBankAccountDetails(bankAccountDetails, bankAccountType, optReason = Some(lockoutReason)))

      result mustBe BarsLockedOut
      verify(mockLockService, times(1)).incrementBarsAttemptsAndReturnNewFailedCount(eqTo(testRegId))
      verifyReplaceSectionIsCalled(testRegId, detailsToBeSaved)
      verifyAuditSent()
      reset(mockBarsService, mockLockService, mockBarsAuditService)
    }
  }

  "saveAnswerForBankAccountNotProvidedPage" when {
    "update existing model to only replace the 'reason' and 'isProvided' values" in new Setup {
      val existing: BankAccount = BankAccount(isProvided = true, None, None, Some(BankAccountType.Business))
      val expected: BankAccount = BankAccount(isProvided = false, None, Some(BeingSetupOrNameChange), Some(BankAccountType.Business))

      mockGetSection[BankAccount](testRegId, Some(existing))
      mockReplaceSection[BankAccount](testRegId, expected)

      val result: BankAccount = await(service.saveAnswerForBankAccountNotProvidedPage(BeingSetupOrNameChange))

      result mustBe expected
    }
  }

}
