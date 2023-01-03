/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.otherbusinessinvolvements

import helpers.FormInspectors.FormErrorOps
import testHelpers.VatRegSpec

class RemoveOtherBusinessFormSpec extends VatRegSpec {

  val testOtherBusinessName = "testOtherBusinessName"
  val formValue = RemoveOtherBusinessForm(testOtherBusinessName).removeOtherBusinessKey
  val testRemoveOtherBusiness = "true"

  "RemoveOtherBusinessForm" must {
    "bind successfully with valid data" in {
      val data = Map(
        formValue -> testRemoveOtherBusiness
      )
      val boundForm = RemoveOtherBusinessForm(testOtherBusinessName).form.bind(data)

      boundForm.value mustBe Some(true)
    }

    "have the correct error if nothing is entered" in {
      val data = Map(
        formValue -> ""
      )
      val boundForm = RemoveOtherBusinessForm(testOtherBusinessName).form.bind(data)

      boundForm shouldHaveErrors Seq(
        formValue -> "validation.obi.removeOtherBusiness.missing"
      )
    }

    "unbind successfully with valid data" in {
      val data = Map(
        formValue -> testRemoveOtherBusiness
      )

      RemoveOtherBusinessForm(testOtherBusinessName).form.fill(true).data mustBe data
    }
  }
}
