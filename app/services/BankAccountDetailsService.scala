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
import connectors.RegistrationApiConnector
import controllers.bankdetails.routes
import featuretoggle.FeatureSwitch.UseNewBarsVerify
import featuretoggle.FeatureToggleSupport
import models._
import models.api.{BankAccountDetailsStatus, IndeterminateStatus, InvalidStatus, ValidStatus}
import models.bars._
import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result}
import services.LockService.{attemptLimit, lockoutReason}
import uk.gov.hmrc.http.HeaderCarrier
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BankAccountDetailsService @Inject() (
    val regApiConnector: RegistrationApiConnector,
    bankAccountRepService: BankAccountReputationService,
    barsService: BarsService,
    lockService: LockService,
    barsAuditService: BarsAuditService
) extends LoggingUtil
    with FeatureToggleSupport {

  def getBankAccount(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[Option[BankAccount]] = {
    infoLog(s"[BankAccountDetailsService][getBankAccount] Fetching bank account for registration: ${profile.registrationId}")
    regApiConnector.getSection[BankAccount](profile.registrationId)
  }

  def saveBankAccount(bankAccount: BankAccount)(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[BankAccount] = {
    infoLog(s"[BankAccountDetailsService][saveBankAccount] Saving bank account for registration")
    regApiConnector.replaceSection[BankAccount](profile.registrationId, bankAccount)
  }

  def saveAnswerForHasCompanyBankAccountPage(hasBankAccount: Boolean)(implicit
      hc: HeaderCarrier,
      profile: CurrentProfile,
      appConfig: FrontendAppConfig,
      ex: ExecutionContext,
      request: Request[_]): Future[BankAccount] =
    if (isEnabled(UseNewBarsVerify)) {
      newSaveAnswerForCanProvideBankAccountDetailsPage(hasBankAccount)
    } else {
      oldSaveAnswerForCanProvideBankAccountDetailsPage(hasBankAccount)
    }

  private def newSaveAnswerForCanProvideBankAccountDetailsPage(canProvideBankAccountDetails: Boolean)(implicit
      hc: HeaderCarrier,
      profile: CurrentProfile,
      ex: ExecutionContext,
      request: Request[_]): Future[BankAccount] = {
    def hasExistingBankDetailsAnd(invalid: Boolean, existingDetails: BankAccount): Boolean =
      existingDetails.isProvided && {
        val status = existingDetails.details.flatMap(_.status)
        // Valid and Indeterminate statuses are both considered successful in frontend user journey
        if (invalid) status.contains(InvalidStatus) else status.exists(_ != InvalidStatus)
      }

    getBankAccount flatMap {
      case None =>
        val newModel = BankAccount(isProvided = canProvideBankAccountDetails, details = None, reason = None, bankAccountType = None)
        saveBankAccount(newModel)

      case Some(existingAccount) if existingAccount.isProvided == canProvideBankAccountDetails =>
        Future.successful(existingAccount)

      case Some(existingAccount) if canProvideBankAccountDetails =>
        val changingFromOldReasonToNewBankDetailsJourney =
          existingAccount.copy(isProvided = canProvideBankAccountDetails, reason = None)
        saveBankAccount(changingFromOldReasonToNewBankDetailsJourney)

      case Some(existingAccount) if !canProvideBankAccountDetails && hasExistingBankDetailsAnd(invalid = true, existingAccount) =>
        // Do not delete invalid bank details, they must be sent to the API at submission
        val changingFromOldInvalidBankDetailsToNewReasonJourney =
          existingAccount.copy(isProvided = canProvideBankAccountDetails)
        saveBankAccount(changingFromOldInvalidBankDetailsToNewReasonJourney)

      case Some(existingAccount) if !canProvideBankAccountDetails && hasExistingBankDetailsAnd(invalid = false, existingAccount) =>
        val changingFromOldValidBankDetailsToNewReasonJourney =
          existingAccount.copy(isProvided = canProvideBankAccountDetails, details = None, bankAccountType = None)
        saveBankAccount(changingFromOldValidBankDetailsToNewReasonJourney)

      case Some(existingAccount) if !canProvideBankAccountDetails =>
        val changingFromOldUnBarsCheckedBankDetailsToNewReasonJourney =
          existingAccount.copy(isProvided = canProvideBankAccountDetails, details = None, bankAccountType = None)
        saveBankAccount(changingFromOldUnBarsCheckedBankDetailsToNewReasonJourney)
    }
  }

  private def oldSaveAnswerForCanProvideBankAccountDetailsPage(hasBankAccount: Boolean)(implicit
      hc: HeaderCarrier,
      profile: CurrentProfile,
      ex: ExecutionContext,
      request: Request[_]): Future[BankAccount] = {
    val bankAccount = getBankAccount map {
      case Some(BankAccount(oldHasBankAccount, _, _, _)) if oldHasBankAccount != hasBankAccount =>
        infoLog(
          s"[BankAccountDetailsService][saveHasCompanyBankAccount] hasBankAccount changed, clearing existing bank account details for registration")
        BankAccount(hasBankAccount, None, None, None)
      case Some(bankAccountDetails) =>
        bankAccountDetails.copy(isProvided = hasBankAccount)
      case None =>
        infoLog(s"[BankAccountDetailsService][saveHasCompanyBankAccount] No existing bank account found, creating new for registration")
        BankAccount(hasBankAccount, None, None, None)
    }
    bankAccount flatMap saveBankAccount
  }

  def saveAnswerForBankAccountNotProvidedPage(reason: NoUKBankAccount)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      profile: CurrentProfile,
      request: Request[_],
      appConfig: FrontendAppConfig): Future[BankAccount] =
    if (isEnabled(UseNewBarsVerify)) {
      for {
        maybeExistingBankDetails <- getBankAccount
        updatedModel = BankAccount(
          isProvided = false,
          details = maybeExistingBankDetails.flatMap(_.details),
          reason = Some(reason),
          bankAccountType = maybeExistingBankDetails.flatMap(_.bankAccountType)
        )
        result <- saveBankAccount(updatedModel)
      } yield result
    } else {
      saveBankAccount(BankAccount(isProvided = false, details = None, reason = Some(reason), bankAccountType = None))
    }

  def saveAnswerForBankAccountTypePage(bankAccountType: BankAccountType)(implicit
      hc: HeaderCarrier,
      profile: CurrentProfile,
      ex: ExecutionContext,
      request: Request[_]): Future[BankAccount] =
    getBankAccount
      .map {
        case Some(existing) => existing.copy(bankAccountType = Some(bankAccountType))
        case None           => BankAccount(isProvided = true, details = None, reason = None, bankAccountType = Some(bankAccountType))
      }
      .flatMap(saveBankAccount)

  /** This is used only when UseNewBarsVerify switch is ON.
    *
    * When switch is off, details are saved to cache by SessionService instead.
    */
  def saveAnswersForBankAccountDetailsPage(bankAccountDetails: BankAccountDetails)(implicit
      hc: HeaderCarrier,
      profile: CurrentProfile,
      ex: ExecutionContext,
      request: Request[_]): Future[Either[Unit, BankAccount]] =
    getBankAccount
      .flatMap {
        case Some(existing) =>
          saveBankAccount(existing.copy(details = Some(bankAccountDetails))).map(Right(_))
        case None =>
          errorLog(
            s"[BankAccountDetailsService][saveAnswersForBankAccountDetailsPage] Unable to fetch bank account details for reg ID: ${profile.registrationId}")
          Future.successful(Left(()))
      }

  /** METHOD TO BE DELETED IN DL-19023 CLEAN UP
    *
    * This is used only when UseNewBarsVerify switch is OFF, in which case the user is unable to access the CYA page.
    *
    * Important: IndeterminateStatus is treated similar to ValidStatus - allowing the user to proceed and not incrementing the failure count. However,
    * unlike Valid their details are given the 'bankDetailsNotValid = true' flag in the backend submission to the API.
    */
  def makeBarsValidationCheckAndSaveValidAnswers(bankAccountDetails: BankAccountDetails)(implicit
      hc: HeaderCarrier,
      profile: CurrentProfile,
      ex: ExecutionContext,
      request: Request[_]): Future[Boolean] =
    bankAccountRepService.validateBankDetails(bankAccountDetails).flatMap {
      case barsResponseStatus @ (ValidStatus | IndeterminateStatus) =>
        val bankAccount = BankAccount(
          isProvided = true,
          details = Some(bankAccountDetails.copy(status = Some(barsResponseStatus))),
          reason = None,
          bankAccountType = None
        )
        saveBankAccount(bankAccount).map(_ => true)

      case InvalidStatus =>
        Future.successful(false)
    }

  /** This is used only when UseNewBarsVerify switch is ON.
    *
    * Important: IndeterminateStatus is treated similar to ValidStatus - allowing the user to proceed and not incrementing the failure count. However,
    * unlike Valid their details are given the 'bankDetailsNotValid = true' flag in the backend submission to the API.
    */
  def verifyAndSaveBankAccountDetails(bankAccountDetails: BankAccountDetails, bankAccountType: BankAccountType)(implicit
      hc: HeaderCarrier,
      profile: CurrentProfile,
      ex: ExecutionContext,
      request: Request[_]): Future[BarsVerificationOutcome] =
    for {
      barsResult: BarsResponseAndVerificationStatus <- barsService.verifyBankDetails(bankAccountDetails, bankAccountType)
      barsStatus: BankAccountDetailsStatus = barsResult.bankDetailsVerificationStatus
      updatedFailureCount: Int <- incrementFailureCountIfInvalidAndReturnNewCount(barsStatus, profile.registrationId)
      isLocked: Boolean           = updatedFailureCount >= attemptLimit
      modelForSaving: BankAccount = buildBankDetailsForSavingFromResult(isLocked, bankAccountDetails, bankAccountType, barsStatus)
      _ <- saveBankAccount(modelForSaving)
      _ <- barsAuditService.sendBarsAuditEvent(
        bankAccountDetails,
        bankAccountType,
        barsResult.barsVerificationResponse,
        updatedFailureCount,
        isLocked,
        barsStatus)
      userJourneyResult: BarsVerificationOutcome = determineUserJourneyResult(barsStatus, isLocked)
    } yield userJourneyResult

  private def incrementFailureCountIfInvalidAndReturnNewCount(barsStatus: BankAccountDetailsStatus, registrationId: String): Future[Int] =
    if (barsStatus == InvalidStatus) {
      lockService.incrementBarsAttemptsAndReturnNewFailedCount(registrationId)
    } else {
      lockService.getBarsAttemptsUsed(registrationId)
    }

  private def buildBankDetailsForSavingFromResult(isLocked: Boolean,
                                                  bankAccountDetails: BankAccountDetails,
                                                  bankAccountType: BankAccountType,
                                                  status: BankAccountDetailsStatus): BankAccount = {
    val model: BankAccount = BankAccount(
      isProvided = true,
      details = Some(bankAccountDetails.copy(status = Some(status))),
      reason = None,
      bankAccountType = Some(bankAccountType)
    )
    if (isLocked) model.copy(reason = Some(lockoutReason)) else model
  }

  private def determineUserJourneyResult(status: BankAccountDetailsStatus, isLocked: Boolean): BarsVerificationOutcome =
    status match {
      case ValidStatus | IndeterminateStatus => BarsSuccess
      case InvalidStatus if isLocked         => BarsLockedOut
      case InvalidStatus                     => BarsFailedNotLocked
    }

}

object BankAccountDetailsService {
  val redirectBackToFirstPageInJourney: Result = Redirect(routes.HasBankAccountController.show)
}
