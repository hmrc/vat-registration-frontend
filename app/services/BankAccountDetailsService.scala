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
import featuretoggle.FeatureSwitch.UseNewBarsVerify
import featuretoggle.FeatureToggleSupport.isEnabled
import models._
import models.api.{BankAccountDetailsStatus, IndeterminateStatus, InvalidStatus, ValidStatus}
import models.bars._
import play.api.mvc.Request
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
)(implicit appConfig: FrontendAppConfig)
    extends LoggingUtil {

  def getBankAccount(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[Option[BankAccount]] = {
    infoLog(s"[BankAccountDetailsService][getBankAccount] Fetching bank account for registration: ${profile.registrationId}")
    regApiConnector.getSection[BankAccount](profile.registrationId)
  }

  def saveBankAccount(bankAccount: BankAccount)(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[BankAccount] = {
    infoLog(s"[BankAccountDetailsService][saveBankAccount] Saving bank account for registration")
    regApiConnector.replaceSection[BankAccount](profile.registrationId, bankAccount)
  }

  def saveHasCompanyBankAccount(hasBankAccount: Boolean)(implicit
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

  def saveEnteredBankAccountDetails(bankAccountDetails: BankAccountDetails, bankAccountType: Option[BankAccountType])(implicit
      hc: HeaderCarrier,
      profile: CurrentProfile,
      ex: ExecutionContext,
      request: Request[_]): Future[(Boolean, Option[BarsVerificationResponse])] =
    selectBarsEndpoint(bankAccountDetails, bankAccountType).flatMap {
      case (status @ (ValidStatus | IndeterminateStatus), rawResponse) =>
        infoLog(s"[BankAccountDetailsService][saveEnteredBankAccountDetails] Verification passed with status: $status")
        val bankAccount = BankAccount(
          isProvided = true,
          details = Some(bankAccountDetails.copy(status = Some(status))),
          reason = None,
          bankAccountType = bankAccountType
        )
        saveBankAccount(bankAccount).map(_ => (true, rawResponse))

      case (InvalidStatus, rawResponse) =>
        warnLog(s"[BankAccountDetailsService][saveEnteredBankAccountDetails] Verification failed with InvalidStatus")
        Future.successful((false, rawResponse))
    }

  def selectBarsEndpoint(bankAccountDetails: BankAccountDetails, bankAccountType: Option[BankAccountType])(implicit
      hc: HeaderCarrier,
      ex: ExecutionContext,
      request: Request[_]): Future[(BankAccountDetailsStatus, Option[BarsVerificationResponse])] =
    if (isEnabled(UseNewBarsVerify))
      bankAccountType match {
        case Some(accountType) => barsService.verifyBankDetails(bankAccountDetails, accountType)
        case None              => Future.failed(new IllegalStateException("bankAccountType is required when UseNewBarsVerify is enabled"))
      }
    else bankAccountRepService.validateBankDetails(bankAccountDetails).map(status => (status, None))

  def verifyAndSaveBankAccountDetails(
      bankAccountDetails: BankAccountDetails,
      bankAccountType: BankAccountType
  )(implicit hc: HeaderCarrier, profile: CurrentProfile, ex: ExecutionContext, request: Request[_]): Future[BarsVerificationOutcome] =
    saveEnteredBankAccountDetails(bankAccountDetails, Some(bankAccountType)).flatMap { case (saved, rawResponse) =>
      if (saved) {
        infoLog(s"[BankAccountDetailsService][verifyAndSaveBankAccountDetails] Bank details saved successfully")
        lockService.getBarsAttemptsUsed(profile.registrationId).flatMap { attemptNumber =>
          barsAuditService
            .sendBarsAuditEvent(bankAccountDetails, bankAccountType, rawResponse, attemptNumber, accountStatus = "unlocked", checkOutcome = "pass")
            .map(_ => BarsSuccess)
        }
      } else {
        warnLog(s"[BankAccountDetailsService][verifyAndSaveBankAccountDetails] Bank details failed verification for registration")
        lockService.incrementBarsAttempts(profile.registrationId).flatMap { attemptNumber =>
          lockService.isBarsLocked(profile.registrationId).flatMap { isLocked =>
            barsAuditService
              .sendBarsAuditEvent(
                bankAccountDetails,
                bankAccountType,
                rawResponse,
                attemptNumber,
                accountStatus = if (isLocked) "locked" else "unlocked",
                checkOutcome = "fail")
              .map(_ => if (isLocked) BarsLockedOut else BarsFailedNotLocked)
          }
        }
      }
    }

  def saveBankAccountNotProvided(
      reason: NoUKBankAccount)(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[BankAccount] = {
    infoLog(s"[BankAccountDetailsService][saveBankAccountNotProvided] Saving no bank account with reason: $reason")
    saveBankAccount(BankAccount(isProvided = false, details = None, reason = Some(reason), bankAccountType = None))
  }

  def saveBankAccountType(bankAccountType: BankAccountType)(implicit
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
}
