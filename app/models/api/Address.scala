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

package models.api

import cats.Show.show
import models.view.HomeAddressView
import models.api.Address.inlineShow.inline
import models.view.vatContact.ppob.PpobView
import models.{ApiModelTransformer => MT}
import org.apache.commons.lang3.text.WordUtils
import play.api.libs.json._

case class Country(code: Option[String], name: Option[String])

object Country {
  implicit val format: Format[Country] = Json.format[Country]
}

case class Address(line1: String,
                   line2: String,
                   line3: Option[String] = None,
                   line4: Option[String] = None,
                   postcode: Option[String] = None,
                   country: Option[Country] = None,
                   addressValidated: Boolean) {

  val id: String = Seq(Some(line1), if (postcode.isDefined) postcode else country).flatten.mkString.replaceAll(" ", "")

  val asLabel: String = inline show this

  /***
    * trims spaces and converts any Some("  ") to None
    */
  def normalise(): Address =
    Address(
      this.line1.trim,
      this.line2.trim,
      this.line3 map (_.trim) filterNot (_.isEmpty),
      this.line4 map (_.trim) filterNot (_.isEmpty),
      this.postcode map (_.trim) filterNot (_.isEmpty),
      this.country.flatMap { country =>
        val optCode = country.code.map(_.trim).filterNot(_.isEmpty)
        val optName = country.name.map(_.trim).filterNot(_.isEmpty)

        (optCode, optName) match {
          case (None, None) =>
            None
          case _ =>
            Some(Country(
              code = country.code.map(_.trim).filterNot(_.isEmpty),
              name = country.name.map(_.trim).filterNot(_.isEmpty)
            ))
          }
      },
      this.addressValidated
    )
}

object Address {

  private sealed trait AddressLineOrPostcode

  private final case class AddressLine(line: String) extends AddressLineOrPostcode

  private final case class Postcode(postcode: String) extends AddressLineOrPostcode

  implicit val format: OFormat[Address] = Json.format[Address]

  object addressLookupReads extends Reads[Address] {
    def reads(json: JsValue): JsResult[Address] = {
      val address = (json \ "address").as[JsObject]
      val postcode = (address \ "postcode").asOpt[String]
      val lineMap = (address \ "lines").as[List[String]].zipWithIndex.map(_.swap).toMap
      val country = (address \ "country").asOpt[Country]
      val addressValidated = (json \ "id").isDefined

      if (postcode.isEmpty && country.isEmpty) {
        JsError(JsonValidationError("neither postcode nor country were defined"))
      } else if (lineMap.size < 2) {
        JsError(JsonValidationError(s"only ${lineMap.size} lines provided from address-lookup-frontend"))
      } else {
        JsSuccess(Address(lineMap(0), lineMap(1), lineMap.get(2), lineMap.get(3), postcode, country, addressValidated))
      }
    }
  }

  def normalisedSeq(address: Address): Seq[String] = {
    Seq[Option[AddressLineOrPostcode]](
      Option(AddressLine(address.line1)),
      Option(AddressLine(address.line2)),
      address.line3.map(AddressLine),
      address.line4.map(AddressLine),
      address.postcode.map(Postcode),
      address.country.flatMap(_.name).map(AddressLine)
    ).collect {
      case Some(AddressLine(line))  => WordUtils.capitalizeFully(line)
      case Some(Postcode(postcode)) => postcode.toUpperCase()
    }
  }

  object htmlShow {
    implicit val html = (a: Address) => normalisedSeq(a)
  }

  object inlineShow {
    implicit val inline = show((a: Address) => normalisedSeq(a).mkString(", "))
  }

  implicit def modelTransformerApplicantHomeAddressView(implicit t: MT[HomeAddressView]): MT[Address] =
    MT((vatScheme: VatScheme) => t.toViewModel(vatScheme).flatMap(_.address))

  implicit def modelTransformerPpobView(implicit t: MT[PpobView]): MT[Address] =
    MT((vatScheme: VatScheme) => t.toViewModel(vatScheme).flatMap(_.address))
}
