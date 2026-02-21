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

package models.bars

import models.bars.BarsErrors._
import play.api.libs.json.{Json, OFormat}

case class BarsVerificationResponse(
                                     accountNumberIsWellFormatted: BarsResponse,
                                     sortCodeIsPresentOnEISCD: BarsResponse,
                                     sortCodeBankName: Option[String],
                                     accountExists: BarsResponse,
                                     nameMatches: BarsResponse,
                                     sortCodeSupportsDirectDebit: BarsResponse,
                                     sortCodeSupportsDirectCredit: BarsResponse,
                                     nonStandardAccountDetailsRequiredForBacs: Option[BarsResponse],
                                     iban: Option[String],
                                     accountName: Option[String]
                                   ) {

  val isSuccessful: Boolean =
    (accountNumberIsWellFormatted == BarsResponse.Yes || accountNumberIsWellFormatted == BarsResponse.Indeterminate) &&
      sortCodeIsPresentOnEISCD == BarsResponse.Yes &&
      accountExists == BarsResponse.Yes &&
      (nameMatches == BarsResponse.Yes || nameMatches == BarsResponse.Partial) &&
      sortCodeSupportsDirectDebit == BarsResponse.Yes

  def check: Either[BarsErrors, BarsVerificationResponse] = {
    val validated: Either[BarsErrors, Unit] = for {
      _ <- checkAccountAndName(accountExists, nameMatches)
      _ <- checkAccountNumberFormat(accountNumberIsWellFormatted)
      _ <- checkSortCodeExistsOnEiscd(sortCodeIsPresentOnEISCD)
      _ <- checkSortCodeDirectDebitSupport(sortCodeSupportsDirectDebit)
      _ <- checkAccountExists(accountExists)
      _ <- checkNameMatches(nameMatches, accountExists)
    } yield ()

    validated.map(_ => this)
  }

  private def checkAccountAndName(accountExists: BarsResponse, nameMatches: BarsResponse): Either[BarsErrors, Unit] =
    if (accountExists == BarsResponse.No && nameMatches == BarsResponse.No) Left(DetailsVerificationFailed)
    else Right(())

  private def checkAccountNumberFormat(accountNumberIsWellFormatted: BarsResponse): Either[BarsErrors, Unit] =
    if (accountNumberIsWellFormatted == BarsResponse.No) Left(AccountDetailInvalidFormat)
    else Right(())

  private def checkSortCodeExistsOnEiscd(sortCodeIsPresentOnEISCD: BarsResponse): Either[BarsErrors, Unit] =
    if (sortCodeIsPresentOnEISCD == BarsResponse.No) Left(SortCodeNotFound)
    else Right(())

  private def checkSortCodeDirectDebitSupport(sortCodeSupportsDirectDebit: BarsResponse): Either[BarsErrors, Unit] =
    if (sortCodeSupportsDirectDebit == BarsResponse.No) Left(SortCodeNotSupported)
    else Right(())

  private def checkAccountExists(accountExists: BarsResponse): Either[BarsErrors, Unit] =
    accountExists match {
      case BarsResponse.No => Left(AccountNotFound)
      case BarsResponse.Indeterminate => Left(BankAccountUnverified)
      case _ => Right(())
    }

  private def checkNameMatches(nameMatches: BarsResponse, accountExists: BarsResponse): Either[BarsErrors, Unit] =
    nameMatches match {
      case BarsResponse.No => Left(NameMismatch)
      case BarsResponse.Indeterminate if accountExists == BarsResponse.Yes => Left(BankAccountUnverified)
      case _ => Right(())
    }
}

object BarsVerificationResponse {
  implicit val format: OFormat[BarsVerificationResponse] = Json.format[BarsVerificationResponse]
}