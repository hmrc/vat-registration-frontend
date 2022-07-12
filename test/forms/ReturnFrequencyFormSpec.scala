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

import models.api.vatapplication._
import play.api.data.Form
import testHelpers.VatRegSpec

class ReturnFrequencyFormSpec extends VatRegSpec {

  val form: Form[ReturnsFrequency] = ReturnFrequencyForm.form

  "Binding ReturnFrequencyForm" should {
    "Bind successfully for a monthly selection" in {
      val data = Map(
        "value" -> "monthly"
      )
      form.bind(data).get mustBe Monthly
    }

    "Bind successfully for a quarterly selection" in {
      val data = Map(
        "value" -> "quarterly"
      )
      form.bind(data).get mustBe Quarterly
    }

    "Fail to bind successfully for an invalid selection" in {
      val data = Map(
        "value" -> "invalidSelection"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "value"
      bound.errors.head.message mustBe "validation.vat.return.frequency.missing"
    }

    "Fail to bind successfully if empty" in {
      val data = Map(
        "value" -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "value"
      bound.errors.head.message mustBe "validation.vat.return.frequency.missing"
    }
  }
}
