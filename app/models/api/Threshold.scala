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

package models.api

import java.time.LocalDate

import play.api.libs.json.{Json, OFormat}

//using backend model because view models not needed as these questions don't leave in this service
case class Threshold(mandatoryRegistration: Boolean,
                     thresholdPreviousThirtyDays: Option[LocalDate] = None,
                     thresholdInTwelveMonths: Option[LocalDate] = None)

object Threshold {
  implicit val format: OFormat[Threshold] = Json.format
  val SELLS: String = "alreadySellsVATTaxableGoodsOrServices"
  val INTENDS_TO_SELL: String = "intendsToSellVATTaxableGoodsOrServices"
}
