/*
 * Copyright 2022 HM Revenue & Customs
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

import models.api.vatapplication._
import play.api.libs.json.{JsObject, JsSuccess, Json}
import testHelpers.VatRegSpec

import java.time.LocalDate

class VatApplicationSpec extends VatRegSpec {

  override val testDate: LocalDate = LocalDate.now()

  val testMonthlyVatApplication: VatApplication = VatApplication(
    tradeVatGoodsOutsideUk = Some(false),
    eoriRequested = Some(false),
    turnoverEstimate = Some(testTurnover),
    zeroRatedSupplies = Some(testZeroRatedSupplies),
    claimVatRefunds = Some(true),
    appliedForExemption = Some(false),
    startDate = Some(testDate),
    returnsFrequency = Some(Monthly),
    staggerStart = Some(MonthlyStagger)
  )

  val testAnnualVatApplication: VatApplication = VatApplication(
    tradeVatGoodsOutsideUk = Some(false),
    eoriRequested = Some(false),
    turnoverEstimate = Some(testTurnover),
    zeroRatedSupplies = Some(testZeroRatedSupplies),
    claimVatRefunds = Some(true),
    appliedForExemption = Some(false),
    startDate = Some(testDate),
    returnsFrequency = Some(Annual),
    staggerStart = Some(JanDecStagger),
    annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment), Some(BankGIRO)))
  )

  val validJsonMonthly: JsObject = Json.obj(
    "tradeVatGoodsOutsideUk" -> false,
    "eoriRequested" -> false,
    "turnoverEstimate" -> testTurnover,
    "zeroRatedSupplies" -> testZeroRatedSupplies,
    "claimVatRefunds" -> true,
    "appliedForExemption" -> false,
    "startDate" -> testDate,
    "returnsFrequency" -> Json.toJson[ReturnsFrequency](Monthly),
    "staggerStart" -> Json.toJson[Stagger](MonthlyStagger)
  )

  val validJsonAnnual: JsObject = Json.obj(
    "tradeVatGoodsOutsideUk" -> false,
    "eoriRequested" -> false,
    "turnoverEstimate" -> testTurnover,
    "zeroRatedSupplies" -> testZeroRatedSupplies,
    "claimVatRefunds" -> true,
    "appliedForExemption" -> false,
    "startDate" -> testDate,
    "returnsFrequency" -> Json.toJson[ReturnsFrequency](Annual),
    "staggerStart" -> Json.toJson[Stagger](JanDecStagger),
    "annualAccountingDetails" -> Json.obj(
      "paymentMethod" -> Json.toJson[PaymentMethod](BankGIRO),
      "paymentFrequency" -> Json.toJson[PaymentFrequency](MonthlyPayment)
    )
  )

  "VatApplication" should {
    "construct valid Json from the monthly model" in {
      Json.toJson[VatApplication](testMonthlyVatApplication) mustBe validJsonMonthly
    }

    "construct a valid model from the monthly Json" in {
      Json.fromJson[VatApplication](validJsonMonthly) mustBe JsSuccess(testMonthlyVatApplication)
    }

    "construct valid Json from the annual model" in {
      Json.toJson[VatApplication](testAnnualVatApplication) mustBe validJsonAnnual
    }

    "construct a valid model from the annual Json" in {
      Json.fromJson[VatApplication](validJsonAnnual) mustBe JsSuccess(testAnnualVatApplication)
    }
  }
}
