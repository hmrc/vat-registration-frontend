/*
 * Copyright 2017 HM Revenue & Customs
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

import fixtures.VatRegistrationFixture
import play.api.libs.json.{JsSuccess, JsValue, Json, __}
import uk.gov.hmrc.play.test.UnitSpec

class OfficerListSpec extends UnitSpec with VatRegistrationFixture {

  val reader = OfficerList.reads

  val json: JsValue = Json.parse(
    """{
      |  "officers":[{
      |    "name_elements" : {
      |        "forename" : "Bob",
      |        "other_forenames" : "Bimbly Bobblous",
      |        "surname" : "Bobbings"
      |    },
      |    "date_of_birth" : {
      |        "day" : "12",
      |        "month" : "11",
      |        "year" : "1973"
      |    },
      |    "address" : {
      |        "premises" : "98",
      |        "address_line_1" : "LIMBRICK LANE",
      |        "address_line_2" : "GORING-BY-SEA",
      |        "locality" : "WORTHING",
      |        "country" : "United Kingdom",
      |        "postal_code" : "BN12 6AG"
      |    },
      |    "officer_role" : "director"
      | },
      | {
      |    "name_elements" : {
      |        "title" : "Mx",
      |        "forename" : "Jingly",
      |        "surname" : "Jingles"
      |    },
      |    "date_of_birth" : {
      |        "day" : "12",
      |        "month" : "07",
      |        "year" : "1988"
      |    },
      |    "address" : {
      |        "premises" : "713",
      |        "address_line_1" : "ST. JAMES GATE",
      |        "locality" : "NEWCASTLE UPON TYNE",
      |        "country" : "England",
      |        "postal_code" : "NE1 4BB"
      |    },
      |    "officer_role" : "secretary"
      |}]}""".stripMargin)

  "OfficerList" should {
    "deserialise from valid JSON" in {
      val officerList = OfficerList(Seq(
        officer,
        Officer(Name(Some("Jingly"), None, "Jingles", Some("Mx")), "secretary", Some(validDob), None, None)
      ))
      reader.reads(json) shouldBe JsSuccess(officerList, (__ \ "officers"))
    }
  }

  "Officer" should {
    "serialise to JSON" in {
      val bobJson = Json.parse(
        """{
          |    "name_elements" : {
          |        "forename" : "Bob",
          |        "other_forenames" : "Bimbly Bobblous",
          |        "surname" : "Bobbings"
          |    },
          |    "date_of_birth" : {
          |        "day" : 12,
          |        "month" : 11,
          |        "year" : 1973
          |    },
          |    "officer_role" : "director"
          |}""".stripMargin)

       Officer.wt.writes(officer) shouldBe bobJson
    }
  }

  "Officer" should {
    "have equality equal" in {
      val officer1 = Officer(name = Name(Some("forename"), Some("other names"), "surname"), role = "director", Some(validDob))
      val officer2 = Officer(name = Name(Some("forename"), Some("other names"), "surname"), role = "director", Some(validDob))

      (officer1 == officer2) shouldBe true
    }
  }

  "Officer" should {
    "have equality not-equal" in {
      val officer1 = Officer(name = Name(Some("forename"), Some("other names"), "surname"), role = "director", Some(validDob))
      val officer2 = Officer(name = Name(Some("forename"), None, "surname"), role = "director", Some(validDob))

      (officer1 == officer2) shouldBe false
    }
  }

}
