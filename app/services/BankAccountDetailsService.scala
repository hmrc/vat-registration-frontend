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
import models._
import models.bars.BankAccountType
import models.api.{IndeterminateStatus, InvalidStatus, ValidStatus}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Request

@Singleton
class BankAccountDetailsService @Inject()(val regApiConnector: RegistrationApiConnector,
                                          val bankAccountRepService: BankAccountReputationService) {

  def getBankAccount(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[Option[BankAccount]] = {
    regApiConnector.getSection[BankAccount](profile.registrationId)
  }

  def saveBankAccount(bankAccount: BankAccount)
                            (implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[BankAccount] = {
    regApiConnector.replaceSection[BankAccount](profile.registrationId, bankAccount)
  }

  def saveHasCompanyBankAccount(hasBankAccount: Boolean)
                               (implicit hc: HeaderCarrier, profile: CurrentProfile, ex: ExecutionContext, request: Request[_]): Future[BankAccount] = {
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

  def saveEnteredBankAccountDetails(accountDetails: BankAccountDetails)
                                   (implicit hc: HeaderCarrier, profile: CurrentProfile, ex: ExecutionContext, request: Request[_]): Future[Boolean] = {
    for {
      existing <- getBankAccount
      result   <- bankAccountRepService.validateBankDetails(accountDetails).flatMap {
        case status@(ValidStatus | IndeterminateStatus) =>
          val bankAccount = BankAccount(
            isProvided = true,
            details = Some(accountDetails.copy(status = Some(status))),
            reason = None,
            bankAccountType = existing.flatMap(_.bankAccountType)
          )
          saveBankAccount(bankAccount) map (_ => true)
        case InvalidStatus => Future.successful(false)
      }
    } yield result
  }

  def saveNoUkBankAccountDetails(reason: NoUKBankAccount)
                                (implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[BankAccount] = {
    val bankAccount = BankAccount(
      isProvided = false,
      details = None,
      reason = Some(reason),
      bankAccountType = None
    )
    saveBankAccount(bankAccount)
  }

  def saveBankAccountType(bankAccountType: BankAccountType)
                         (implicit hc: HeaderCarrier, profile: CurrentProfile, ex: ExecutionContext, request: Request[_]): Future[BankAccount] = {
    getBankAccount.map {
      case Some(existing) => existing.copy(bankAccountType = Some(bankAccountType))
      case None           => BankAccount(isProvided = true, details = None, reason = None, bankAccountType = Some(bankAccountType))
    }.flatMap(saveBankAccount)
  }
}
