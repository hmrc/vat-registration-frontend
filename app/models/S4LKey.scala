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
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.labour.{SkilledWorkers, _}
import models.view.test.SicStub
import models.view.vatFinancials._
import models.view.vatTradingDetails.{StartDateView, TaxableTurnover, TradingNameView, VoluntaryRegistration}

trait S4LKey[T] {

  val key: String

}

object S4LKey {

  def apply[T](implicit cacheKey: S4LKey[T]): S4LKey[T] = cacheKey

  def apply[T](k: String): S4LKey[T] = new S4LKey[T] {
    override val key = k
  }

  implicit val startDateS4LKey: S4LKey[StartDateView] = S4LKey("StartDate")
  implicit val tradingNameS4LKey: S4LKey[TradingNameView] = S4LKey("TradingNameView")
  implicit val voluntaryRegistrationS4LKey: S4LKey[VoluntaryRegistration] = S4LKey("VoluntaryRegistration")
  implicit val taxableTurnoverS4LKey: S4LKey[TaxableTurnover] = S4LKey("TaxableTurnover")
  implicit val estimateVatTurnoverS4LKey: S4LKey[EstimateVatTurnover] = S4LKey("EstimateVatTurnover")
  implicit val zeroRatedSalesS4LKey: S4LKey[ZeroRatedSales] = S4LKey("ZeroRatedSales")
  implicit val estimateZeroRatedSalesS4LKey: S4LKey[EstimateZeroRatedSales] = S4LKey("EstimateZeroRatedSales")
  implicit val vatChargeExpectancyS4LKey: S4LKey[VatChargeExpectancy] = S4LKey("VatChargeExpectancy")
  implicit val vatReturnFrequencyS4LKey: S4LKey[VatReturnFrequency] = S4LKey("VatReturnFrequency")
  implicit val accountingPeriodS4LKey: S4LKey[AccountingPeriod] = S4LKey("AccountingPeriod")
  implicit val companyBankAccountS4LKey: S4LKey[CompanyBankAccount] = S4LKey("CompanyBankAccount")
  implicit val companyBankAccountDetailsS4LKey: S4LKey[CompanyBankAccountDetails] = S4LKey("CompanyBankAccountDetails")
  implicit val businessActivityDescriptionS4LKey: S4LKey[BusinessActivityDescription] = S4LKey("BusinessActivityDescription")
  implicit val sicStub: S4LKey[SicStub] = S4LKey("SicStub")
  implicit val companyProvideWorkers: S4LKey[CompanyProvideWorkers] = S4LKey("CompanyProvideWorkers")
  implicit val notForProfit: S4LKey[NotForProfit] = S4LKey("NotForProfit")
  implicit val workers: S4LKey[Workers] = S4LKey("Workers")
  implicit val temporaryContracts: S4LKey[TemporaryContracts] = S4LKey("TemporaryContracts")
  implicit val skilledWorkers: S4LKey[SkilledWorkers] = S4LKey("SkilledWorkers")


}
