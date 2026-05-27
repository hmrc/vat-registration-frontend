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

import models.api.{BankAccountDetailsStatus, IndeterminateStatus, InvalidStatus, ValidStatus}
import models.bars.BarsError._
import models.bars.BarsResponse._
import play.api.libs.json.{Json, Reads}

case class BarsVerificationResponse(accountNumberIsWellFormatted: BarsResponse,
                                    sortCodeIsPresentOnEISCD: BarsResponse,
                                    sortCodeBankName: Option[String],
                                    accountExists: BarsResponse,
                                    nameMatches: BarsResponse,
                                    sortCodeSupportsDirectDebit: BarsResponse,
                                    sortCodeSupportsDirectCredit: BarsResponse,
                                    nonStandardAccountDetailsRequiredForBacs: Option[BarsResponse],
                                    iban: Option[String],
                                    accountName: Option[String]) {

  def handleVerificationResponse: (BankAccountDetailsStatus, Seq[BarsErrorAndReason]) = {
    val barsErrors: Seq[BarsErrorAndReason] = Seq(
      checkSortCodeExistsOnEiscd,
      checkAccountExists,
      checkNameMatches
    ).flatten
    val errorsAreIndeterminateOnly: Boolean = barsErrors.forall(_.barsError == BankAccountUnverified)

    if (barsErrors.isEmpty) {
      (ValidStatus, Seq.empty[BarsErrorAndReason])
    } else if (errorsAreIndeterminateOnly) {
      (IndeterminateStatus, barsErrors)
    } else {
      (InvalidStatus, barsErrors)
    }
  }

  private def checkSortCodeExistsOnEiscd: Option[BarsErrorAndReason] = {
    val result = sortCodeIsPresentOnEISCD match {
      case Yes   => None
      case No    => Some(SortCodeNotFound)
      case Error => Some(ThirdPartyError)
    }
    result.map(barsError => BarsErrorAndReason(barsError, s"${barsError.toString} failure: sortCodeIsPresentOnEISCD = $sortCodeIsPresentOnEISCD"))
  }

  private def checkAccountExists: Option[BarsErrorAndReason] = {
    val result = accountExists match {
      case Yes               => None
      case No | Inapplicable => Some(AccountNotFound)
      case Indeterminate     => Some(BankAccountUnverified)
      case Error             => Some(ThirdPartyError)
    }
    result.map(barsError => BarsErrorAndReason(barsError, s"${barsError.toString} failure: accountExists = $accountExists"))
  }

  private def checkNameMatches: Option[BarsErrorAndReason] = {
    val result = nameMatches match {
      case Yes | Partial                         => None
      case Indeterminate if accountExists == Yes => Some(BankAccountUnverified)
      case No | Inapplicable | Indeterminate     => Some(NameMismatch)
      case Error                                 => Some(ThirdPartyError)
    }
    result.map(barsError => BarsErrorAndReason(barsError, s"${barsError.toString} failure: nameMatches = $nameMatches"))
  }
}

object BarsVerificationResponse {
  implicit val reads: Reads[BarsVerificationResponse] = Json.reads[BarsVerificationResponse]
}
