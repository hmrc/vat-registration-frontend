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

case class VatAccountingPeriod(periodStart: Option[String], frequency: String)


object VatAccountingPeriod {

  implicit val format = (
    (__ \ "periodStart").formatNullable[String] and
      (__ \ "frequency").format[String]) (VatAccountingPeriod.apply, unlift(VatAccountingPeriod.unapply))

  def empty: VatAccountingPeriod = VatAccountingPeriod(None, "")

  // TODO remove 'default' once we have VatAccountingPeriod story is in place
  def default: VatAccountingPeriod = VatAccountingPeriod(None, "monthly")
}
