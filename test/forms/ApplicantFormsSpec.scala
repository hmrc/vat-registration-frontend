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

import forms.HomeAddressForm.ADDRESS_ID
import helpers.FormInspectors._
import models.api.Address
import models.view._
import testHelpers.VatRegSpec

import java.time.LocalDate

class ApplicantFormsSpec extends VatRegSpec {



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
