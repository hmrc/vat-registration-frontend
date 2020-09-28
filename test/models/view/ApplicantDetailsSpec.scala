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
import play.api.libs.json.Json
import testHelpers.VatRegSpec

class ApplicantDetailsSpec extends VatRegSpec {
  val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
  val previousAddress = ScrsAddress(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"))

  "fromJsonToName" should {
    "return a correct full name with full json" in {
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

      ApplicantDetails.fromJsonToName(json) mustBe Name(first = Some("First"), middle = Some("Middle"), last = "Last")
    }

    "return a correct name with min json" in {
      val json = Json.parse(
        s"""
           |{
           |  "name": {
           |    "last": "Last"
           |  },
           |  "role": "Director",
           |  "dob": "1998-07-12",
           |  "nino": "AA123456Z"
           |}
         """.stripMargin)

      ApplicantDetails.fromJsonToName(json) mustBe Name(first = None, middle = None, last = "Last")
    }
  }

  "fromApi" should {
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

      ApplicantDetails.fromApi(json) mustBe applicantDetails
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

      ApplicantDetails.fromApi(json) mustBe applicantDetails
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

      ApplicantDetails.fromApi(json) mustBe applicantDetails
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

      ApplicantDetails.fromApi(json) mustBe applicantDetails
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
           |  "role": "secretary",
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
           |  "name": {
           |    "first": "fakeName",
           |    "last": "fakeSurname"
           |  },
           |  "dateOfBirth": "2020-01-01",
           |  "nino": "AB123456C",
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
           |  "role": "secretary",
           |  "contact": {
           |    "mobile": "5678",
           |    "tel": "1234",
           |    "email": "test@t.test"
           |  },
           |  "name": {
           |    "first": "fakeName",
           |    "last": "fakeSurname"
           |  },
           |  "dateOfBirth": "2020-01-01",
           |  "nino": "AB123456C",
           |  "currentAddress": {
           |    "line1": "TestLine1",
           |    "line2": "TestLine2",
           |    "postcode": "TE 1ST"
           |  }
           |}""".stripMargin)

      Json.toJson(data)(ApplicantDetails.apiWrites) mustBe validJson
    }

    "throw an IllegalStateException when trying to convert to Json with missing Applicant current address view data" in {
      val data = ApplicantDetails(
        homeAddress = None,
        contactDetails = Some(ContactDetailsView(Some("1234"), Some("test@t.test"), Some("5678"))),
        formerName = Some(FormerNameView(yesNo = true, Some("New Name Cosmo"))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(yesNo = false, Some(previousAddress)))
      )

      an[IllegalStateException] mustBe thrownBy(Json.toJson(data)(ApplicantDetails.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Applicant current address data" in {
      val data = ApplicantDetails(
        homeAddress = Some(HomeAddressView(currentAddress.id, None)),
        contactDetails = Some(ContactDetailsView(Some("1234"), Some("test@t.test"), Some("5678"))),
        formerName = Some(FormerNameView(yesNo = true, Some("New Name Cosmo"))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(yesNo = false, Some(previousAddress)))
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(ApplicantDetails.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Applicant contact data" in {
      val data = ApplicantDetails(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = None,
        formerName = Some(FormerNameView(yesNo = true, Some("New Name Cosmo"))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(yesNo = false, Some(previousAddress)))
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(ApplicantDetails.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Applicant former name view data" in {
      val data = ApplicantDetails(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("1234"), Some("test@t.test"), Some("5678"))),
        formerName = None,
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(yesNo = false, Some(previousAddress)))
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(ApplicantDetails.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Applicant former name data" in {
      val data = ApplicantDetails(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("1234"), Some("test@t.test"), Some("5678"))),
        formerName = Some(FormerNameView(yesNo = true, None)),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(yesNo = false, Some(previousAddress)))
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(ApplicantDetails.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Applicant former name change date" in {
      val data = ApplicantDetails(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("1234"), Some("test@t.test"), Some("5678"))),
        formerName = Some(FormerNameView(yesNo = true, Some("New Name Cosmo"))),
        formerNameDate = None,
        previousAddress = Some(PreviousAddressView(yesNo = false, Some(previousAddress)))
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(ApplicantDetails.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Applicant previous address view data" in {
      val data = ApplicantDetails(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("1234"), Some("test@t.test"), Some("5678"))),
        formerName = Some(FormerNameView(yesNo = true, Some("New Name Cosmo"))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = None
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(ApplicantDetails.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Applicant previous address data" in {
      val data = ApplicantDetails(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("1234"), Some("test@t.test"), Some("5678"))),
        formerName = Some(FormerNameView(yesNo = true, Some("New Name Cosmo"))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(yesNo = false, None))
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(ApplicantDetails.apiWrites))
    }
  }
}
