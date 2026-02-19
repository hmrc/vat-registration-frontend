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

import com.google.inject.{Inject, Singleton}
import connectors.BarsConnector
import models.BankAccountDetails
import models.api.{BankAccountDetailsStatus, IndeterminateStatus, InvalidStatus, ValidStatus}
import models.bars.BarsErrors
import models.bars.BarsErrors._
import models.bars._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class BarsService @Inject()(
                                  barsConnector: BarsConnector
                                )(implicit ec: ExecutionContext) {

  private val checkAccountAndName = (accountExists: BarsResponse, nameMatches: BarsResponse) => {
    if (accountExists == BarsResponse.Indeterminate || nameMatches == BarsResponse.Indeterminate) {
      Left(BankAccountUnverified)
    } else {
      Right(())
    }
  }

  private val checkAccountNumberFormat = (accountNumberIsWellFormatted: BarsResponse) =>
    if (accountNumberIsWellFormatted == BarsResponse.No) Left(AccountDetailInvalidFormat) else Right(())

  private val checkSortCodeExistsOnEiscd = (sortCodeIsPresentOnEISCD: BarsResponse) =>
    if (sortCodeIsPresentOnEISCD == BarsResponse.No) Left(SortCodeNotFound) else Right(())

  private val checkSortCodeDirectDebitSupport = (sortCodeSupportsDirectDebit: BarsResponse) =>
    if (sortCodeSupportsDirectDebit == BarsResponse.No) Left(SortCodeNotSupported) else Right(())

  private val checkAccountExists = (accountExists: BarsResponse) =>
    if (accountExists == BarsResponse.No || accountExists == BarsResponse.Inapplicable) Left(AccountNotFound) else Right(())

  private val checkNameMatches = (nameMatches: BarsResponse, accountExists: BarsResponse) =>
    if (
      nameMatches == BarsResponse.No || nameMatches == BarsResponse.Inapplicable ||
        (nameMatches == BarsResponse.Indeterminate && accountExists != BarsResponse.Indeterminate)
    ) {
      Left(NameMismatch)
    } else {
      Right(())
    }

  private val checkBarsResponseSuccess = (response: BarsVerificationResponse) =>
    (response.accountNumberIsWellFormatted == BarsResponse.Yes || response.accountNumberIsWellFormatted == BarsResponse.Indeterminate) &&
      response.sortCodeIsPresentOnEISCD == BarsResponse.Yes &&
      response.accountExists == BarsResponse.Yes &&
      (response.nameMatches == BarsResponse.Yes || response.nameMatches == BarsResponse.Partial) &&
      response.sortCodeSupportsDirectDebit == BarsResponse.Yes


   private def barsVerification(
                        bankAccountType: BankAccountType,
                        bankDetails: BankAccountDetails
                      )(implicit hc: HeaderCarrier): Future[Either[BarsErrors, BarsVerificationResponse]] = {

     val requestJson: JsValue = bankAccountType match {
       case BankAccountType.Personal =>
         Json.toJson(
           BarsPersonalRequest(
             BarsAccount(bankDetails.sortCode, bankDetails.number),
             BarsSubject(bankDetails.name)
           )
         )
       case BankAccountType.Business =>
         Json.toJson(
           BarsBusinessRequest(
             BarsAccount(bankDetails.sortCode, bankDetails.number),
             BarsBusiness(bankDetails.name)
           )
         )
     }

    val verificationFuture: Future[Either[BarsErrors, BarsVerificationResponse]] =
      barsConnector.verify(bankAccountType, requestJson)
        .map(Right(_))
        .recover {
          case e: UpstreamBarsException if e.status == 400 && e.errorCode.contains("SORT_CODE_ON_DENY_LIST") =>
            Left(SortCodeOnDenyList)
          case e: UpstreamBarsException if e.status == 400 =>
            Left(DetailsVerificationFailed)
          case _ =>
            Left(DetailsVerificationFailed)
        }

    verificationFuture.map {
      case Left(error) => Left(error)

      case Right(verificationResponse) =>
        if (checkBarsResponseSuccess(verificationResponse)) {
          Right(verificationResponse) 
        } else {
          val validated: Either[BarsErrors, Unit] = for {
            _ <- checkAccountAndName(verificationResponse.accountExists, verificationResponse.nameMatches)
            _ <- checkAccountNumberFormat(verificationResponse.accountNumberIsWellFormatted)
            _ <- checkSortCodeExistsOnEiscd(verificationResponse.sortCodeIsPresentOnEISCD)
            _ <- checkSortCodeDirectDebitSupport(verificationResponse.sortCodeSupportsDirectDebit)
            _ <- checkAccountExists(verificationResponse.accountExists)
            _ <- checkNameMatches(verificationResponse.nameMatches, verificationResponse.accountExists)
          } yield () 

          Left(validated.fold(identity, _ => DetailsVerificationFailed))
        }
    }
  }

  def verifyBankDetails(
                                 bankaccountType: BankAccountType,
                                 bankDetails: BankAccountDetails
                               )(implicit hc: HeaderCarrier): Future[BankAccountDetailsStatus] =
    barsVerification(bankaccountType, bankDetails).map {
      case Right(_) =>
        ValidStatus

      case Left(BankAccountUnverified) =>
        IndeterminateStatus

      case Left(_) =>
        InvalidStatus
    }
}