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
import models.api.{BankAccountDetailsStatus, IndeterminateStatus, IndeterminateStatusWithDetails, InvalidStatus, InvalidStatusWithDetails, SimpleIndeterminateStatus, SimpleInvalidStatus, ValidStatus, ValidationDetails}
import models.bars._
import play.api.libs.json.Json
import play.api.mvc.Request
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.affinityGroup
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BankAccountDetailsService @Inject() (auditConnector: AuditConnector,
                                           val regApiConnector: RegistrationApiConnector,
                                           bankAccountRepService: BankAccountReputationService,
                                           barsService: BarsService,
                                           lockService: LockService,
                                           val authConnector: AuthConnector)(implicit appConfig: FrontendAppConfig) extends AuthorisedFunctions {

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
      request: Request[_]): Future[(Boolean, BankAccountDetailsStatus)] =
    for {
      barsResponse <- selectBarsEndpoint(bankAccountDetails, bankAccountType)
      result <- barsResponse match {
        case status @ (ValidStatus | SimpleIndeterminateStatus) =>
          val bankAccount = BankAccount(
            isProvided = true,
            details = Some(bankAccountDetails.copy(status = Some(status))),
            reason = None,
            bankAccountType = bankAccountType
          )
          saveBankAccount(bankAccount) map (_ => true)
        case SimpleInvalidStatus => Future.successful(false)
      }
    } yield (result, barsResponse)

  def selectBarsEndpoint(bankAccountDetails: BankAccountDetails, bankAccountType: Option[BankAccountType])(implicit
      hc: HeaderCarrier,
      request: Request[_]): Future[BankAccountDetailsStatus] =
    if (isEnabled(UseNewBarsVerify))
      bankAccountType match {
        case Some(accountType) => barsService.verifyBankDetails(bankAccountDetails, accountType)
        case None              => Future.failed(new IllegalStateException("bankAccountType is required when UseNewBarsVerify is enabled"))
      }
    else bankAccountRepService.validateBankDetails(bankAccountDetails)

  def verifyAndSaveBankAccountDetails(
      bankAccountDetails: BankAccountDetails,
      bankAccountType: BankAccountType,
      registrationId: String
  )(implicit hc: HeaderCarrier, profile: CurrentProfile, ex: ExecutionContext, request: Request[_]): Future[BarsVerificationOutcome] =
    saveEnteredBankAccountDetails(bankAccountDetails, Some(bankAccountType)).flatMap { saved =>
      if (saved._1) {
        sendAuditEvent(bankAccountDetails, bankAccountType, attemptNumber = None, Some("unlocked"), buildBarsAuditInfo("pass",saved._2))
        Future.successful(BarsSuccess)
      } else {
        lockService.incrementBarsAttempts(registrationId).flatMap { attemptNumber =>
          lockService.isBarsLocked(registrationId).map { isLocked =>
            val accountStatus = if (isLocked) "locked" else "unlocked"
            sendAuditEvent(bankAccountDetails, bankAccountType, Some(attemptNumber),
              Some(accountStatus), buildBarsAuditInfo("fail",saved._2))
            if (isLocked) BarsLockedOut else BarsFailedNotLocked
          }
        }
      }
    }

  def saveBankAccountNotProvided(
      reason: NoUKBankAccount)(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[BankAccount] = {
    val bankAccount = BankAccount(
      isProvided = false,
      details = None,
      reason = Some(reason),
      bankAccountType = None
    )
    saveBankAccount(bankAccount)
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



  private def buildBarsAuditInfo(outcome: String, barsResponse: BankAccountDetailsStatus) : BarsAuditInfo = {
    barsResponse match {
      case InvalidStatusWithDetails(ValidationDetails(accountExists, nameMatches)) =>
        BarsAuditInfo(Some(outcome), Some(if (accountExists)  "yes" else "no"), Some(if (nameMatches)  "yes" else "no"))
      case IndeterminateStatusWithDetails(ValidationDetails(accountExists, nameMatches)) =>
        BarsAuditInfo(Some(outcome), Some(if (accountExists)  "yes" else "no"), Some(if (nameMatches)  "yes" else "no"))
      case SimpleInvalidStatus =>
        BarsAuditInfo(Some(outcome), Some("no"), Some("no"))
      case ValidStatus =>
        BarsAuditInfo(Some(outcome), Some("yes"), Some("yes"))
      case SimpleIndeterminateStatus =>
        BarsAuditInfo(Some(outcome), Some("yes"), Some("no"))
    }
  }

  def sendAuditEvent(
                      bankAccountDetails: BankAccountDetails,
                      bankAccountType: BankAccountType,
                      attemptNumber: Option[Int] = None,
                      accountStatus: Option[String] = Some(""),
                      barsAuditInfo: BarsAuditInfo
                      )(
                              implicit hc: HeaderCarrier,
                              request: Request[_],
                              ec: ExecutionContext
                            ): Future[Unit] =
    for {
      affinityGroup <- retrieveIdentityDetails()
      _ <- auditConnector.sendEvent(
        buildBarsCheckAttemptAuditEvent(
          bankAccountDetails,
          bankAccountType,
          attemptNumber,
          accountStatus,
          barsAuditInfo,
          affinityGroup
        )
      )
    } yield ()


  case class BarsAuditInfo(checkOutcome: Option[String] = None, accountExists: Option[String] = None,
                           nameMatches: Option[String] = None)

  private def buildBarsCheckAttemptAuditEvent(
                                                bankAccountDetails: BankAccountDetails,
                                                bankAccountType: BankAccountType,
                                                attemptNumber: Option[Int] = None,
                                                accountStatus: Option[String] = Some(""),
                                                barsAuditInfo: BarsAuditInfo,
                                                affinityGroup: AffinityGroup
                                              )(
                                                implicit hc: HeaderCarrier,
                                                request: Request[_]
                                              ): DataEvent = {

    val AuditSource: String  = "vat-registration-frontend"
    val AuditType: String  = "BarsCheckAttempt"
    val TransactionName: String = "MTDVATPostCodeFail"

    val baseDetail = attemptNumber.map("attemptNumber" -> _.toString).toMap ++
      accountStatus.map("accountStatus" -> _).toMap ++
      barsAuditInfo.checkOutcome.map("checkOutcome" -> _).toMap

    val detailsJson = Json.obj(
      "detailsSubmitted" -> Json.obj(
        "account" -> Json.obj(
          "sortCode" -> bankAccountDetails.sortCode,
          "accountNumber" -> bankAccountDetails.number
        ),
        "accountType" -> bankAccountType.asBars,
        "accountName" -> bankAccountDetails.name
      ),
      "validationResponse" -> Json.obj(
        "accountExists" -> barsAuditInfo.accountExists,
        "nameMatches" -> barsAuditInfo.nameMatches
      )
    )
    val detailsString: String = Json.stringify(detailsJson)

    val detail = (baseDetail ++
        Map("detail" -> detailsString) + ("userType" -> affinityGroup.toString))
        .filter { case (_, value) => value.nonEmpty }

    DataEvent(
      auditSource = AuditSource,
      auditType = AuditType,
      tags = AuditExtensions.auditHeaderCarrier(hc)
        .toAuditTags(TransactionName, request.path),
      detail = AuditExtensions.auditHeaderCarrier(hc)
        .toAuditDetails(detail.toSeq: _*)
    )
  }

  private def retrieveIdentityDetails()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AffinityGroup] = {
    authorised().retrieve(affinityGroup) {
      case Some(affinity) => Future.successful(affinity)
      case _ => Future.failed(throw new InternalServerException("[BankAccountDetailsService] Couldn't retrieve auth details for user"))
    }
  }
}
