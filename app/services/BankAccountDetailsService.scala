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

import connectors.VatRegistrationConnector
import javax.inject.{Inject, Singleton}
import models.{BankAccount, BankAccountDetails, CurrentProfile, S4LKey}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BankAccountDetailsService @Inject()(val vatRegConnector: VatRegistrationConnector,
                                          val s4LService: S4LService,
                                          val bankAccountRepService: BankAccountReputationService) {
  
  def fetchBankAccountDetails(implicit hc: HeaderCarrier, profile: CurrentProfile, ex: ExecutionContext): Future[Option[BankAccount]] = {
    s4LService.fetchAndGetNoAux(BankAccount.s4lKey) flatMap {
      case Some(bankAccount) => Future.successful(Some(bankAccount))
      case None => vatRegConnector.getBankAccount(profile.registrationId)
    }
  }

  def saveHasCompanyBankAccount(hasBankAccount: Boolean)
                               (implicit hc: HeaderCarrier, profile: CurrentProfile, ex: ExecutionContext): Future[BankAccount] = {
    val bankAccount = if (hasBankAccount) {
      fetchBankAccountDetails map {
        case Some(bankAccountDetails) => bankAccountDetails.copy(isProvided = true)
        case None => BankAccount(hasBankAccount, None, None)
      }
    } else {
      Future.successful(BankAccount(isProvided = false, None, None))
    }

    bankAccount flatMap saveBankAccountDetails
  }

  def saveEnteredBankAccountDetails(accountDetails: BankAccountDetails)
                                   (implicit hc: HeaderCarrier, profile: CurrentProfile, ex: ExecutionContext): Future[Boolean] = {
    bankAccountRepService.bankAccountDetailsModulusCheck(accountDetails).flatMap { validDetails =>
      if (validDetails) {
        val bankAccount = BankAccount(isProvided = true, Some(accountDetails), None)
        saveBankAccountDetails(bankAccount) map (_ => true)
      } else {
        Future.successful(false)
      }
    }
  }

  private[services] def bankAccountBlockCompleted(bankAccount: BankAccount): Completion[BankAccount] = {
    bankAccount match {
      case BankAccount(true, Some(_), None) => Complete(bankAccount)
      case BankAccount(false, _, Some(_)) => Complete(bankAccount.copy(details = None))
      case _ => Incomplete(bankAccount)
    }
  }

  def saveBankAccountDetails(bankAccount: BankAccount)
                                              (implicit hc: HeaderCarrier, profile: CurrentProfile, ex: ExecutionContext): Future[BankAccount] = {
    bankAccountBlockCompleted(bankAccount) fold(
      incomplete => s4LService.saveNoAux(incomplete, BankAccount.s4lKey) map (_ => incomplete),
      complete => for {
        _ <- vatRegConnector.patchBankAccount(profile.registrationId, complete)
        _ <- s4LService.saveNoAux(complete, BankAccount.s4lKey)
      } yield complete
    )
  }
}
