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

package models.api

import cats.Show.show
import models.view.HomeAddressView
import models.api.ScrsAddress.inlineShow.inline
import models.view.vatContact.ppob.PpobView
import models.{ApiModelTransformer => MT}
import org.apache.commons.lang3.text.WordUtils
import play.api.libs.json._

case class ScrsAddress(line1: String,
                       line2: String,
                       line3: Option[String] = None,
                       line4: Option[String] = None,
                       postcode: Option[String] = None,
                       country: Option[String] = None) {

  val id: String = Seq(Some(line1), if (postcode.isDefined) postcode else country).flatten.mkString.replaceAll(" ", "")

  val asLabel: String = inline show this

  override def equals(obj: Any): Boolean = obj match {
    case ScrsAddress(objLine1, _, _, _, Some(objPostcode), _)
      => line1.equalsIgnoreCase(objLine1) && postcode.getOrElse("").equalsIgnoreCase(objPostcode)
    case ScrsAddress(objLine1, _, _, _, _, Some(objCountry))
      => line1.equalsIgnoreCase(objLine1) && country.getOrElse("").equalsIgnoreCase(objCountry)

    case _ => false
  }

  override def hashCode: Int = 1 // TODO temporary fix to ensure List.distinct works

  /***
    * trims spaces and converts any Some("  ") to None
    */
  def normalise(): ScrsAddress =
    ScrsAddress(
      this.line1.trim,
      this.line2.trim,
      this.line3 map (_.trim) filterNot (_.isEmpty),
      this.line4 map (_.trim) filterNot (_.isEmpty),
      this.postcode map (_.trim) filterNot (_.isEmpty),
      this.country map (_.trim) filterNot (_.isEmpty))
}

object ScrsAddress {

  private sealed trait AddressLineOrPostcode

  private final case class AddressLine(line: String) extends AddressLineOrPostcode

  private final case class Postcode(postcode: String) extends AddressLineOrPostcode

  implicit val format: OFormat[ScrsAddress] = Json.format[ScrsAddress]

  object adressLookupReads extends Reads[ScrsAddress] {
    def reads(json: JsValue): JsResult[ScrsAddress] = {
      val address = (json \ "address").as[JsObject]
      val postcode = (address \ "postcode").asOpt[String]
      val lineMap = (address \ "lines").as[List[String]].zipWithIndex.map(_.swap).toMap
      val countryCode = (address \ "country" \ "code").asOpt[String]
      val countryName = (address \ "country" \ "name").asOpt[String]
      if (postcode.isEmpty && countryCode.isEmpty) {
        JsError(JsonValidationError("neither postcode nor country were defined"))
      } else if (lineMap.size < 2) {
        JsError(JsonValidationError(s"only ${lineMap.size} lines provided from address-lookup-frontend"))
      } else {
        JsSuccess(ScrsAddress(lineMap(0), lineMap(1), lineMap.get(2), lineMap.get(3), postcode, countryName.fold(countryCode)(_ => countryName)))
      }
    }
  }

  def normalisedSeq(address: ScrsAddress): Seq[String] = {
    Seq[Option[AddressLineOrPostcode]](
      Option(AddressLine(address.line1)),
      Option(AddressLine(address.line2)),
      address.line3.map(AddressLine),
      address.line4.map(AddressLine),
      address.postcode.map(Postcode),
      address.country.map(AddressLine)
    ).collect {
      case Some(AddressLine(line))  => WordUtils.capitalizeFully(line)
      case Some(Postcode(postcode)) => postcode.toUpperCase()
    }
  }

  object htmlShow {
    implicit val html = (a: ScrsAddress) => normalisedSeq(a)
  }

  object inlineShow {
    implicit val inline = show((a: ScrsAddress) => normalisedSeq(a).mkString(", "))
  }

  implicit def modelTransformerOfficerHomeAddressView(implicit t: MT[HomeAddressView]): MT[ScrsAddress] =
    MT((vatScheme: VatScheme) => t.toViewModel(vatScheme).flatMap(_.address))

  implicit def modelTransformerPpobView(implicit t: MT[PpobView]): MT[ScrsAddress] =
    MT((vatScheme: VatScheme) => t.toViewModel(vatScheme).flatMap(_.address))
}
