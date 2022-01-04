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

package models

import play.api.libs.json._

import java.time.LocalDate
import scala.language.implicitConversions

case class Start(date: Option[LocalDate])

object Start {
  implicit val format = Json.format[Start]
}

object DateSelection extends Enumeration {
  val company_registration_date = Value
  val business_start_date = Value
  val specific_date = Value
  val calculated_date = Value

  implicit def toString(f: DateSelection.Value): String = f.toString
}
