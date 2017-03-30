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

import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.vatFinancials._
import models.view.vatTradingDetails.{StartDateView, TaxableTurnover, TradingNameView, VoluntaryRegistration}
import uk.gov.hmrc.play.test.UnitSpec

class CacheKeySpec extends UnitSpec {

  "cacheKey" should {
    "be generated for each view model" in {
      S4LKey[StartDateView].key shouldBe "StartDate"
      S4LKey[TradingNameView].key shouldBe "TradingNameView"
      S4LKey[VoluntaryRegistration].key shouldBe "VoluntaryRegistration"
      S4LKey[TaxableTurnover].key shouldBe "TaxableTurnover"
      S4LKey[EstimateVatTurnover].key shouldBe "EstimateVatTurnover"
      S4LKey[ZeroRatedSales].key shouldBe "ZeroRatedSales"
      S4LKey[EstimateZeroRatedSales].key shouldBe "EstimateZeroRatedSales"
      S4LKey[VatChargeExpectancy].key shouldBe "VatChargeExpectancy"
      S4LKey[VatReturnFrequency].key shouldBe "VatReturnFrequency"
      S4LKey[AccountingPeriod].key shouldBe "AccountingPeriod"
      S4LKey[CompanyBankAccount].key shouldBe "CompanyBankAccount"
      S4LKey[CompanyBankAccountDetails].key shouldBe "CompanyBankAccountDetails"
      S4LKey[BusinessActivityDescription].key shouldBe "BusinessActivityDescription"
    }
  }

}
