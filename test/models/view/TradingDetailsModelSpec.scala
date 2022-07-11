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

package models.view

import fixtures.VatRegistrationFixture
import models.{TradingDetails, TradingNameView}
import org.scalatest.Inspectors
import play.api.libs.json.Json
import testHelpers.VatRegSpec

class TradingDetailsModelSpec extends VatRegSpec with Inspectors with VatRegistrationFixture {
  "S4LTradingDetails" should {
    implicit val frmt = TradingDetails.apiFormat

    "construct valid json" in {
      Json.toJson(TradingDetails(
        Some(TradingNameView(yesNo = true, Some("test"))))) mustBe Json.parse(
        """{
          |"tradingName":"test"
          |}""".stripMargin)
    }

    "construct valid model" in {
      Json.parse(
        """{
          |"tradingName":"test"
          |}"""
          .stripMargin).as[TradingDetails] mustBe TradingDetails(
        Some(TradingNameView(yesNo = true, Some("test"))))
    }
  }
}
