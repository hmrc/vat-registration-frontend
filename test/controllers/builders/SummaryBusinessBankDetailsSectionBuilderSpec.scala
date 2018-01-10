/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.builders

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.{BankAccount, BankAccountDetails}
import models.api._
import models.view.SummaryRow
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency

class SummaryBusinessBankDetailsSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  val accountName = "testName"
  val accountNumber = "12345678"
  val sortCode = "12-34-56"

  val bankAccountNotProvided = BankAccount(isProvided = false, None)
  val bankAccount = BankAccount(
    isProvided = true,
    Some(BankAccountDetails(accountName, sortCode, accountNumber))
  )

  val bankAccountNotProvidedSection = SummaryBusinessBankDetailsSectionBuilder(Some(bankAccountNotProvided))
  val bankAccountProvidedSection = SummaryBusinessBankDetailsSectionBuilder(Some(bankAccount))

  val hasCompanyBankAccountUrl = features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView()
  val enterCompanyBankAccountDetailsUrl = features.bankAccountDetails.routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails()

  "accountIsProvidedRow" should {

    "return a isProvided row when isProvided is true" in {
      bankAccountProvidedSection.accountIsProvidedRow mustBe
        SummaryRow(
          "bankDetails.companyBankAccount",
          "app.common.yes",
          Some(hasCompanyBankAccountUrl)
        )
    }

    "return a isProvided row when isProvided is false" in {
      bankAccountNotProvidedSection.accountIsProvidedRow mustBe
        SummaryRow(
          "bankDetails.companyBankAccount",
          "app.common.no",
          Some(hasCompanyBankAccountUrl)
        )
    }
  }

  "companyBankAccountNameRow" should {

    "return an account name row when one is provided in the details block" in {
      bankAccountProvidedSection.companyBankAccountNameRow mustBe
        SummaryRow(
          "bankDetails.companyBankAccount.name",
          accountName,
          Some(enterCompanyBankAccountDetailsUrl)
        )
    }

    "not return an account name row when the details block is empty" in {
      bankAccountNotProvidedSection.companyBankAccountNameRow mustBe
        SummaryRow(
          "bankDetails.companyBankAccount.name",
          "app.common.no",
          Some(enterCompanyBankAccountDetailsUrl)
        )
    }
  }

  "companyBankAccountNumberRow" should {

    "return an account number row when one is provided in the details block with the account number being partially masked" in {
      bankAccountProvidedSection.companyBankAccountNumberRow mustBe
        SummaryRow(
          "bankDetails.companyBankAccount.number",
          "****5678",
          Some(enterCompanyBankAccountDetailsUrl)
        )
    }

    "not return an account number row when the details block is empty" in {
      bankAccountNotProvidedSection.companyBankAccountNumberRow mustBe
        SummaryRow(
          "bankDetails.companyBankAccount.number",
          "app.common.no",
          Some(enterCompanyBankAccountDetailsUrl)
        )
    }
  }

  "companyBankAccountSortCodeRow" should {

    "return an account sort code row when one is provided in the details block" in {
      bankAccountProvidedSection.companyBankAccountSortCodeRow mustBe
        SummaryRow(
          "bankDetails.companyBankAccount.sortCode",
          sortCode,
          Some(enterCompanyBankAccountDetailsUrl)
        )
    }

    "not return an account sort code row when the details block is empty" in {
      bankAccountNotProvidedSection.companyBankAccountSortCodeRow mustBe
        SummaryRow(
          "bankDetails.companyBankAccount.sortCode",
          "app.common.no",
          Some(enterCompanyBankAccountDetailsUrl)
        )
    }
  }

  "section" should {

    "return a summary section" in {
      bankAccountProvidedSection.section.id mustBe "bankDetails"
      bankAccountProvidedSection.section.rows.length mustEqual 4
    }
  }
}