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

import models.view._
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.vatChoice.{StartDate, TaxableTurnover, VoluntaryRegistration}
import models.view.vatFinancials._
import models.view.vatTradingDetails.TradingName
import uk.gov.hmrc.play.test.UnitSpec

class CacheKeySpec extends UnitSpec {

  "cacheKey" should {
    "be generated for each view model" in {
      CacheKey[StartDate].cacheKey shouldBe "StartDate"
      CacheKey[TradingName].cacheKey shouldBe "TradingName"
      CacheKey[VoluntaryRegistration].cacheKey shouldBe "VoluntaryRegistration"
      CacheKey[TaxableTurnover].cacheKey shouldBe "TaxableTurnover"
      CacheKey[EstimateVatTurnover].cacheKey shouldBe "EstimateVatTurnover"
      CacheKey[ZeroRatedSales].cacheKey shouldBe "ZeroRatedSales"
      CacheKey[EstimateZeroRatedSales].cacheKey shouldBe "EstimateZeroRatedSales"
      CacheKey[VatChargeExpectancy].cacheKey shouldBe "VatChargeExpectancy"
      CacheKey[VatReturnFrequency].cacheKey shouldBe "VatReturnFrequency"
      CacheKey[AccountingPeriod].cacheKey shouldBe "AccountingPeriod"
      CacheKey[CompanyBankAccount].cacheKey shouldBe "CompanyBankAccount"
      CacheKey[CompanyBankAccountDetails].cacheKey shouldBe "CompanyBankAccountDetails"
      CacheKey[BusinessActivityDescription].cacheKey shouldBe "BusinessActivityDescription"
    }
  }

}
