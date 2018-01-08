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
import features.officer.models.view.{FormerNameDateView, FormerNameView, SecurityQuestionsView}
import helpers.FormInspectors._
import uk.gov.hmrc.play.test.UnitSpec

class OfficerFormsSpec extends UnitSpec {
  "CompletionCapacityForm" should {
    val testForm = CompletionCapacityForm.form
    val testName = "TestData"

    "bind successfully with data" in {
      val data = Map(NAME_ID -> testName)

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result shouldBe testName
    }

    "have the correct error if no data is provided" in {
      val data: Map[String, String] = Map()
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq(NAME_ID -> "validation.completionCapacity.missing")
    }

    "Unbind successfully with full data" in {
      val data = Map(NAME_ID -> testName)

      testForm.fill(testName).data shouldBe data
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
    val testForm = FormerNameForm.form
    val testDataWithName = FormerNameView(true, Some("Test Old Name"))
    val testDataNoName = FormerNameView(false, None)

    "bind successfully with data" in {
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

    "have the correct error if nothing is selected" in {
      val data = Map(
        "formerNameRadio" -> "",
        "formerName" -> ""
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerNameRadio" -> "error.required")
    }

    "have the correct error if true is selected and no name is provided" in {
      val data = Map(
        "formerNameRadio" -> "true",
        "formerName" -> ""
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerName" -> "error.required")
    }

    "have the correct error if true is selected and no name is provided" in {
      val data = Map(
        "formerNameRadio" -> "true",
        "formerName" -> "wrong N@m€"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq("formerName" -> "validation.formerName.selected.invalid")
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
        "formerNameRadio" -> "false",
        "formerName" -> ""
      )

      testForm.fill(testDataNoName).data shouldBe data
    }
  }

  "FormerNameDateForm" should {
    val testForm = FormerNameDateForm.form
    val testData = FormerNameDateView(LocalDate.of(1998, 7, 12))

    "bind successfully with data" in {
      val data = Map(
        "formerNameDate.day" -> "12",
        "formerNameDate.month" -> "7",
        "formerNameDate.year" -> "1998"
      )

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result shouldBe testData
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

      boundForm shouldHaveErrors Seq("dob" -> "validation.formerNameDate.invalid")
    }

    "Unbind successfully with full data" in {
      val data = Map(
        "formerNameDate.day" -> "12",
        "formerNameDate.month" -> "7",
        "formerNameDate.year" -> "1998"
      )

      testForm.fill(testData).data shouldBe data
    }
  }
}
