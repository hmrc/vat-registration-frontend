/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class VatThreshold(date: LocalDateTime, amount: BigDecimal)

object VatThreshold {

  val dateFormat: Format[LocalDateTime] = Format[LocalDateTime](
    Reads[LocalDateTime](js =>
      js.validate[String].map(
        LocalDateTime.parse(_, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
      )
    ),
    Writes[LocalDateTime] { dt =>
      JsString(dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }
  )

  val reads: Reads[VatThreshold] = (
    (__ \ "date").read[LocalDateTime](dateFormat) and
      (__ \ "amount").read[BigDecimal]
    )(VatThreshold.apply _)

  val writes: Writes[VatThreshold] = (
    (__ \ "date").write(dateFormat) and
      (__ \ "amount").write[BigDecimal]
    )(unlift(VatThreshold.unapply))

  implicit val format: Format[VatThreshold] = Format(reads, writes)
}