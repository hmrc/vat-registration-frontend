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

import connectors.RegistrationApiConnector
import controllers.bankdetails.routes
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
class BankAccountDetailsService @Inject() (val regApiConnector: RegistrationApiConnector,
                                           barsService: BarsService,
                                           lockService: LockService,
                                           barsAuditService: BarsAuditService)
    extends LoggingUtil
    with FeatureToggleSupport {

  def getBankAccount(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[Option[BankAccount]] = {
    infoLog(s"[BankAccountDetailsService][getBankAccount] Fetching bank account for registration: ${profile.registrationId}")
    regApiConnector.getSection[BankAccount](profile.registrationId)
  }

  def saveBankAccount(bankAccount: BankAccount)(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[BankAccount] = {
    infoLog(s"[BankAccountDetailsService][saveBankAccount] Saving bank account for registration")
    regApiConnector.replaceSection[BankAccount](profile.registrationId, bankAccount)
  }

  def saveAnswerCanProvideBankAccountDetailsPage(canProvideBankAccountDetails: Boolean)(implicit
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
        // Do not delete invalid bank details, they are sent to the API at submission for audit purposes
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

  def saveAnswerForBankAccountNotProvidedPage(
      reason: NoUKBankAccount)(implicit hc: HeaderCarrier, ec: ExecutionContext, profile: CurrentProfile, request: Request[_]): Future[BankAccount] =
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

  /**
    * Important: IndeterminateStatus is treated similar to ValidStatus - allowing the user to proceed and not incrementing the failure count.
    * However, unlike Valid their details are given the 'bankDetailsNotValid = true' flag in the backend submission to the API.
    */
  def verifyAndSaveBankAccountDetails(bankAccountDetails: BankAccountDetails, bankAccountType: BankAccountType, optReason: Option[NoUKBankAccount])(
      implicit
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
        optReason,
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
  val redirectBackToFirstPageInJourney: Result = Redirect(routes.CanYouProvideBankAccountDetailsController.show())
}
