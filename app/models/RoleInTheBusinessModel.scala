/*
 * Copyright 2021 HM Revenue & Customs
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

sealed trait RoleInTheBusiness

case object OwnerProprietor extends RoleInTheBusiness
case object Partner extends RoleInTheBusiness
case object Director extends RoleInTheBusiness
case object CompanySecretary extends RoleInTheBusiness
case object Trustee extends RoleInTheBusiness
case object BoardMember extends RoleInTheBusiness
case object AccountantAgent extends RoleInTheBusiness
case object Representative extends RoleInTheBusiness
case object AuthorisedEmployee extends RoleInTheBusiness
case object Other extends RoleInTheBusiness
case object HmrcOfficer extends RoleInTheBusiness

object RoleInTheBusiness {

  val stati: Map[RoleInTheBusiness, String] = Map[RoleInTheBusiness, String] (
    OwnerProprietor -> "01",
    Partner -> "02",
    Director -> "03",
    CompanySecretary -> "04",
    Trustee -> "05",
    BoardMember -> "06",
    AccountantAgent -> "07",
    Representative -> "08",
    AuthorisedEmployee -> "09",
    Other -> "10",
    HmrcOfficer -> "11"
  )

  val inverseStati: Map[String, RoleInTheBusiness] = stati.map(_.swap)

  def fromString(value: String): RoleInTheBusiness = inverseStati(value)
  def toJsString(value: RoleInTheBusiness): JsString = JsString(stati(value))

  implicit val format: Format[RoleInTheBusiness] = Format[RoleInTheBusiness](
    Reads[RoleInTheBusiness] { json => json.validate[String] map fromString },
    Writes[RoleInTheBusiness] (toJsString)
  )

}
