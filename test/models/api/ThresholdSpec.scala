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

package models.api

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, JsString, JsSuccess, Json}

import java.time.LocalDate

class ThresholdSpec extends PlaySpec {

  val testPrevDate = LocalDate.parse("2019-01-01")
  val testDateIn12Months = LocalDate.parse("2020-01-01")
  val testNextDate = LocalDate.parse("2019-01-02")
  val validMinimalJson = Json.obj("mandatoryRegistration" -> true)

  val validJsonWithPrev30Days = validMinimalJson.as[JsObject] + ("thresholdPreviousThirtyDays" -> JsString(testPrevDate.toString))
  val validJsonWith12Months = validJsonWithPrev30Days.as[JsObject] + ("thresholdInTwelveMonths" -> JsString(testDateIn12Months.toString))
  val validFullJson = validJsonWith12Months.as[JsObject] + ("thresholdNextThirtyDays" -> JsString(testNextDate.toString))

  "Threshold model" should {
    "parse from minimal valid json" in {
      val res = Json.fromJson[Threshold](validMinimalJson)
      res mustBe JsSuccess(Threshold(mandatoryRegistration = true))
    }

    "parse valid json with the previous 30 days date set" in {
      val res = Json.fromJson[Threshold](validJsonWithPrev30Days)
      res mustBe JsSuccess(Threshold(
        mandatoryRegistration = true,
        thresholdPreviousThirtyDays = Some(testPrevDate)
      ))
    }

    "parse valid json with the threshold in 12 months date set" in {
      val res = Json.fromJson[Threshold](validJsonWith12Months)
      res mustBe JsSuccess(Threshold(
        mandatoryRegistration = true,
        thresholdPreviousThirtyDays = Some(testPrevDate),
        thresholdInTwelveMonths = Some(testDateIn12Months)
      ))
    }

    "parse valid json with all fields set" in {
      val res = Json.fromJson[Threshold](validFullJson)
      res mustBe JsSuccess(Threshold(
        mandatoryRegistration = true,
        thresholdPreviousThirtyDays = Some(testPrevDate),
        thresholdInTwelveMonths = Some(testDateIn12Months),
        thresholdNextThirtyDays = Some(testNextDate)
      ))
    }
  }

}
