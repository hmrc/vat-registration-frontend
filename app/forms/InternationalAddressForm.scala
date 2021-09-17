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

package forms

import connectors.ConfigConnector
import models.api
import models.api.{Address, Country}
import play.api.data.Forms.{mapping, of, optional, text}
import play.api.data.format.Formatter
import play.api.data.validation.Constraints.{maxLength, pattern}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.data.{Form, FormError}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.mappers.StopOnFirstFail

import javax.inject.{Inject, Singleton}

@Singleton
class InternationalAddressForm @Inject()(configConnector: ConfigConnector) {
  import InternationalAddressForm._

  // scalastyle:off
  def form(invalidCountries: Seq[String] = Seq.empty): Form[Address] = Form[Address] (
    mapping(
      line1Key -> of[String](stringFormat).verifying(
        StopOnFirstFail(
          maxLength(maxLineLength, "internationalAddress.error.line1.length"),
          pattern(lineRegex, line1Key, "internationalAddress.error.line1.invalid")
        )
      ),
      line2Key -> optional(text.verifying(
        StopOnFirstFail(
          maxLength(maxLineLength, "internationalAddress.error.line2.length"),
          pattern(lineRegex, line2Key, "internationalAddress.error.line2.invalid")
        )
      )),
      line3Key -> optional(text.verifying(
        StopOnFirstFail(
          maxLength(maxLineLength, "internationalAddress.error.line3.length"),
          pattern(lineRegex, line3Key, "internationalAddress.error.line3.invalid")
        )
      )),
      line4Key -> optional(text.verifying(
        StopOnFirstFail(
          maxLength(maxLineLength, "internationalAddress.error.line4.length"),
          pattern(lineRegex, line4Key, "internationalAddress.error.line4.invalid")
        )
      )),
      line5Key -> optional(text.verifying(
        StopOnFirstFail(
          maxLength(maxLineLength, "internationalAddress.error.line5.length"),
          pattern(lineRegex, line5Key, "internationalAddress.error.line5.invalid")
        )
      )),
      postcodeKey -> optional(text.verifying(
        StopOnFirstFail(
          maxLength(maxPostcodeLength, "internationalAddress.error.postcode.length"),
          pattern(postcodeRegex, postcodeKey, "internationalAddress.error.postcode.invalid")
        )
      )),
      countryKey -> text
        .verifying(validCountry(invalidCountries))
        .transform[Option[Country]](
          countryName => configConnector.countries
            .diff(invalidCountries)
            .find(_.name
              .map(_.toLowerCase)
              .contains(countryName.toLowerCase)
            ),
          country => country.flatMap(_.name)
            .getOrElse(throw new InternalServerException("Country missing from international address"))
        )
    )(Address.apply(_, _, _, _, _, _, _))(address => Address.unapply(address).map {
      case (l1, optL2, optL3, optL4, optL5, optPostcode, optCountry, _) =>
        (l1, optL2, optL3, optL4, optL5, optPostcode, optCountry)
    })
  )

  val stringFormat: Formatter[String] = new Formatter[String] {
    def bind(key: String, data: Map[String, String]) =
      data.get(key).toRight(Seq(FormError(key, "internationalAddress.error.line1.empty", Nil)))

    def unbind(key: String, value: String) =
      Map(key -> value)
  }

  def validCountry(invalidCountries: Seq[String]): Constraint[String] = Constraint[String] { countryName: String =>
    val lowerCaseCountries = configConnector.countries
      .flatMap(_.name)
      .diff(invalidCountries)
      .map(_.toLowerCase())

    if (lowerCaseCountries.contains(countryName.toLowerCase)) {
      Valid
    } else {
      Invalid("internationalAddress.error.country.invalid")
    }
  }

}

object InternationalAddressForm {
  val line1Key = "line1"
  val line2Key = "line2"
  val line3Key = "line3"
  val line4Key = "line4"
  val line5Key = "line5"
  val postcodeKey = "postcode"
  val countryKey = "country"

  val minLineLength = 1
  val maxLineLength = 35
  val maxPostcodeLength = 10

  val postcodeRegex = "^[A-Za-z0-9 \\-,.&'\\\\/()!]{1,10}$".r
  val lineRegex = "^[A-Za-z0-9 \\-,.&'\\\\/()!]{1,35}$".r
}