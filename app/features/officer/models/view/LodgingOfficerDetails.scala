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

package features.officer.models.view

import java.time.LocalDate

import models.api.ScrsAddress
import play.api.libs.json._

case class LodgingOfficerDetails(formerName: Option[String],
                                 nameChangeDate: Option[LocalDate],
                                 email: Option[String],
                                 tel: Option[String],
                                 mobile: Option[String],
                                 currentAddress: Option[ScrsAddress],
                                 previousAddress: Option[ScrsAddress])

object LodgingOfficerDetails {
  implicit val format: Format[LodgingOfficerDetails] = Json.format[LodgingOfficerDetails]

  val apiReads: Reads[LodgingOfficerDetails] = new Reads[LodgingOfficerDetails] {
    override def reads(json: JsValue) = {
      val formerNameBase = json.\("details").\("changeOfName").asOpt[JsObject]
      val contactBase    = json.\("details").\("contact").as[JsObject]

      val buildFormerName = formerNameBase.fold[Option[String]](None) { fn =>
        val first  = fn.\("name").\("first").asOpt[String].getOrElse("")
        val middle = fn.\("name").\("middle").asOpt[String].getOrElse("")
        val last   = fn.\("name").\("last").asOpt[String].getOrElse("")

        Some(s"$first $middle $last")
      }

      val buildFormerNameChangeDate = formerNameBase.fold[Option[LocalDate]](None)(_.\("change").asOpt[LocalDate])

      JsSuccess(LodgingOfficerDetails(
        formerName      = buildFormerName,
        nameChangeDate  = buildFormerNameChangeDate,
        email           = contactBase.\("email").asOpt[String],
        tel             = contactBase.\("tel").asOpt[String],
        mobile          = contactBase.\("mobile").asOpt[String],
        currentAddress  = json.\("details").\("currentAddress").asOpt[ScrsAddress],
        previousAddress = json.\("details").\("previousAddress").asOpt[ScrsAddress]
      ))
    }
  }

  val apiWrites: (LodgingOfficer, Boolean) => Writes[LodgingOfficerDetails] = (officer, ivStatus) => new Writes[LodgingOfficerDetails] {
    override def writes(o: LodgingOfficerDetails) = {

      Json.obj(
        "name" -> Json.obj(

        )
      )
    }
  }
}
