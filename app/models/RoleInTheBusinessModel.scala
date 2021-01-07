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

case object Director extends RoleInTheBusiness

case object CompanySecretary extends RoleInTheBusiness

object RoleInTheBusiness {
  val director = "03"
  val companySecretary = "04"

  implicit val reads: Reads[RoleInTheBusiness] = Reads[RoleInTheBusiness] {
    case JsString(`director`) => JsSuccess(Director)
    case JsString(`companySecretary`) => JsSuccess(CompanySecretary)
    case _ => JsError("Could not parse role in the business")
  }

  implicit val writes: Writes[RoleInTheBusiness] = Writes[RoleInTheBusiness] {
    case Director => JsString(director)
    case CompanySecretary => JsString(companySecretary)
  }

  implicit val format: Format[RoleInTheBusiness] = Format(reads, writes)

}