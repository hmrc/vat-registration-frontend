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

package models.view.vatFinancials.vatAccountingPeriod

import fixtures.VatRegistrationFixture
import models.api.{VatAccountingPeriod, VatFinancials, VatScheme}
import models.view.vatFinancials.vatAccountingPeriod.AccountingPeriod.{FEB_MAY_AUG_NOV, JAN_APR_JUL_OCT, MAR_JUN_SEP_DEC}
import models.{ApiModelTransformer, S4LVatFinancials}
import uk.gov.hmrc.play.test.UnitSpec

class AccountingPeriodSpec extends UnitSpec with VatRegistrationFixture {

  val accountingPeriods1 = VatAccountingPeriod(periodStart = Some("jan_apr_jul_oct"), frequency = "quarterly")
  val accountingPeriods2 = VatAccountingPeriod(periodStart = Some("feb_may_aug_nov"), frequency = "quarterly")
  val accountingPeriods3 = VatAccountingPeriod(periodStart = Some("mar_jun_sep_dec"), frequency = "quarterly")
  val turnover = 100L

  val vatFinancialsWithAccountingPeriod1 = VatFinancials(
    turnoverEstimate = turnover,
    reclaimVatOnMostReturns = false,
    accountingPeriods = accountingPeriods1
  )

  val vatFinancialsWithAccountingPeriod2 = VatFinancials(
    turnoverEstimate = turnover,
    reclaimVatOnMostReturns = false,
    accountingPeriods = accountingPeriods2
  )

  val vatFinancialsWithAccountingPeriod3 = VatFinancials(
    turnoverEstimate = turnover,
    reclaimVatOnMostReturns = false,
    accountingPeriods = accountingPeriods3
  )

  val vatScheme = VatScheme(testRegId)

  "apply" should {

    "convert VatFinancials with accounting period jan_apr_jul_oct to view model" in {
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithAccountingPeriod1))
      ApiModelTransformer[AccountingPeriod].toViewModel(vs) shouldBe Some(AccountingPeriod(JAN_APR_JUL_OCT))
    }

    "convert VatFinancials with accounting period feb_may_aug_nov to view model" in {
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithAccountingPeriod2))
      ApiModelTransformer[AccountingPeriod].toViewModel(vs) shouldBe Some(AccountingPeriod(FEB_MAY_AUG_NOV))
    }

    "convert VatFinancials with accounting period mar_jun_sep_dec to view model" in {
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithAccountingPeriod3))
      ApiModelTransformer[AccountingPeriod].toViewModel(vs) shouldBe Some(AccountingPeriod(MAR_JUN_SEP_DEC))
    }

    "convert VatScheme without VatFinancials to empty view model" in {
      val vs = vatScheme.copy(financials = None)
      ApiModelTransformer[AccountingPeriod].toViewModel(vs) shouldBe None
    }

  }

  "ViewModelFormat" should {
    val testAccountingPeriod = AccountingPeriod(AccountingPeriod.MAR_JUN_SEP_DEC)

    val s4lVatFinancials: S4LVatFinancials = S4LVatFinancials(accountingPeriod = Some(testAccountingPeriod))

    "extract AccountingPeriod from VatFinancials" in {
      AccountingPeriod.viewModelFormat.read(s4lVatFinancials) shouldBe Some(testAccountingPeriod)
    }

    "update empty VatFinancials with AccountingPeriod" in {
      AccountingPeriod.viewModelFormat.update(testAccountingPeriod, Option.empty[S4LVatFinancials]).
        accountingPeriod shouldBe Some(testAccountingPeriod)
    }

    "update non-empty VatFinancials with AccountingPeriod" in {
      AccountingPeriod.viewModelFormat.update(testAccountingPeriod, Some(s4lVatFinancials)).
        accountingPeriod shouldBe Some(testAccountingPeriod)
    }
  }

}

