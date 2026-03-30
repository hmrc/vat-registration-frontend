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
import play.api.Logging
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Request
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BankAccountDetailsService @Inject() (
    val regApiConnector: RegistrationApiConnector,
    bankAccountRepService: BankAccountReputationService,
    barsService: BarsService,
    lockService: LockService,
    auditConnector: AuditConnector,
    val authConnector: AuthConnector
)(implicit appConfig: FrontendAppConfig)
    extends Logging
    with AuthorisedFunctions {

  private val BarsCheckAttemptAuditType = "BarsCheckAttempt"

  def getBankAccount(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[Option[BankAccount]] =
    regApiConnector.getSection[BankAccount](profile.registrationId)

  def saveBankAccount(bankAccount: BankAccount)(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[BankAccount] =
    regApiConnector.replaceSection[BankAccount](profile.registrationId, bankAccount)

  def saveHasCompanyBankAccount(hasBankAccount: Boolean)(implicit
      hc: HeaderCarrier,
      profile: CurrentProfile,
      ex: ExecutionContext,
      request: Request[_]): Future[BankAccount] = {
    val bankAccount = getBankAccount map {
      case Some(BankAccount(oldHasBankAccount, _, _, _)) if oldHasBankAccount != hasBankAccount =>
        BankAccount(hasBankAccount, None, None, None)
      case Some(bankAccountDetails) =>
        bankAccountDetails.copy(isProvided = hasBankAccount)
      case None =>
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
        val bankAccount = BankAccount(
          isProvided = true,
          details = Some(bankAccountDetails.copy(status = Some(status))),
          reason = None,
          bankAccountType = bankAccountType
        )
        saveBankAccount(bankAccount).map(_ => (true, rawResponse))

      case (InvalidStatus, rawResponse) =>
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
      authorised().retrieve(Retrievals.internalId) {
        case Some(credId) =>
          if (saved) {
            lockService.getBarsAttemptsUsed(profile.registrationId).map { attemptNumber =>
              sendBarsAuditEvent(
                bankAccountDetails,
                bankAccountType,
                rawResponse,
                credId,
                attemptNumber,
                accountStatus = "unlocked",
                checkOutcome = "pass")
              BarsSuccess
            }
          } else {
            lockService.incrementBarsAttempts(profile.registrationId).flatMap { attemptNumber =>
              lockService.isBarsLocked(profile.registrationId).map { isLocked =>
                sendBarsAuditEvent(
                  bankAccountDetails,
                  bankAccountType,
                  rawResponse,
                  credId,
                  attemptNumber,
                  accountStatus = if (isLocked) "locked" else "unlocked",
                  checkOutcome = "fail")
                if (isLocked) BarsLockedOut else BarsFailedNotLocked
              }
            }
          }
        case None =>
          throw new InternalServerException("Missing internal ID for BARS check auditing")
      }
    }

  private def sendBarsAuditEvent(
      bankAccountDetails: BankAccountDetails,
      bankAccountType: BankAccountType,
      rawResponse: Option[BarsVerificationResponse],
      credId: String,
      attemptNumber: Int,
      accountStatus: String,
      checkOutcome: String
  )(implicit hc: HeaderCarrier, profile: CurrentProfile, ex: ExecutionContext): Unit = {

    logger.info(s"Raising BARS audit event: outcome=$checkOutcome attempt=$attemptNumber accountStatus=$accountStatus")

    val userType = if (profile.agentReferenceNumber.isDefined) "agent" else "organisation"

    val detail: JsObject = Json.obj(
      "attemptNumber" -> attemptNumber,
      "accountStatus" -> accountStatus,
      "checkOutcome"  -> checkOutcome,
      "credId"        -> credId,
      "journeyId"     -> profile.registrationId,
      "userType"      -> userType,
      "detailsSubmitted" -> Json.obj(
        "account" -> Json.obj(
          "sortCode"      -> bankAccountDetails.sortCode,
          "accountNumber" -> bankAccountDetails.number
        ),
        "accountType" -> bankAccountType.toString.toLowerCase,
        "accountName" -> bankAccountDetails.name
      ),
      "validationResponse" -> rawResponse.map { r =>
        Json.obj(
          "accountExists" -> r.accountExists.toString.toLowerCase,
          "nameMatches"   -> r.nameMatches.toString.toLowerCase
        )
      }
    )

    auditConnector.sendExplicitAudit(BarsCheckAttemptAuditType, detail)
  }

  def saveBankAccountNotProvided(
      reason: NoUKBankAccount)(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[BankAccount] =
    saveBankAccount(BankAccount(isProvided = false, details = None, reason = Some(reason), bankAccountType = None))

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
