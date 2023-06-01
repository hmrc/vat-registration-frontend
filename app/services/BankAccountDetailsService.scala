/*
 * Copyright 2023 HM Revenue & Customs
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
import models.api.{IndeterminateStatus, InvalidStatus, ValidStatus}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Request

@Singleton
class BankAccountDetailsService @Inject()(val regApiConnector: RegistrationApiConnector,
                                          val bankAccountRepService: BankAccountReputationService) {

  def fetchBankAccountDetails(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Option[BankAccount]] = {
    regApiConnector.getSection[BankAccount](profile.registrationId)
  }

  def saveBankAccountDetails(bankAccount: BankAccount)
                            (implicit hc: HeaderCarrier, profile: CurrentProfile): Future[BankAccount] = {
    regApiConnector.replaceSection[BankAccount](profile.registrationId, bankAccount)
  }

  def saveHasCompanyBankAccount(hasBankAccount: Boolean)
                               (implicit hc: HeaderCarrier, profile: CurrentProfile, ex: ExecutionContext): Future[BankAccount] = {
    val bankAccount = fetchBankAccountDetails map {
      case Some(BankAccount(oldHasBankAccount, _, _)) if oldHasBankAccount != hasBankAccount =>
        BankAccount(hasBankAccount, None, None)
      case Some(bankAccountDetails) =>
        bankAccountDetails.copy(isProvided = hasBankAccount)
      case None =>
        BankAccount(hasBankAccount, None, None)
    }

    bankAccount flatMap saveBankAccountDetails
  }

  def saveEnteredBankAccountDetails(accountDetails: BankAccountDetails)
                                   (implicit hc: HeaderCarrier, profile: CurrentProfile, ex: ExecutionContext, request: Request[_]): Future[Boolean] = {
    bankAccountRepService.validateBankDetails(accountDetails).flatMap {
      case status@(ValidStatus | IndeterminateStatus) =>
        val bankAccount = BankAccount(
          isProvided = true,
          details = Some(accountDetails.copy(status = Some(status))),
          reason = None
        )
        saveBankAccountDetails(bankAccount) map (_ => true)
      case InvalidStatus => Future.successful(false)
    }
  }

  def saveNoUkBankAccountDetails(reason: NoUKBankAccount)
                                (implicit hc: HeaderCarrier, profile: CurrentProfile): Future[BankAccount] = {
    val bankAccount = BankAccount(
      isProvided = false,
      details = None,
      reason = Some(reason)
    )
    saveBankAccountDetails(bankAccount)
  }
}
