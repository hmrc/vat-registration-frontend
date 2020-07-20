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

package features.sicAndCompliance.models

import play.api.libs.json.{Json, OFormat}

case class  CompanyProvideWorkers(yesNo: String)
object CompanyProvideWorkers {
  val PROVIDE_WORKERS_YES = "PROVIDE_WORKERS_YES"
  val PROVIDE_WORKERS_NO = "PROVIDE_WORKERS_NO"

  def toBool(answer:String):Option[Boolean] = if(PROVIDE_WORKERS_YES == answer) Some(true) else Some(false)

  val valid = (item: String) => List(PROVIDE_WORKERS_YES, PROVIDE_WORKERS_NO).contains(item.toUpperCase)

  implicit val format = Json.format[CompanyProvideWorkers]
}

case class  SkilledWorkers(yesNo: String)
object SkilledWorkers {
  def toBool(answer:String):Option[Boolean] = if(SKILLED_WORKERS_YES == answer) Some(true) else Some(false)

  val SKILLED_WORKERS_YES = "SKILLED_WORKERS_YES"
  val SKILLED_WORKERS_NO = "SKILLED_WORKERS_NO"

  val valid = (item: String) => List(SKILLED_WORKERS_YES, SKILLED_WORKERS_NO).contains(item.toUpperCase)

  implicit val format = Json.format[SkilledWorkers]
}

case class TemporaryContracts(yesNo: String)
object TemporaryContracts {
  def toBool(answer:String) = if(TEMP_CONTRACTS_YES == answer) Some(true) else Some(false)

  val TEMP_CONTRACTS_YES = "TEMP_CONTRACTS_YES"
  val TEMP_CONTRACTS_NO = "TEMP_CONTRACTS_NO"

  val valid = (item: String) => List(TEMP_CONTRACTS_YES, TEMP_CONTRACTS_NO).contains(item.toUpperCase)

  implicit val format = Json.format[TemporaryContracts]
}

case class Workers(numberOfWorkers: Int)
object Workers {
  implicit val format: OFormat[Workers] = Json.format[Workers]
}
