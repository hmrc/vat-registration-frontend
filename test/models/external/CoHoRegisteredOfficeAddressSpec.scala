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

package models.external

import models.api.ScrsAddress
import play.api.libs.json.{JsSuccess, JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec

class CoHoRegisteredOfficeAddressSpec extends UnitSpec {

  val reader = CoHoRegisteredOfficeAddress.formatModel

  val json: JsValue = Json.parse(
    """{
        |"registered_office_address":{
          |"premises":"premises",
          |"address_line_1":"address_line_1",
          |"address_line_2":"address_line_2",
          |"locality":"locality",
          |"country":"country",
          |"po_box":"po_box",
          |"postal_code":"postal_code",
          |"region":"region"
        |}
      |}""".stripMargin)

  val coHoRegisteredOfficeAddress =
    CoHoRegisteredOfficeAddress("premises",
      "address_line_1",
      Some("address_line_2"),
      "locality",
      Some("country"),
      Some("po_box"),
      Some("postal_code"),
      Some("region"))

  val coHoRegisteredOfficeAddress2 =
    CoHoRegisteredOfficeAddress("premises",
      "address_line_1",
      None,
      "locality",
      None, None, Some("postal_code"), None)

  val scsrAddress = ScrsAddress("premises address_line_1", "address_line_2 po_box", Some("locality"),Some("region"),Some("postal_code"),Some("country"))

  "CoHoRegisteredOfficeAddress" should {
    "reads should return coHoCompanyProfile" in {
      reader.reads(json) shouldBe JsSuccess(coHoRegisteredOfficeAddress)
    }
  }

  "convertToAddress" should {
    "convert a complete CoHoRegisteredOfficeAddress to a ScsrAddress" in {
      CoHoRegisteredOfficeAddress.convertToAddress(coHoRegisteredOfficeAddress) shouldBe scsrAddress
    }
    "convert a partial CoHoRegisteredOfficeAddress to a ScsrAddress" in {
      val partial = ScrsAddress(line1 = "premises address_line_1",line2 = "locality", postcode = Some("postal_code"))
      CoHoRegisteredOfficeAddress.convertToAddress(coHoRegisteredOfficeAddress2) shouldBe partial
    }
  }
}
