/*
 * Copyright 2021 HM Revenue & Customs
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

import models.api.returns._
import play.api.libs.json.{JsObject, JsSuccess, Json}
import testHelpers.VatRegSpec

import java.time.LocalDate

class ReturnsSpec extends VatRegSpec {

  override val testDate: LocalDate = LocalDate.now()
  val testZeroRatedSupplies: BigDecimal = 10000.5

  val testMonthlyReturns: Returns = Returns(Some(testZeroRatedSupplies), Some(true), Some(Monthly), Some(MonthlyStagger), Some(testDate))
  val testAnnualReturns: Returns = Returns(Some(testZeroRatedSupplies), Some(false), Some(Annual), Some(JanDecStagger), Some(testDate), Some(AASDetails(Some(BankGIRO), Some(MonthlyPayment))))

  val validJsonMonthly: JsObject = Json.obj(
    "zeroRatedSupplies" -> testZeroRatedSupplies,
    "reclaimVatOnMostReturns" -> true,
    "returnsFrequency" -> Json.toJson[ReturnsFrequency](Monthly),
    "staggerStart" -> Json.toJson[Stagger](MonthlyStagger),
    "startDate" -> testDate
  )

  val validJsonAnnual: JsObject = Json.obj(
    "zeroRatedSupplies" -> testZeroRatedSupplies,
    "reclaimVatOnMostReturns" -> false,
    "returnsFrequency" -> Json.toJson[ReturnsFrequency](Annual),
    "staggerStart" -> Json.toJson[Stagger](JanDecStagger),
    "startDate" -> testDate,
    "annualAccountingDetails" -> Json.obj(
      "paymentMethod" -> Json.toJson[PaymentMethod](BankGIRO),
      "paymentFrequency" -> Json.toJson[PaymentFrequency](MonthlyPayment)
    )
  )

  "Returns" should {
    "construct valid Json from the monthly model" in {
      Json.toJson[Returns](testMonthlyReturns) mustBe validJsonMonthly
    }

    "construct a valid model from the monthly Json" in {
      Json.fromJson[Returns](validJsonMonthly) mustBe JsSuccess(testMonthlyReturns)
    }

    "construct valid Json from the annual model" in {
      Json.toJson[Returns](testAnnualReturns) mustBe validJsonAnnual
    }

    "construct a valid model from the annual Json" in {
      Json.fromJson[Returns](validJsonAnnual) mustBe JsSuccess(testAnnualReturns)
    }
  }
}
