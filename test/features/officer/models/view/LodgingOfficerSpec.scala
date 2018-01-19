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
import models.external.{Name, Officer}
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class LodgingOfficerSpec extends UnitSpec {
  "fromApi" should {
    "return a correct partial LodgingOfficer view model with full name" in {
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

      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
        role = "Director"
      )

      val lodgingOfficer = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA123456Z")),
        homeAddress = None,
        contactDetails = None,
        formerName = None,
        formerNameDate = None,
        previousAddress = None
      )

      LodgingOfficer.fromApi(json) shouldBe lodgingOfficer
    }

    "return a correct partial LodgingOfficer view model with partial name" in {
      val json = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "last": "Last"
           |  },
           |  "role": "Director",
           |  "dob": "1998-07-12",
           |  "nino": "AA123456Z"
           |}
         """.stripMargin)


      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = None, surname = "Last"),
        role = "Director"
      )

      val lodgingOfficer = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA123456Z")),
        homeAddress = None,
        contactDetails = None,
        formerName = None,
        formerNameDate = None,
        previousAddress = None
      )

      LodgingOfficer.fromApi(json) shouldBe lodgingOfficer
    }

    "return a correct full LodgingOfficer view model with max data" in {
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
           |  "details": {
           |    "currentAddress": {
           |      "line1": "TestLine1",
           |      "line2": "TestLine2",
           |      "postcode": "TE 1ST"
           |    },
           |    "contact": {
           |      "email": "test@t.test",
           |      "tel": "1234",
           |      "mobile": "5678"
           |    },
           |    "changeOfName": {
           |      "name": {
           |        "first": "New",
           |        "middle": "Name",
           |        "last": "Cosmo"
           |      },
           |      "change": "2000-07-12"
           |    },
           |    "previousAddress": {
           |      "line1": "TestLine11",
           |      "line2": "TestLine22",
           |      "postcode": "TE1 1ST"
           |    }
           |  }
           |}
         """.stripMargin)


      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = None, surname = "Last"),
        role = "Director"
      )

      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
      val formerName = Name(forename = Some("New"), otherForenames = Some("Name"), surname = "Cosmo")
      val previousAddress = ScrsAddress(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"))

      val lodgingOfficer = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA123456Z")),
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
        formerName = Some(FormerNameView(true, Some(formerName.asLabel))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(false, Some(previousAddress)))
      )

      LodgingOfficer.fromApi(json) shouldBe lodgingOfficer
    }

    "return a correct full LodgingOfficer view model with min data" in {
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
           |  "details": {
           |    "currentAddress": {
           |      "line1": "TestLine1",
           |      "line2": "TestLine2",
           |      "postcode": "TE 1ST"
           |    },
           |    "contact": {
           |      "email": "test@t.test",
           |      "tel": "1234",
           |      "mobile": "5678"
           |    }
           |  }
           |}
         """.stripMargin)


      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = None, surname = "Last"),
        role = "Director"
      )

      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))

      val lodgingOfficer = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA123456Z")),
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
        formerName = Some(FormerNameView(false, None)),
        formerNameDate = None,
        previousAddress = Some(PreviousAddressView(true, None))
      )

      LodgingOfficer.fromApi(json) shouldBe lodgingOfficer
    }
  }

  "Calling apiWrites" should {
    "return a correct partial JsValue with maximum data" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
        role = "Director"
      )

      val data = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z")),
        homeAddress = None,
        contactDetails = None,
        formerName = None,
        formerNameDate = None,
        previousAddress = None
      )

      val validJson = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "middle": "Middle",
           |    "last": "Last"
           |  },
           |  "role": "Director",
           |  "dob": "1998-07-12",
           |  "nino": "AA112233Z"
           |}""".stripMargin)

      Json.toJson(data)(LodgingOfficer.apiWrites) shouldBe validJson
    }

    "return a correct partial JsValue with minimum data" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = None, surname = "Last"),
        role = "Director"
      )

      val data = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z")),
        homeAddress = None,
        contactDetails = None,
        formerName = None,
        formerNameDate = None,
        previousAddress = None
      )

      val validJson = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "last": "Last"
           |  },
           |  "role": "Director",
           |  "dob": "1998-07-12",
           |  "nino": "AA112233Z"
           |}""".stripMargin)

      Json.toJson(data)(LodgingOfficer.apiWrites) shouldBe validJson
    }

    "return a correct full JsValue with maximum data" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
        role = "Director"
      )

      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
      val prevAddress = ScrsAddress(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"))

      val data = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z")),
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
        formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(false, Some(prevAddress)))
      )

      val validJson = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "middle": "Middle",
           |    "last": "Last"
           |  },
           |  "role": "Director",
           |  "dob": "1998-07-12",
           |  "nino": "AA112233Z",
           |  "details": {
           |    "currentAddress": {
           |      "line1": "TestLine1",
           |      "line2": "TestLine2",
           |      "postcode": "TE 1ST"
           |    },
           |    "contact": {
           |      "email": "test@t.test",
           |      "tel": "1234",
           |      "mobile": "5678"
           |    },
           |    "changeOfName": {
           |      "name": {
           |        "first": "New",
           |        "middle": "Name",
           |        "last": "Cosmo"
           |      },
           |      "change": "2000-07-12"
           |    },
           |    "previousAddress": {
           |      "line1": "TestLine11",
           |      "line2": "TestLine22",
           |      "postcode": "TE1 1ST"
           |    }
           |  }
           |}""".stripMargin)

      Json.toJson(data)(LodgingOfficer.apiWrites) shouldBe validJson
    }

    "return a correct full JsValue with minimum data" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = None, surname = "Last"),
        role = "Director"
      )

      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))

      val data = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z")),
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
        formerName = Some(FormerNameView(false, None)),
        formerNameDate = None,
        previousAddress = Some(PreviousAddressView(true, None))
      )

      val validJson = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "last": "Last"
           |  },
           |  "role": "Director",
           |  "dob": "1998-07-12",
           |  "nino": "AA112233Z",
           |  "details": {
           |    "currentAddress": {
           |      "line1": "TestLine1",
           |      "line2": "TestLine2",
           |      "postcode": "TE 1ST"
           |    },
           |    "contact": {
           |      "email": "test@t.test",
           |      "tel": "1234",
           |      "mobile": "5678"
           |    }
           |  }
           |}""".stripMargin)

      Json.toJson(data)(LodgingOfficer.apiWrites) shouldBe validJson
    }

    "throw an IllegalStateException when trying to convert to Json with missing Officer security data" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
        role = "Director"
      )

      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
      val prevAddress = ScrsAddress(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"))

      val data = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = None,
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
        formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(false, Some(prevAddress)))
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(LodgingOfficer.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Officer current address view data" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
        role = "Director"
      )

      val prevAddress = ScrsAddress(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"))

      val data = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z")),
        homeAddress = None,
        contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
        formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(false, Some(prevAddress)))
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(LodgingOfficer.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Officer current address data" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
        role = "Director"
      )

      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
      val prevAddress = ScrsAddress(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"))

      val data = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z")),
        homeAddress = Some(HomeAddressView(currentAddress.id, None)),
        contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
        formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(false, Some(prevAddress)))
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(LodgingOfficer.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Officer contact data" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
        role = "Director"
      )

      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
      val prevAddress = ScrsAddress(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"))

      val data = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z")),
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = None,
        formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(false, Some(prevAddress)))
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(LodgingOfficer.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Officer former name view data" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
        role = "Director"
      )

      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
      val prevAddress = ScrsAddress(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"))

      val data = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z")),
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
        formerName = None,
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(false, Some(prevAddress)))
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(LodgingOfficer.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Officer former name data" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
        role = "Director"
      )

      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
      val prevAddress = ScrsAddress(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"))

      val data = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z")),
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
        formerName = Some(FormerNameView(true, None)),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(false, Some(prevAddress)))
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(LodgingOfficer.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Officer former name change date" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
        role = "Director"
      )

      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
      val prevAddress = ScrsAddress(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"))

      val data = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z")),
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
        formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
        formerNameDate = None,
        previousAddress = Some(PreviousAddressView(false, Some(prevAddress)))
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(LodgingOfficer.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Officer previous address view data" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
        role = "Director"
      )

      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))

      val data = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z")),
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
        formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = None
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(LodgingOfficer.apiWrites))
    }

    "throw an IllegalStateException when trying to convert to Json with missing Officer previous address data" in {
      val officer = Officer(
        name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
        role = "Director"
      )

      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))

      val data = LodgingOfficer(
        completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
        securityQuestions = Some(SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA112233Z")),
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
        formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
        formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
        previousAddress = Some(PreviousAddressView(false, None))
      )

      an[IllegalStateException] shouldBe thrownBy(Json.toJson(data)(LodgingOfficer.apiWrites))
    }
  }
}