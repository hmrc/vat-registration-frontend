/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.libs.json._

case class VatAccountingPeriod(
                                frequency: String, // "monthly" or "quarterly"
                                periodStart: Option[String] = None // "jan_apr_jul_oct", "feb_may_aug_nov" or "mar_jun_sep_dec"
                              )

object VatAccountingPeriod {

  implicit val format: OFormat[VatAccountingPeriod] = Json.format[VatAccountingPeriod]

}
