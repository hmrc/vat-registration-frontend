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

package models

import java.time.LocalDate

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class IncorporationDetails(companyNumber: String,
                                companyName: String,
                                ctutr: String,
                                dateOfIncorporation: LocalDate)

object IncorporationDetails {
  val apiFormat: Format[IncorporationDetails] = (
    (__ \ "companyProfile" \ "companyNumber").format[String] and
    (__ \ "companyProfile" \ "companyName").format[String] and
    (__ \ "ctutr").format[String] and
    (__ \ "companyProfile" \ "dateOfIncorporation").format[LocalDate]
  )(IncorporationDetails.apply, unlift(IncorporationDetails.unapply))

  implicit val format: Format[IncorporationDetails] = Json.format[IncorporationDetails]
}