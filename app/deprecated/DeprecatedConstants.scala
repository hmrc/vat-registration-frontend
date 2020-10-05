/*
 * Copyright 2020 HM Revenue & Customs
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

package deprecated

import org.joda.time.{DateTime, LocalDate => JodaLocalDate}
import play.api.libs.json.JodaWrites.{DefaultJodaLocalDateWrites, JodaDateTimeWrites}
import play.api.libs.json.{Format, JodaReads, JsResult, JsValue}

object DeprecatedConstants {
  @Deprecated
  val fakeCompanyName: String = "FAKECOMPANY Ltd."

  @Deprecated // Migrate to Java LocalDate
  implicit val jodaLocalDateFormat = new Format[JodaLocalDate] {
    override def reads(json: JsValue): JsResult[JodaLocalDate] = JodaReads.DefaultJodaLocalDateReads.reads(json)
    override def writes(o: JodaLocalDate): JsValue = DefaultJodaLocalDateWrites.writes(o)
  }

  @Deprecated // Migrate to Java LocalDate
  implicit val jodaDateTimeFormat = new Format[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] = JodaReads.DefaultJodaDateTimeReads.reads(json)
    override def writes(o: DateTime): JsValue = JodaDateTimeWrites.writes(o)
  }

}
