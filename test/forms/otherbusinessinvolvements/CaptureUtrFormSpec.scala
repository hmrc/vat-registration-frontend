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

class CaptureUtrFormSpec extends VatRegSpec {

  val formValue: String = CaptureUtrForm.captureUtrKey
  val testUtr = "1234567890"

  "CaptureUtrForm" must {
    "bind successfully with valid data" in {
      val data = Map(
        formValue -> testUtr
      )
      val boundForm = CaptureUtrForm().bind(data)

      boundForm.value mustBe Some(testUtr)
    }

    "have the correct error if nothing is entered" in {
      val data = Map(
        formValue -> ""
      )
      val boundForm = CaptureUtrForm().bind(data)

      boundForm shouldHaveErrors Seq(
        formValue -> "validation.obi.captureUtr.missing"
      )
    }

    "have the correct error if an invalid utr is entered" in {
      val data = Map(
        formValue -> "123456789a"
      )
      val boundForm = CaptureUtrForm().bind(data)

      boundForm shouldHaveErrors Seq(
        formValue -> "validation.obi.captureUtr.invalid"
      )
    }

    "have the correct error if the utr length is too long" in {
      val data = Map(
        formValue -> "12345678900"
      )
      val boundForm = CaptureUtrForm().bind(data)

      boundForm shouldHaveErrors Seq(
        formValue -> "validation.obi.captureUtr.invalid"
      )
    }

    "unbind successfully with valid data" in {
      val data = Map(
        formValue -> testUtr
      )

      CaptureUtrForm().fill(testUtr).data mustBe data
    }
  }
}
