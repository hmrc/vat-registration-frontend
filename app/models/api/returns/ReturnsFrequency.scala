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

package models.api.returns

import play.api.libs.json._

sealed trait ReturnsFrequency
case object Monthly extends ReturnsFrequency
case object Quarterly extends ReturnsFrequency
case object Annual extends ReturnsFrequency

object ReturnsFrequency {

  val monthly: String = "monthly"
  val quarterly: String = "quarterly"
  val annual: String = "annual"

  val reads: Reads[ReturnsFrequency] = Reads[ReturnsFrequency] {
    case JsString(`monthly`) => JsSuccess(Monthly)
    case JsString(`quarterly`) => JsSuccess(Quarterly)
    case JsString(`annual`) => JsSuccess(Annual)
    case _ => JsError("Could not parse payment frequency")
  }

  val writes: Writes[ReturnsFrequency] = Writes[ReturnsFrequency] { period =>
    JsString(period.toString.toLowerCase)
  }

  implicit val format: Format[ReturnsFrequency] = Format(reads, writes)

}

