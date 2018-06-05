/*
 * Copyright 2018 HM Revenue & Customs
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

import features.tradingDetails.{TradingDetails, TradingNameView}
import fixtures.VatRegistrationFixture
import org.scalatest.Inspectors
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class TradingDetailsModelSpec extends UnitSpec with Inspectors with VatRegistrationFixture {
  "S4LTradingDetails" should {
    implicit val frmt = TradingDetails.apiFormat

    "construct valid json" in {
      Json.toJson(TradingDetails(
        Some(TradingNameView(yesNo = true, Some("test"))),
        Some(true)
      )) shouldBe Json.parse(
        """{
          |"tradingName":"test",
          |"eoriRequested":true
          |}""".stripMargin)
    }

    "construct valid model" in {
      Json.parse(
        """{
          |"tradingName":"test",
          |"eoriRequested":true
          |}"""
          .stripMargin).as[TradingDetails] shouldBe TradingDetails(
        Some(TradingNameView(yesNo = true, Some("test"))),
        Some(true)
      )
    }
  }
}
