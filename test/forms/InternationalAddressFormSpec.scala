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

package forms

import fixtures.VatRegistrationFixture
import models.api.{Address, Country}
import org.mockito.Mockito._
import testHelpers.VatRegSpec


class InternationalAddressFormSpec extends VatRegSpec with VatRegistrationFixture {

  val form = new InternationalAddressForm(mockConfigConnector).form()

  "The International Address form" must {
    "bind successfully" when {
      "only the mandatory fields are provided and valid" in {
        when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

        val res = form.bind(Map(
          "line1" -> testLine1,
          "country" -> "Norway"
        )).value

        res mustBe Some(Address(testLine1, country = Some(Country(Some("NO"), Some("Norway")))))
      }
      "all fields are provided and valid" in {
        when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

        val res = form.bind(Map(
          "line1" -> testLine1,
          "line2" -> testLine2,
          "line3" -> testLine3,
          "line4" -> testLine4,
          "line5" -> testLine5,
          "postcode" -> testPostcode,
          "country" -> "Norway"
        )).value

        res mustBe Some(Address(
          line1 = testLine1,
          line2 = Some(testLine2),
          line3 = Some(testLine3),
          line4 = Some(testLine4),
          line5 = Some(testLine5),
          postcode = Some(testPostcode),
          country = Some(Country(Some("NO"), Some("Norway")))))
      }
    }
    "not bind" when {
      "the mandatory fields are populated" when {
        "line 1 isn't provided" in {
          when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

          val res = form.bind(Map(
            "country" -> "Norway"
          ))

          res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.line1.empty")
        }
        "line 1 is too long" in {
          when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

          val res = form.bind(Map(
            "line1" -> "w" * 36,
            "country" -> "Norway"
          ))

          res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.line1.length")
        }
        "line 1 contains invalid characters" in {
          when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

          val invalidCharacters = Seq("#", "$", "%", "^", ";", ":", "*")

          invalidCharacters.foreach { character =>
            val res = form.bind(Map(
              "line1" -> character,
              "country" -> "Norway"
            ))

            res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.line1.invalid")
          }
        }
        "country isn't on the list of allowed countries" in {
          when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("FR"), Some("France"))))

          val res = form.bind(Map(
            "line1" -> testLine1,
            "country" -> "Norway"
          ))

          res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.country.invalid")
        }
        "country is on a list of additional disallowed countries that has been passed into the form" in {
          val formWithInvalidCountries = new InternationalAddressForm(mockConfigConnector).form(Seq("Norway"))

          when(mockConfigConnector.countries).thenReturn(Seq(
            Country(Some("FR"), Some("France")),
            Country(Some("NO"), Some("Norway"))
          ))

          val res = formWithInvalidCountries.bind(Map(
            "line1" -> testLine1,
            "country" -> "Norway"
          ))

          res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.country.invalid")
        }
      }
      "all fields are populated" when {
        "line 2 is too long" in {
          when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

          val res = form.bind(Map(
            "line1" -> testLine1,
            "line2" -> "w" * 36,
            "country" -> "Norway"
          ))

          res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.line2.length")
        }
        "line 2 contains invalid characters" in {
          when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

          val invalidCharacters = Seq("#", "$", "%", "^", ";", ":", "*")

          invalidCharacters.foreach { character =>
            val res = form.bind(Map(
              "line1" -> testLine1,
              "line2" -> character,
              "country" -> "Norway"
            ))

            res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.line2.invalid")
          }
        }
        "line 3 is too long" in {
          when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

          val res = form.bind(Map(
            "line1" -> testLine1,
            "line2" -> testLine2,
            "line3" -> "w" * 36,
            "country" -> "Norway"
          ))

          res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.line3.length")
        }
        "line 3 contains invalid characters" in {
          when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

          val invalidCharacters = Seq("#", "$", "%", "^", ";", ":", "*")

          invalidCharacters.foreach { character =>
            val res = form.bind(Map(
              "line1" -> testLine1,
              "line2" -> testLine2,
              "line3" -> character,
              "country" -> "Norway"
            ))

            res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.line3.invalid")
          }
        }
        "line 4 is too long" in {
          when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

          val res = form.bind(Map(
            "line1" -> testLine1,
            "line2" -> testLine2,
            "line4" -> testLine3,
            "line4" -> "w" * 36,
            "country" -> "Norway"
          ))

          res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.line4.length")
        }
        "line 4 contains invalid characters" in {
          when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

          val invalidCharacters = Seq("#", "$", "%", "^", ";", ":", "*")

          invalidCharacters.foreach { character =>
            val res = form.bind(Map(
              "line1" -> testLine1,
              "line2" -> testLine2,
              "line3" -> testLine3,
              "line4" -> character,
              "country" -> "Norway"
            ))

            res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.line4.invalid")
          }
        }
        "line 5 is too long" in {
          when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

          val res = form.bind(Map(
            "line1" -> testLine1,
            "line2" -> testLine2,
            "line3" -> testLine3,
            "line4" -> testLine4,
            "line5" -> "w" * 36,
            "country" -> "Norway"
          ))

          res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.line5.length")
        }
        "line 5 contains invalid characters" in {
          when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

          val invalidCharacters = Seq("#", "$", "%", "^", ";", ":", "*")

          invalidCharacters.foreach { character =>
            val res = form.bind(Map(
              "line1" -> testLine1,
              "line2" -> testLine2,
              "line3" -> testLine3,
              "line4" -> testLine4,
              "line5" -> character,
              "country" -> "Norway"
            ))

            res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.line5.invalid")
          }
        }
        "postcode contains invalid characters" in {
          when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

          val invalidCharacters = Seq("#", "$", "%", "^", ";", ":", "*")

          invalidCharacters.foreach { character =>
            val res = form.bind(Map(
              "line1" -> testLine1,
              "line2" -> testLine2,
              "line3" -> testLine3,
              "line4" -> testLine4,
              "line5" -> testLine5,
              "postcode" -> character,
              "country" -> "Norway"
            ))

            res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.postcode.invalid")
          }
        }
        "postcode is too long" in {
          when(mockConfigConnector.countries).thenReturn(Seq(Country(Some("NO"), Some("Norway"))))

          val res = form.bind(Map(
            "line1" -> testLine1,
            "line2" -> testLine2,
            "line3" -> testLine3,
            "line4" -> testLine4,
            "line5" -> testLine5,
            "postcode" -> "w" * 11,
            "country" -> "Norway"
          ))

          res.errors.headOption.map(_.message) mustBe Some("internationalAddress.error.postcode.length")
        }
      }
    }
  }

}
