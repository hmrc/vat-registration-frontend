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

package models.api

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class VatScheme(
                      id: String,
                      tradingDetails: VatTradingDetails,
                      vatChoice: VatChoice
                    )

object VatScheme {
  val r =
    (__ \ "ID").read[String] and
      (__ \ "trading-details").read[VatTradingDetails] and
      (__ \ "vat-choice").read[VatChoice]

  val w =
    (__ \ "ID").write[String] and
      (__ \ "trading-details").write[VatTradingDetails] and
      (__ \ "vat-choice").write[VatChoice]

  val apiReads: Reads[VatScheme] = r(VatScheme.apply _)
  val apiWrites: Writes[VatScheme] = w(unlift(VatScheme.unapply))

  implicit val format = Format(apiReads, apiWrites)
}