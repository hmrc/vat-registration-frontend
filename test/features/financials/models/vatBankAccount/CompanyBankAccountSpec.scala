/*
 * Copyright 2017 HM Revenue & Customs
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

package models.view.vatFinancials.vatBankAccount

import fixtures.VatRegistrationFixture
import models.api.{VatBankAccount, VatFinancials, VatScheme}
import models.view.vatFinancials.vatBankAccount.CompanyBankAccount.{COMPANY_BANK_ACCOUNT_NO, COMPANY_BANK_ACCOUNT_YES}
import models.{ApiModelTransformer, S4LVatFinancials}
import uk.gov.hmrc.play.test.UnitSpec

class CompanyBankAccountSpec extends UnitSpec with VatRegistrationFixture {

  "apply" should {
    val vatScheme = VatScheme(testRegId)
    val someBankAccount = VatBankAccount(accountName = "test", accountNumber = "12345678", accountSortCode = "12-12-12")

    "convert VatFinancials with bank account to view model" in {
      val vatFinancialsWithAccount = VatFinancials(
        bankAccount = Some(someBankAccount),
        turnoverEstimate = 100L,
        reclaimVatOnMostReturns = true,
        accountingPeriods = monthlyAccountingPeriod
      )
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithAccount))
      ApiModelTransformer[CompanyBankAccount].toViewModel(vs) shouldBe Some(CompanyBankAccount(COMPANY_BANK_ACCOUNT_YES))
    }

    "convert VatFinancials without bank account to view model" in {
      val vatFinancialsWithoutAccount = VatFinancials(
        turnoverEstimate = 100L,
        reclaimVatOnMostReturns = true,
        accountingPeriods = monthlyAccountingPeriod
      )
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithoutAccount))

      ApiModelTransformer[CompanyBankAccount].toViewModel(vs) shouldBe Some(CompanyBankAccount(COMPANY_BANK_ACCOUNT_NO))
    }

    "convert VatScheme without VatFinancials to empty view model" in {
      val vs = vatScheme.copy(financials = None)
      ApiModelTransformer[CompanyBankAccount].toViewModel(vs) shouldBe None
    }
  }

  "ViewModelFormat" should {

    val s4lVatFinancials: S4LVatFinancials = S4LVatFinancials(companyBankAccount = Some(validCompanyBankAccount))

    "extract CompanyBankAccount from VatFinancials" in {
      CompanyBankAccount.viewModelFormat.read(s4lVatFinancials) shouldBe Some(validCompanyBankAccount)
    }

    "update empty VatFinancials with CompanyBankAccount" in {
      CompanyBankAccount.viewModelFormat.update(validCompanyBankAccount, Option.empty[S4LVatFinancials]).
        companyBankAccount shouldBe Some(validCompanyBankAccount)
    }

    "update non-empty VatFinancials with CompanyBankAccount" in {
      CompanyBankAccount.viewModelFormat.update(validCompanyBankAccount, Some(s4lVatFinancials)).
        companyBankAccount shouldBe Some(validCompanyBankAccount)
    }
  }
}

