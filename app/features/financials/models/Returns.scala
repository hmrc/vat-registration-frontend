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

package features.financials.models

import java.time.LocalDate
import play.api.libs.json._

object Frequency extends Enumeration {
  val monthly   = Value
  val quarterly = Value

  implicit val format = Format(Reads.enumNameReads(Frequency), Writes.enumNameWrites)
}

object Stagger extends Enumeration {
  val jan_feb_mar = Value
  val apr_may_jun = Value
  val jul_aug_sep = Value
  val oct_nov_dec = Value

  implicit val format = Format(Reads.enumNameReads(Stagger), Writes.enumNameWrites)
}

case class Returns(reclaimVatOnMostReturns: Option[Boolean],
                   frequency: Option[Frequency.Value],
                   staggerStart: Option[Stagger.Value],
                   vatStartDate: Option[LocalDate])
object Returns {
  implicit val format = Json.format[Returns]

  def empty = Returns(None, None, None, None)
}
