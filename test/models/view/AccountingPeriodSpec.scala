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

package models.view

import fixtures.VatRegistrationFixture
import models.api.{VatAccountingPeriod, VatFinancials, VatScheme}
import models.view.AccountingPeriod.{FEB_MAY_AUG_NOV, JAN_APR_JUL_OCT, MAR_JUN_SEP_DEC}
import models.{ApiModelTransformer, ViewModelTransformer}
import uk.gov.hmrc.play.test.UnitSpec

class AccountingPeriodSpec extends UnitSpec with VatRegistrationFixture {

  val vatAccountingPeriod1 = VatAccountingPeriod(Some("jan_apr_jul_oct"), "quarterly")
  val vatAccountingPeriod2 = VatAccountingPeriod(Some("feb_may_aug_nov"), "quarterly")
  val vatAccountingPeriod3 = VatAccountingPeriod(Some("mar_jun_sep_dec"), "quarterly")
  val turnover = 100L

  val vatFinancialsWithAccountingPeriod1 = VatFinancials(
    turnoverEstimate = turnover,
    reclaimVatOnMostReturns = false,
    vatAccountingPeriod = vatAccountingPeriod1
  )

  val vatFinancialsWithAccountingPeriod2 = VatFinancials(
    turnoverEstimate = turnover,
    reclaimVatOnMostReturns = false,
    vatAccountingPeriod = vatAccountingPeriod2
  )

  val vatFinancialsWithAccountingPeriod3 = VatFinancials(
    turnoverEstimate = turnover,
    reclaimVatOnMostReturns = false,
    vatAccountingPeriod = vatAccountingPeriod3
  )

  val vatScheme = VatScheme(validRegId)

  "toApi" should {
    val accountingPeriod = AccountingPeriod(Some(JAN_APR_JUL_OCT))

    val vatFinancials = VatFinancials(
      turnoverEstimate = turnover,
      reclaimVatOnMostReturns = false,
      vatAccountingPeriod = vatAccountingPeriod2
    )

    val differentVatFinancials = VatFinancials(
      turnoverEstimate = turnover,
      reclaimVatOnMostReturns = false,
      vatAccountingPeriod = vatAccountingPeriod1
    )

    "update VatFinancials with new AccountingPeriod" in {
      ViewModelTransformer[AccountingPeriod, VatFinancials]
        .toApi(accountingPeriod, vatFinancials) shouldBe differentVatFinancials
    }
  }

  "apply" should {

    "convert VatFinancials with accounting period jan_apr_jul_oct to view model" in {
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithAccountingPeriod1))
      ApiModelTransformer[AccountingPeriod].toViewModel(vs) shouldBe Some(AccountingPeriod(Some(JAN_APR_JUL_OCT)))
    }

    "convert VatFinancials with accounting period feb_may_aug_nov to view model" in {
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithAccountingPeriod2))
      ApiModelTransformer[AccountingPeriod].toViewModel(vs) shouldBe Some(AccountingPeriod(Some(FEB_MAY_AUG_NOV)))
    }

    "convert VatFinancials with accounting period mar_jun_sep_dec to view model" in {
      val vs = vatScheme.copy(financials = Some(vatFinancialsWithAccountingPeriod3))
      ApiModelTransformer[AccountingPeriod].toViewModel(vs) shouldBe Some(AccountingPeriod(Some(MAR_JUN_SEP_DEC)))
    }

    "convert VatScheme without VatFinancials to empty view model" in {
      val vs = vatScheme.copy(financials = None)
      ApiModelTransformer[AccountingPeriod].toViewModel(vs) shouldBe None
    }

  }

}

