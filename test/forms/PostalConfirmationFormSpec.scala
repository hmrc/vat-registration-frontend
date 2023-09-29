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

package forms

import helpers.FormInspectors.FormErrorOps
import play.api.data.Form
import testHelpers.VatRegSpec

class PostalConfirmationFormSpec extends VatRegSpec {
  val testForm: Form[Boolean] = PostalConfirmationPageForm()

  "Postal confirmation form" must {
    "be valid" when {
      "yes is selected" in {
        val data = Map("value" -> Seq("true"))
        testForm.bindFromRequest(data) shouldContainValue true
      }

      "no is selected" in {
        val data = Map("value" -> Seq("false"))
        testForm.bindFromRequest(data) shouldContainValue false
      }
    }

    "be rejected with correct error messages" when {
      "no data is provided" in {
        val data = Map[String, Seq[String]]().empty
        testForm.bindFromRequest(data) shouldHaveErrors Seq("value" -> "error.required")
      }
    }
  }
}
