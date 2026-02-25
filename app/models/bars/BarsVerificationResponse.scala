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

import models.bars.BarsError._
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

  def check: Seq[BarsError] =
    Seq(
      checkAccountAndName(accountExists, nameMatches),
      checkAccountNumberFormat(accountNumberIsWellFormatted),
      checkSortCodeExistsOnEiscd(sortCodeIsPresentOnEISCD),
      checkSortCodeDirectDebitSupport(sortCodeSupportsDirectDebit),
      checkAccountExists(accountExists),
      checkNameMatches(nameMatches, accountExists)
    ).flatten

  private def checkAccountAndName(accountExists: BarsResponse, nameMatches: BarsResponse): Option[BarsError] =
    if (accountExists == BarsResponse.No && nameMatches == BarsResponse.No)
      Some(DetailsVerificationFailed)
    else None

  private def checkAccountNumberFormat(accountNumberIsWellFormatted: BarsResponse): Option[BarsError] =
    if (accountNumberIsWellFormatted == BarsResponse.No)
      Some(AccountDetailInvalidFormat)
    else None

  private def checkSortCodeExistsOnEiscd(sortCodeIsPresentOnEISCD: BarsResponse): Option[BarsError] =
    if (sortCodeIsPresentOnEISCD == BarsResponse.No)
      Some(SortCodeNotFound)
    else None

  private def checkSortCodeDirectDebitSupport(sortCodeSupportsDirectDebit: BarsResponse): Option[BarsError] =
    if (sortCodeSupportsDirectDebit == BarsResponse.No)
      Some(SortCodeNotSupported)
    else None

  private def checkAccountExists(accountExists: BarsResponse): Option[BarsError] =
    accountExists match {
      case BarsResponse.No | BarsResponse.Inapplicable => Some(AccountNotFound)
      case BarsResponse.Indeterminate                  => Some(BankAccountUnverified)
      case _                                           => None
    }

  private def checkNameMatches(nameMatches: BarsResponse, accountExists: BarsResponse): Option[BarsError] =
    nameMatches match {
      case BarsResponse.No | BarsResponse.Inapplicable                     => Some(NameMismatch)
      case BarsResponse.Indeterminate if accountExists == BarsResponse.Yes => Some(BankAccountUnverified)
      case _                                                               => None
    }
}

object BarsVerificationResponse {
  implicit val format: OFormat[BarsVerificationResponse] = Json.format[BarsVerificationResponse]
}
