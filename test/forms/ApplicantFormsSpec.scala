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

import helpers.FormInspectors._
import testHelpers.VatRegSpec

import java.time.LocalDate

class ApplicantFormsSpec extends VatRegSpec {

  "FormerNameDateForm" should {
    val testForm = FormerNameDateForm.form(LocalDate.of(2000, 1, 1))
    val testData = LocalDate.of(2000, 1, 1)

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

  "PreviousAddressForm" should {
    val testForm = PreviousAddressForm.form()
    val testData = false

    "bind successfully with data" in {
      val data = Map(
        "value" -> "true"
      )

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result mustBe true
    }

    "have the correct error if no data is provided" in {
      val data: Map[String, String] = Map()
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("value" -> "validation.previousAddressQuestion.missing")
    }

    "have the correct error if no data is provided and an alternate error code is specified" in {
      val data: Map[String, String] = Map()
      val boundForm = PreviousAddressForm.form("previousAddressQuestionThirdParty").bind(data)

      boundForm shouldHaveErrors Seq("value" -> "validation.previousAddressQuestionThirdParty.missing")
    }

    "Unbind successfully with full data" in {
      val data = Map("value" -> "false")

      testForm.fill(testData).data mustBe data
    }
  }
}
