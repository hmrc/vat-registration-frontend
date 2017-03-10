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

package models

import enums.CacheKeys
import models.view._
import uk.gov.hmrc.play.test.UnitSpec

class CacheKeySpec extends UnitSpec {

  "cacheKey" should {
    "be generated for each view model" in {
      CacheKey[StartDate].cacheKey shouldBe CacheKeys.StartDate.toString
      CacheKey[TradingName].cacheKey shouldBe CacheKeys.TradingName.toString
      CacheKey[VoluntaryRegistration].cacheKey shouldBe CacheKeys.VoluntaryRegistration.toString
      CacheKey[TaxableTurnover].cacheKey shouldBe CacheKeys.TaxableTurnover.toString
      CacheKey[EstimateVatTurnover].cacheKey shouldBe CacheKeys.EstimateVatTurnover.toString
      CacheKey[ZeroRatedSales].cacheKey shouldBe CacheKeys.ZeroRatedSales.toString
      CacheKey[EstimateZeroRatedSales].cacheKey shouldBe CacheKeys.EstimateZeroRatedSales.toString
      CacheKey[VatChargeExpectancy].cacheKey shouldBe CacheKeys.VatChargeExpectancy.toString
      CacheKey[VatReturnFrequency].cacheKey shouldBe CacheKeys.VatReturnFrequency.toString
      CacheKey[AccountingPeriod].cacheKey shouldBe CacheKeys.AccountingPeriod.toString
      CacheKey[CompanyBankAccount].cacheKey shouldBe CacheKeys.CompanyBankAccount.toString
      CacheKey[CompanyBankAccountDetails].cacheKey shouldBe CacheKeys.CompanyBankAccountDetails.toString
      CacheKey[BusinessActivityDescription].cacheKey shouldBe CacheKeys.BusinessActivityDescription.toString
    }
  }

}
