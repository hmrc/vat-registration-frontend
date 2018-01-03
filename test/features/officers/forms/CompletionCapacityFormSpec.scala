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

package features.officers.forms

import helpers.FormInspectors._
import forms.vatLodgingOfficer.CompletionCapacityForm
import forms.vatLodgingOfficer.CompletionCapacityForm.NAME_ID
import uk.gov.hmrc.play.test.UnitSpec

class CompletionCapacityFormSpec extends UnitSpec {
  val testForm = CompletionCapacityForm.form
  val testName = "TestData"

  "Binding CompletionCapacityForm to a String" should {
    "bind successfully with data" in {
      val data = Map(NAME_ID -> testName)

      val result = testForm.bind(data).fold(
        errors => errors,
        success => success
      )

      result shouldBe testName
    }

    "have the correct error if no data is provided" in {
      val data: Map[String,String] = Map()
      val boundForm = testForm.bind(data)

      boundForm shouldHaveErrors Seq(NAME_ID -> "validation.completionCapacity.missing")
    }
  }

  "Unbinding CompletionCapacityForm to a Map" should {
    "Unbind successfully with full data" in {
      val data = Map(NAME_ID -> testName)

      testForm.fill(testName).data shouldBe data
    }
  }
}
