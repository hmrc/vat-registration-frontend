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
import models.external.Name
import testHelpers.VatRegSpec

class FormerNameCaptureFormSpec extends VatRegSpec {


  "FormerNameCaptureForm" must {
    val testForm = FormerNameCaptureForm.form
    val testFirstName = "Test"
    val testLastName = "Name"
    val testName = Name(Some(testFirstName), last = testLastName)

    "bind successfully with a valid name" in {
      val data = Map(
        "formerFirstName" -> testFirstName,
        "formerLastName" -> testLastName
      )

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result mustBe testName
    }

    "have the correct error if a name is missing" in {
      val data = Map(
        "formerFirstName" -> "",
        "formerLastName" -> ""
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq(
        "formerFirstName" -> "validation.formerNameCapture.first.missing",
        "formerLastName" -> "validation.formerNameCapture.last.missing"
      )
    }

    "have the correct error if a name is invalid" in {
      val data = Map(
        "formerFirstName" -> "$$££",
        "formerLastName" -> "$$££"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq(
        "formerFirstName" -> "validation.formerNameCapture.first.invalid",
        "formerLastName" -> "validation.formerNameCapture.last.invalid"
      )
    }

    "have the correct error if a name exceeds the maximum length" in {
      val data = Map(
        "formerFirstName" -> "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        "formerLastName" -> "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
      )
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq(
        "formerFirstName" -> "validation.formerNameCapture.first.maxlen",
        "formerLastName" -> "validation.formerNameCapture.last.maxlen"
      )
    }

    "unbind successfully with valid name" in {
      val data = Map(
        "formerFirstName" -> testFirstName,
        "formerLastName" -> testLastName
      )

      testForm.fill(testName).data mustBe data
    }
  }

}
