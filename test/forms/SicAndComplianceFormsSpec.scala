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

package forms

import helpers.FormInspectors._
import models.BusinessActivityDescription
import testHelpers.VatRegSpec

class SicAndComplianceFormsSpec extends VatRegSpec {
  "Business Activity Description form" must {
    val testForm = BusinessActivityDescriptionForm.form
    val desc = "bla bla , bla  & blo-bli ble/blip"

    "be valid" when {
      "a correct description is provided" in {
        val data = Map("description" -> Seq(desc))
        testForm.bindFromRequest(data) shouldContainValue BusinessActivityDescription(desc)
      }
    }

    "be rejected with correct error messages" when {
      "no description is provided" in {
        val data = Map("description" -> Seq(""))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("description" -> "validation.businessActivity.description.missing")
      }
      "a description with incorrect characters is provided" in {
        val data = Map("description" -> Seq("@gdf gd"))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("description" -> "validation.businessActivity.description.invalid")
      }
      "a description with too many characters is provided" in {
        val data = Map("description" -> Seq(List.fill(63)("qw e").mkString))
        testForm.bindFromRequest(data) shouldHaveErrors Seq("description" -> "validation.businessActivity.description.maxlen")
      }
    }
  }
}
