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
import play.api.libs.json.{Json, Reads}

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
    sortCodeIsPresentOnEISCD == BarsResponse.Yes &&
      accountExists == BarsResponse.Yes &&
      (nameMatches == BarsResponse.Yes || nameMatches == BarsResponse.Partial)

  def check: Seq[BarsError] =
    Seq(
      checkSortCodeExistsOnEiscd(sortCodeIsPresentOnEISCD),
      checkAccountExists(accountExists),
      checkNameMatches(nameMatches, accountExists)
    ).flatten

  private def checkSortCodeExistsOnEiscd(sortCodeIsPresentOnEISCD: BarsResponse): Option[BarsError] =
    sortCodeIsPresentOnEISCD match {
      case BarsResponse.No    => Some(SortCodeNotFound)
      case BarsResponse.Error => Some(ThirdPartyError)
      case _                  => None
    }

  private def checkAccountExists(accountExists: BarsResponse): Option[BarsError] =
    accountExists match {
      case BarsResponse.No | BarsResponse.Inapplicable => Some(AccountNotFound)
      case BarsResponse.Indeterminate                  => Some(BankAccountUnverified)
      case BarsResponse.Error                          => Some(ThirdPartyError)
      case _                                           => None
    }

  private def checkNameMatches(nameMatches: BarsResponse, accountExists: BarsResponse): Option[BarsError] =
    nameMatches match {
      case BarsResponse.No | BarsResponse.Inapplicable                     => Some(NameMismatch)
      case BarsResponse.Indeterminate if accountExists == BarsResponse.Yes => Some(BankAccountUnverified)
      case BarsResponse.Error                                              => Some(ThirdPartyError)
      case _                                                               => None
    }
}

object BarsVerificationResponse {
  implicit val reads: Reads[BarsVerificationResponse] = Json.reads[BarsVerificationResponse]
}
