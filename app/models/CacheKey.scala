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

import models.view.sicAndCompliance.{BusinessActivityDescription, CulturalComplianceQ1}
import models.view.test.SicStub
import models.view.vatChoice.{StartDate, TaxableTurnover, VoluntaryRegistration}
import models.view.vatFinancials._
import models.view.vatTradingDetails.TradingName

trait CacheKey[T] {

  def cacheKey: String

}

object CacheKey {

  def apply[T](implicit cacheKey: CacheKey[T]): CacheKey[T] = cacheKey

  def apply[T](key: String): CacheKey[T] = new CacheKey[T] {
    override def cacheKey = key
  }

  implicit val startDateCacheKey: CacheKey[StartDate] = CacheKey("StartDate")
  implicit val tradingNameCacheKey: CacheKey[TradingName] = CacheKey("TradingName")
  implicit val voluntaryRegistrationCacheKey: CacheKey[VoluntaryRegistration] = CacheKey("VoluntaryRegistration")
  implicit val taxableTurnoverCacheKey: CacheKey[TaxableTurnover] = CacheKey("TaxableTurnover")
  implicit val estimateVatTurnoverCacheKey: CacheKey[EstimateVatTurnover] = CacheKey("EstimateVatTurnover")
  implicit val zeroRatedSalesCacheKey: CacheKey[ZeroRatedSales] = CacheKey("ZeroRatedSales")
  implicit val estimateZeroRatedSalesCacheKey: CacheKey[EstimateZeroRatedSales] = CacheKey("EstimateZeroRatedSales")
  implicit val vatChargeExpectancyCacheKey: CacheKey[VatChargeExpectancy] = CacheKey("VatChargeExpectancy")
  implicit val vatReturnFrequencyCacheKey: CacheKey[VatReturnFrequency] = CacheKey("VatReturnFrequency")
  implicit val accountingPeriodCacheKey: CacheKey[AccountingPeriod] = CacheKey("AccountingPeriod")
  implicit val companyBankAccountCacheKey: CacheKey[CompanyBankAccount] = CacheKey("CompanyBankAccount")
  implicit val companyBankAccountDetailsCacheKey: CacheKey[CompanyBankAccountDetails] = CacheKey("CompanyBankAccountDetails")
  implicit val businessActivityDescriptionCacheKey: CacheKey[BusinessActivityDescription] = CacheKey("BusinessActivityDescription")
  implicit val sicStub: CacheKey[SicStub] = CacheKey("SicStub")
  implicit val culturalComplianceQ1: CacheKey[CulturalComplianceQ1] = CacheKey("CulturalComplianceQ1")

}
