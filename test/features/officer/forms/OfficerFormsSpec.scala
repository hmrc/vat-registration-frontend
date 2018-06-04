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

package features.officer.forms

import java.time.LocalDate

import features.officer.forms.CompletionCapacityForm.NAME_ID
import features.officer.forms.HomeAddressForm.ADDRESS_ID
import features.officer.models.view._
import helpers.FormInspectors._
import models.api.ScrsAddress
import uk.gov.hmrc.play.test.UnitSpec

class OfficerFormsSpec extends UnitSpec {
  "CompletionCapacityForm" should {
    val testForm = CompletionCapacityForm.form
    val testName = "TestData"
    val testData = CompletionCapacityView(testName, None)

    "bind successfully with data" in {
      val data = Map(NAME_ID -> testName)

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result shouldBe testData
    }

    "have the correct error if no data is provided" in {
      val data: Map[String, String] = Map()
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq(NAME_ID -> "validation.completionCapacity.missing")
    }

    "Unbind successfully with full data" in {
      val data = Map(NAME_ID -> testName)

      testForm.fill(testData).data shouldBe data
    }
  }

  "SecurityQuestionsForm" should {
    val testForm = SecurityQuestionsForm.form
    val testData = SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA123456C")

    "bind successfully with data" in {
      val data = Map(
        "dob.day" -> "12",
        "dob.month" -> "7",
        "dob.year" -> "1998",
        "nino" -> testData.nino
      )

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result shouldBe testData
    }

    "have the correct error if no dob month is provided" in {
      val data = Map(
        "dob.day" -> "12",
        "dob.year" -> "1998",
        "nino" -> testData.nino
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("dob.month" -> "error.required")
    }

    "have the correct error if no correct dob is provided" in {
      val data = Map(
        "dob.day" -> "12",
        "dob.month" -> "13",
        "dob.year" -> "1998",
        "nino" -> testData.nino
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("dob" -> "validation.security.questions.dob.invalid")
    }

    "have the correct error if no nino is provided" in {
      val data = Map(
        "dob.day" -> "12",
        "dob.month" -> "7",
        "dob.year" -> "1998"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("nino" -> "error.required")
    }

    "have the correct error if no correct nino is provided" in {
      val data = Map(
        "dob.day" -> "12",
        "dob.month" -> "7",
        "dob.year" -> "1998",
        "nino" -> "AA123456Z"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("nino" -> "validation.security.questions.nino.invalid")
    }

    "Unbind successfully with full data" in {
      val data = Map(
        "dob.day" -> "12",
        "dob.month" -> "7",
        "dob.year" -> "1998",
        "nino" -> testData.nino
      )

      testForm.fill(testData).data shouldBe data
    }
  }

  "FormerNameForm" should {
    val testForm = FormerNameForm.form("TestCurrentName")
    val testDataWithName = FormerNameView(true, Some("Test Old Name"))
    val testDataNoName = FormerNameView(false, None)

    "bind successfully with data set to true" in {
      val data = Map(
        "formerNameRadio" -> "true",
        "formerName" -> "Test Old Name"
      )

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result shouldBe testDataWithName
    }

    "bind successfully with data set to false" in {
      val data = Map(
        "formerNameRadio" -> "false",
        "formerName" -> ""
      )

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result shouldBe testDataNoName
    }

    "have the correct error if nothing is selected" in {
      val data = Map(
        "formerNameRadio" -> "",
        "formerName" -> "Test Old Name"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerNameRadio" -> "validation.formerName.choice.missing")
    }

    "have the correct error if true is selected and no name is provided" in {
      val data = Map(
        "formerNameRadio" -> "true",
        "formerName" -> ""
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerName" -> "validation.formerName.missing")
    }

    "have the correct error if true is selected and invalid name is provided" in {
      val data = Map(
        "formerNameRadio" -> "true",
        "formerName" -> "wrong N@m€"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerName" -> "validation.formerName.invalid")
    }

    "have the correct error if true is selected and a too long name is provided" in {
      val data = Map(
        "formerNameRadio" -> "true",
        "formerName" -> "tooooooooooooooooooo looooooooooonnnnnnng nnnnaaaaaaaaaaammeeeeeeeeeeee"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerName" -> "validation.formerName.maxlen")
    }

    "have the correct error if true is selected and the name provided is exact same as the completion capacity" in {
      val data = Map(
        "formerNameRadio" -> "true",
        "formerName" -> "Test Current Name"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerName" -> "validation.formerName.match.cc")
    }

    "have the correct error if true is selected and the name provided is same as the completion capacity (case insensitive)" in {
      val data = Map(
        "formerNameRadio" -> "true",
        "formerName" -> "test current name"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerName" -> "validation.formerName.match.cc")
    }

    "Unbind successfully with true and valid name" in {
      val data = Map(
        "formerNameRadio" -> "true",
        "formerName" -> "Test Old Name"
      )

      testForm.fill(testDataWithName).data shouldBe data
    }

    "Unbind successfully with false and no name" in {
      val data = Map(
        "formerNameRadio" -> "false"
      )

      testForm.fill(testDataNoName).data shouldBe data
    }
  }

  "FormerNameDateForm" should {
    val testForm = FormerNameDateForm.form(LocalDate.of(2000, 1, 1))
    val testData = FormerNameDateView(LocalDate.of(2000, 1, 1))

    "bind successfully with data" in {
      val data = Map(
        "formerNameDate.day" -> "1",
        "formerNameDate.month" -> "1",
        "formerNameDate.year" -> "2000"
      )

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result shouldBe testData
    }

    "bind unsuccessfully with data" in {
      val data = Map(
        "formerNameDate.day" -> "31",
        "formerNameDate.month" -> "12",
        "formerNameDate.year" -> "1999"
      )

      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerNameDate" -> "validation.formerNameDate.range.below")
    }

    "have the correct error if no formerNameDate month is provided" in {
      val data = Map(
        "formerNameDate.day" -> "12",
        "formerNameDate.year" -> "1998"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerNameDate.month" -> "error.required")
    }

    "have the correct error if no correct formerNameDate is provided" in {
      val data = Map(
        "formerNameDate.day" -> "12",
        "formerNameDate.month" -> "13",
        "formerNameDate.year" -> "1998"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerNameDate" -> "validation.formerNameDate.invalid")
    }

    "Unbind successfully with full data" in {
      val data = Map(
        "formerNameDate.day" -> "1",
        "formerNameDate.month" -> "1",
        "formerNameDate.year" -> "2000"
      )

      testForm.fill(testData).data shouldBe data
    }
  }

  "ContactDetailsForm" should {
    val testForm = ContactDetailsForm.form
    val testData = ContactDetailsView(Some("t@t.tt.co.tt"), Some("12345678901234567890"), Some("5678912345"))

    "bind successfully with data" in {
      val data = Map(
        "email" -> "t@t.tt.co.tt",
        "daytimePhone" -> "12345678901234567890",
        "mobile" -> "5678912345"
      )

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result shouldBe testData
    }

    "have the correct error if no contact field is provided" in {
      val data = Map(
        "email" -> "",
        "daytimePhone" -> "",
        "mobile" -> ""
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveGlobalErrors "validation.officerContact.atLeastOneContact.missing"
    }

    "have the correct error if invalid email is provided" in {
      val data = Map(
        "email" -> "dgdfg.co.tt",
        "daytimePhone" -> "",
        "mobile" -> ""
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("email" -> "validation.officerContactDetails.email.invalid")
    }

    "have the correct error if too long email is provided" in {
      val data = Map(
        "email" -> "dgfffffffffffffffffffffffffgggggggggggggggd@feeeeeeeeg.crrrrrreeero.ttt",
        "daytimePhone" -> "",
        "mobile" -> ""
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("email" -> "validation.officerContactDetails.email.maxlen")
    }

    "have the correct error if too short daytimePhone is provided" in {
      val data = Map(
        "email" -> "",
        "daytimePhone" -> "123 4 5678",
        "mobile" -> ""
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("daytimePhone" -> "validation.invalid.officerContactDetails.tooShort")
    }

    "have the correct error if too short mobile is provided" in {
      val data = Map(
        "email" -> "",
        "daytimePhone" -> "",
        "mobile" -> "123 4 56789"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("mobile" -> "validation.invalid.officerContactDetails.tooShort")
    }

    "have the correct error if too long mobile is provided" in {
      val data = Map(
        "email" -> "",
        "daytimePhone" -> "",
        "mobile" -> "123456789012345678901"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("mobile" -> "validation.invalid.officerContactDetails.tooLong")
    }

    "have the correct error if too long daytimePhone is provided" in {
      val data = Map(
        "email" -> "",
        "daytimePhone" -> "123456789012345678912",
        "mobile" -> ""
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("daytimePhone" -> "validation.invalid.officerContactDetails.tooLong")
    }

    "have the correct error if invalid daytimePhone is provided" in {
      val data = Map(
        "email" -> "",
        "daytimePhone" -> "123er",
        "mobile" -> ""
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("daytimePhone" -> "validation.invalid.officerContactDetails")
    }

    "have the correct error if invalid mobile is provided" in {
      val data = Map(
        "email" -> "",
        "daytimePhone" -> "",
        "mobile" -> "we123"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("mobile" -> "validation.invalid.officerContactDetails")
    }

    "Unbind successfully with full data" in {
      val data = Map(
        "email" -> "t@t.tt.co.tt",
        "daytimePhone" -> "12345678901234567890",
        "mobile" -> "5678912345"
      )

      testForm.fill(testData).data shouldBe data
    }
  }

  "OfficerHomeAddressForm" should {
    val address = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
    val testForm = HomeAddressForm.form
    val testData = HomeAddressView(address.id, Some(address))

    "bind successfully with data" in {
      val data = Map(
        "homeAddressRadio" -> address.id
      )

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result shouldBe testData.copy(address = None)
    }

    "have the correct error if no data is provided" in {
      val data: Map[String, String] = Map()
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq(ADDRESS_ID -> "validation.officerHomeAddress.missing")
    }

    "Unbind successfully with full data" in {
      val data = Map(ADDRESS_ID -> address.id)

      testForm.fill(testData).data shouldBe data
    }
  }

  "PreviousAddressForm" should {
    val address = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
    val testForm = PreviousAddressForm.form
    val testData = PreviousAddressView(false, Some(address))

    "bind successfully with data" in {
      val data = Map(
        "previousAddressQuestionRadio" -> "true"
      )

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result shouldBe PreviousAddressView(true, None)
    }

    "have the correct error if no data is provided" in {
      val data: Map[String, String] = Map()
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("previousAddressQuestionRadio" -> "validation.previousAddressQuestion.missing")
    }

    "Unbind successfully with full data" in {
      val data = Map("previousAddressQuestionRadio" -> "false")

      testForm.fill(testData).data shouldBe data
    }
  }
}
