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
sealed trait DeclarationCapacity

case object OwnerProprietor extends DeclarationCapacity with RoleInTheBusiness
case object Partner extends DeclarationCapacity with RoleInTheBusiness
case object Director extends DeclarationCapacity with RoleInTheBusiness
case object CompanySecretary extends DeclarationCapacity with RoleInTheBusiness
case object Trustee extends DeclarationCapacity with RoleInTheBusiness
case object BoardMember extends DeclarationCapacity with RoleInTheBusiness
case object AccountantAgent extends DeclarationCapacity
case object Representative extends DeclarationCapacity
case object AuthorisedEmployee extends DeclarationCapacity
case object Other extends DeclarationCapacity with RoleInTheBusiness
case object HmrcOfficer extends DeclarationCapacity
case object FinancialController extends RoleInTheBusiness

object RoleInTheBusiness {

  val stati: Map[RoleInTheBusiness, String] = Map[RoleInTheBusiness, String] (
    OwnerProprietor -> "01",
    Partner -> "02",
    Director -> "03",
    CompanySecretary -> "04",
    BoardMember -> "05",
    Trustee -> "06",
    FinancialController -> "07",
    Other -> "08"
  )

  val inverseStati: Map[String, RoleInTheBusiness] = stati.map(_.swap)

  def fromString(value: String): RoleInTheBusiness = inverseStati(value)
  def toJsString(value: RoleInTheBusiness): JsString = JsString(stati(value))

  implicit val format: Format[RoleInTheBusiness] = Format[RoleInTheBusiness](
    Reads[RoleInTheBusiness] { json => json.validate[String] map fromString },
    Writes[RoleInTheBusiness] (toJsString)
  )
}

object DeclarationCapacity {

  val stati: Map[DeclarationCapacity, String] = Map[DeclarationCapacity, String] (
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

  val inverseStati: Map[String, DeclarationCapacity] = stati.map(_.swap)

  def fromString(value: String): DeclarationCapacity = inverseStati(value)
  def toJsString(value: DeclarationCapacity): JsString = JsString(stati(value))

  implicit val format: Format[DeclarationCapacity] = Format[DeclarationCapacity](
    Reads[DeclarationCapacity] { json => json.validate[String] map fromString },
    Writes[DeclarationCapacity] (toJsString)
  )

}

