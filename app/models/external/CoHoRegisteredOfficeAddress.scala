/*
 * Copyright 2019 HM Revenue & Customs
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

package models.external

import models.api.ScrsAddress
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, __}

case class CoHoRegisteredOfficeAddress(
                                        premises: String,
                                        addressLine1: String,
                                        addressLine2: Option[String] = None,
                                        locality: String,
                                        country: Option[String] = None,
                                        poBox: Option[String] = None,
                                        postalCode: Option[String] = None,
                                        region: Option[String] = None
                                      )

object CoHoRegisteredOfficeAddress {

  private val roAddress = __ \ "registered_office_address"

  implicit val formatModel: Reads[CoHoRegisteredOfficeAddress] = (
    (roAddress \ "premises").read[String] and
      (roAddress \ "address_line_1").read[String] and
      (roAddress \ "address_line_2").readNullable[String] and
      (roAddress \ "locality").read[String] and
      (roAddress \ "country").readNullable[String] and
      (roAddress \ "po_box").readNullable[String] and
      (roAddress \ "postal_code").readNullable[String] and
      (roAddress \ "region").readNullable[String]
    ) (CoHoRegisteredOfficeAddress.apply _)

  import scala.language.implicitConversions

  implicit def convertToAddress(address: CoHoRegisteredOfficeAddress): ScrsAddress = {
    val (line1, oLine2) = if ((address.premises + " " + address.addressLine1).length > 26) {
      (address.premises, Some(address.addressLine1))
    } else {
      (address.premises + " " + address.addressLine1, None)
    }

    val addrLine2POBox: Option[String] = Seq(address.addressLine2, address.poBox).flatten.foldLeft("")(_ + " " + _).trim match {
      case "" => None
      case str => Some(str)
    }

    val additionalLines: Seq[String] = Seq(oLine2, addrLine2POBox, Some(address.locality), address.region).flatten

    ScrsAddress(
      line1,
      additionalLines.head, {
        if (additionalLines.length > 1) Some(additionalLines(1)) else None
      }, {
        if (additionalLines.length > 2) Some(additionalLines(2)) else None
      },
      address.postalCode,
      address.country
    ).normalise()
  }
}
