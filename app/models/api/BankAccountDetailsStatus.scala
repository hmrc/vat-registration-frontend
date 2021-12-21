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

package models.api

import play.api.libs.json.{Format, JsString}

sealed trait BankAccountDetailsStatus
case object ValidStatus extends BankAccountDetailsStatus
case object InvalidStatus extends BankAccountDetailsStatus
case object IndeterminateStatus extends BankAccountDetailsStatus

object BankAccountDetailsStatus {

  val map: Map[BankAccountDetailsStatus, String] = Map(
    ValidStatus -> "yes",
    InvalidStatus -> "no",
    IndeterminateStatus -> "indeterminate"
  )
  val inverseMap: Map[String, BankAccountDetailsStatus] = map.map(_.swap)

  def fromString(value: String): BankAccountDetailsStatus = inverseMap(value)
  def toJsString(value: BankAccountDetailsStatus): JsString = JsString(map(value))

  implicit val format = Format[BankAccountDetailsStatus](_.validate[String] map fromString, toJsString)
}