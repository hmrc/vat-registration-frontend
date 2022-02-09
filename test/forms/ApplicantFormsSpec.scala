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

import java.time.LocalDate
import forms.HomeAddressForm.ADDRESS_ID
import helpers.FormInspectors._
import models.api.Address
import models.view._
import testHelpers.VatRegSpec

class ApplicantFormsSpec extends VatRegSpec {


  "FormerNameForm" should {
    val testForm = FormerNameForm.form
    val testDataWithName = FormerNameView(true, Some("Test Old Name"))
    val testDataNoName = FormerNameView(false, None)

    "bind successfully with data set to true" in {
      val data = Map(
        "value" -> "true",
        "formerName" -> "Test Old Name"
      )

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result mustBe testDataWithName
    }

    "bind successfully with data set to false" in {
      val data = Map(
        "value" -> "false",
        "formerName" -> ""
      )

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result mustBe testDataNoName
    }

    "have the correct error if nothing is selected" in {
      val data = Map(
        "value" -> "",
        "formerName" -> "Test Old Name"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("value" -> "validation.formerName.choice.missing")
    }

    "have the correct error if true is selected and no name is provided" in {
      val data = Map(
        "value" -> "true",
        "formerName" -> ""
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerName" -> "validation.formerName.missing")
    }

    "have the correct error if true is selected and invalid name is provided" in {
      val data = Map(
        "value" -> "true",
        "formerName" -> "wrong N@m€"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerName" -> "validation.formerName.invalid")
    }

    "have the correct error if true is selected and a too long name is provided" in {
      val data = Map(
        "value" -> "true",
        "formerName" -> "tooooooooooooooooooo looooooooooonnnnnnng nnnnaaaaaaaaaaammeeeeeeeeeeee"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerName" -> "validation.formerName.maxlen")
    }

    "Unbind successfully with true and valid name" in {
      val data = Map(
        "value" -> "true",
        "formerName" -> "Test Old Name"
      )

      testForm.fill(testDataWithName).data mustBe data
    }

    "Unbind successfully with false and no name" in {
      val data = Map(
        "value" -> "false"
      )

      testForm.fill(testDataNoName).data mustBe data
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

      result mustBe testData
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

      testForm.fill(testData).data mustBe data
    }
  }

  "ApplicantHomeAddressForm" should {
    val address = Address(line1 = "TestLine1", line2 = Some("TestLine2"), postcode = Some("TE 1ST"), addressValidated = true)
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

      result mustBe testData.copy(address = None)
    }

    "have the correct error if no data is provided" in {
      val data: Map[String, String] = Map()
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq(ADDRESS_ID -> "validation.applicantHomeAddress.missing")
    }

    "Unbind successfully with full data" in {
      val data = Map(ADDRESS_ID -> address.id)

      testForm.fill(testData).data mustBe data
    }
  }

  "PreviousAddressForm" should {
    val address = Address(line1 = "TestLine1", line2 = Some("TestLine2"), postcode = Some("TE 1ST"), addressValidated = true)
    val testForm = PreviousAddressForm.form
    val testData = PreviousAddressView(yesNo = false, Some(address))

    "bind successfully with data" in {
      val data = Map(
        "previousAddressQuestionRadio" -> "true"
      )

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result mustBe PreviousAddressView(yesNo = true, None)
    }

    "have the correct error if no data is provided" in {
      val data: Map[String, String] = Map()
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("previousAddressQuestionRadio" -> "validation.previousAddressQuestion.missing")
    }

    "Unbind successfully with full data" in {
      val data = Map("previousAddressQuestionRadio" -> "false")

      testForm.fill(testData).data mustBe data
    }
  }
}
