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

package models.view

import java.time.LocalDate

import models.api.ScrsAddress
import models.external.Name
import play.api.libs.json.{JsSuccess, Json}
import testHelpers.VatRegSpec

class ApplicantDetailsSpec extends VatRegSpec {
  val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
  val previousAddress = ScrsAddress(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"))

  "apiReads" should {
    "return a correct partial ApplicantDetails view model with full name" in {
      val json = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "middle": "Middle",
           |    "last": "Last"
           |  },
           |  "role": "Director",
           |  "dob": "1998-07-12",
           |  "nino": "AA123456Z"
           |}
         """.stripMargin)

      val applicantDetails = ApplicantDetails(

        homeAddress = None,
        contactDetails = None,
        formerName = Some(FormerNameView(false, None)),
        formerNameDate = None,
        previousAddress = Some(PreviousAddressView(true, None))
      )

      ApplicantDetails.apiReads.reads(json) mustBe JsSuccess(applicantDetails)
    }

    "return a correct partial ApplicantDetails view model with partial name" in {
      val json = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "last": "Last"
           |  },
           |  "role": "Director",
           |  "nino": "AA123456Z"
           |}
         """.stripMargin)

      val applicantDetails = ApplicantDetails(
        homeAddress = None,
        contactDetails = None,
        formerName = Some(FormerNameView(false, None)),
        formerNameDate = None,
        previousAddress = Some(PreviousAddressView(true, None))
      )

      ApplicantDetails.apiReads.reads(json) mustBe JsSuccess(applicantDetails)
    }

    "return a correct full ApplicantDetails view model with max data" in {
      val json = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "last": "Last"
           |  },
           |  "role": "Director",
           |  "dob": "1998-07-12",
           |  "nino": "AA123456Z",
           |  "currentAddress": {
           |    "line1": "TestLine1",
           |    "line2": "TestLine2",
           |    "postcode": "TE 1ST"
           |  },
           |  "contact": {
           |    "email": "test@t.test",
           |    "tel": "1234",
           |    "mobile": "5678"
           |  },
           |  "changeOfName": {
           |    "name": {
           |      "first": "New",
           |      "middle": "Name",
           |      "last": "Cosmo"
           |    },
           |    "change": "2000-07-12"
           |  },
           |  "previousAddress": {
           |    "line1": "TestLine11",
           |    "line2": "TestLine22",
           |    "postcode": "TE1 1ST"
           |  }
           |}
         """.stripMargin)

      val formerName = Name(first = Some("New"), middle = Some("Name"), last = "Cosmo")

      val applicantDetails = ApplicantDetails(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("1234"), Some("test@t.test"), Some("5678"))),
        formerName = Some(FormerNameView(yesNo = true, Some(formerName.asLabel))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(yesNo = false, Some(previousAddress)))
      )

      ApplicantDetails.apiReads.reads(json) mustBe JsSuccess(applicantDetails)
    }

    "return a correct full ApplicantDetails view model with min data" in {
      val json = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "last": "Last"
           |  },
           |  "role": "Director",
           |  "dob": "1998-07-12",
           |  "nino": "AA123456Z",
           |  "currentAddress": {
           |    "line1": "TestLine1",
           |    "line2": "TestLine2",
           |    "postcode": "TE 1ST"
           |  },
           |  "contact": {
           |    "email": "test@t.test",
           |    "tel": "1234",
           |    "mobile": "5678"
           |  }
           |}
         """.stripMargin)

      val applicantDetails = ApplicantDetails(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("1234"), Some("test@t.test"), Some("5678"))),
        formerName = Some(FormerNameView(yesNo = false, None)),
        formerNameDate = None,
        previousAddress = Some(PreviousAddressView(yesNo = true, None))
      )

      ApplicantDetails.apiReads.reads(json) mustBe JsSuccess(applicantDetails)
    }
  }

  "Calling apiWrites" should {
    "return a correct partial JsValue with data" in {
      val data = ApplicantDetails(
        homeAddress = None,
        contactDetails = None,
        formerName = None,
        formerNameDate = None,
        previousAddress = None
      )

      val validJson = Json.parse(
        s"""
           |{
           |}""".stripMargin)

      Json.toJson(data)(ApplicantDetails.apiWrites) mustBe validJson
    }

    "return a correct full JsValue with maximum data" in {
      val data = ApplicantDetails(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("1234"), Some("test@t.test"), Some("5678"))),
        formerName = Some(FormerNameView(yesNo = true, Some("New Name Cosmo"))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(yesNo = false, Some(previousAddress)))
      )

      val validJson = Json.parse(
        s"""
           |{
           |  "changeOfName": {
           |    "change": "2000-07-12",
           |    "name": {
           |      "middle": "Name",
           |      "last": "Cosmo",
           |      "first": "New"
           |    }
           |  },
           |  "previousAddress": {
           |    "line1": "TestLine11",
           |    "line2": "TestLine22",
           |    "postcode": "TE1 1ST"
           |  },
           |  "contact": {
           |    "mobile": "5678",
           |    "tel": "1234",
           |    "email": "test@t.test"
           |  },
           |  "currentAddress": {
           |    "line1": "TestLine1",
           |    "line2": "TestLine2",
           |    "postcode": "TE 1ST"
           |  }
           |}""".stripMargin)

      Json.toJson(data)(ApplicantDetails.apiWrites) mustBe validJson
    }

    "return a correct full JsValue with minimum data" in {
      val data = ApplicantDetails(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("1234"), Some("test@t.test"), Some("5678"))),
        formerName = Some(FormerNameView(yesNo = false, None)),
        formerNameDate = None,
        previousAddress = Some(PreviousAddressView(yesNo = true, None))
      )

      val validJson = Json.parse(
        s"""
           |{
           |  "contact": {
           |    "mobile": "5678",
           |    "tel": "1234",
           |    "email": "test@t.test"
           |  },
           |  "currentAddress": {
           |    "line1": "TestLine1",
           |    "line2": "TestLine2",
           |    "postcode": "TE 1ST"
           |  }
           |}""".stripMargin)

      Json.toJson(data)(ApplicantDetails.apiWrites) mustBe validJson
    }
  }

}
