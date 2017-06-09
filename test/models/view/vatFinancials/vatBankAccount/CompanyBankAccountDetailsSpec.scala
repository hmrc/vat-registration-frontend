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
import models.{ApiModelTransformer, S4LVatFinancials}
import models.api.{VatBankAccount, VatFinancials, VatScheme}
import uk.gov.hmrc.play.test.UnitSpec

class CompanyBankAccountDetailsSpec extends UnitSpec with VatRegistrationFixture {

  "apply" should {
    val vatScheme = VatScheme(validRegId)
    val someBankAccount = VatBankAccount(accountName = "test", accountNumber = "12345678", accountSortCode = "12-12-12")

    "convert VatFinancials with bank account to view model" in {
      val vatFinancialsWithAccount = VatFinancials(
        bankAccount = Some(someBankAccount),
        turnoverEstimate = 100L,
        reclaimVatOnMostReturns = true,
        accountingPeriods = monthlyAccountingPeriod
      )
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithAccount))
      ApiModelTransformer[CompanyBankAccountDetails].toViewModel(vs) shouldBe
        Some(CompanyBankAccountDetails(someBankAccount.accountName, someBankAccount.accountNumber, someBankAccount.accountSortCode))
    }

    "convert VatFinancials without bank account to view model" in {
      val vatFinancialsWithoutAccount = VatFinancials(
        turnoverEstimate = 100L,
        reclaimVatOnMostReturns = true,
        accountingPeriods = monthlyAccountingPeriod
      )
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithoutAccount))

      ApiModelTransformer[CompanyBankAccountDetails].toViewModel(vs) shouldBe None
    }

    "convert VatScheme without VatFinancials to empty view model" in {
      val vs = vatScheme.copy(financials = None)
      ApiModelTransformer[CompanyBankAccountDetails].toViewModel(vs) shouldBe None
    }
  }


  "ViewModelFormat" should {

    val s4lVatFinancials: S4LVatFinancials = S4LVatFinancials(companyBankAccountDetails = Some(validBankAccountDetails))

    "extract CompanyBankAccountDetails from VatFinancials" in {
      CompanyBankAccountDetails.viewModelFormat.read(s4lVatFinancials) shouldBe Some(validBankAccountDetails)
    }

    "update empty VatFinancials with CompanyBankAccountDetails" in {
      CompanyBankAccountDetails.viewModelFormat.update(validBankAccountDetails, Option.empty[S4LVatFinancials]).
        companyBankAccountDetails shouldBe Some(validBankAccountDetails)
    }

    "update non-empty VatFinancials with CompanyBankAccountDetails" in {
      CompanyBankAccountDetails.viewModelFormat.update(validBankAccountDetails, Some(s4lVatFinancials)).
        companyBankAccountDetails shouldBe Some(validBankAccountDetails)
    }
  }

}
