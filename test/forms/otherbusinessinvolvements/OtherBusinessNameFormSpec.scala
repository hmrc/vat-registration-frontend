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

package forms.otherbusinessinvolvements

import helpers.FormInspectors.FormErrorOps
import testHelpers.VatRegSpec

class OtherBusinessNameFormSpec extends VatRegSpec {

  val formValue: String = OtherBusinessNameForm.businessNameKey
  val testBusinessName = "testBusinessName"

  "FormerNameForm" must {
    "bind successfully with valid data" in {
      val data = Map(
        formValue -> testBusinessName
      )
      val boundForm = OtherBusinessNameForm().bind(data)

      boundForm.value mustBe Some(testBusinessName)
    }

    "have the correct error if nothing is entered" in {
      val data = Map(
        formValue -> ""
      )
      val boundForm = OtherBusinessNameForm().bind(data)

      boundForm shouldHaveErrors Seq(
        formValue -> "validation.obi.otherBusinessName.missing"
      )
    }

    "have the correct error if an invalid name is entered" in {
      val data = Map(
        formValue -> "неверное имя"
      )
      val boundForm = OtherBusinessNameForm().bind(data)

      boundForm shouldHaveErrors Seq(
        formValue -> "validation.obi.otherBusinessName.invalid"
      )
    }

    "have the correct error if the name length is too long" in {
      val data = Map(
        formValue -> "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
      )
      val boundForm = OtherBusinessNameForm().bind(data)

      boundForm shouldHaveErrors Seq(
        formValue -> "validation.obi.otherBusinessName.maxlen"
      )
    }

    "unbind successfully with valid data" in {
      val data = Map(
        formValue -> testBusinessName
      )

      OtherBusinessNameForm().fill(testBusinessName).data mustBe data
    }
  }
}
