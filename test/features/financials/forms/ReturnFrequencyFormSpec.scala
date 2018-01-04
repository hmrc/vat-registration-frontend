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

package features.financials.forms

import forms.ReturnFrequencyForm
import helpers.VatRegSpec
import forms.ReturnFrequencyForm._
import features.financials.models.Frequency._

class ReturnFrequencyFormSpec extends VatRegSpec {

  val form = ReturnFrequencyForm.form

  "Binding ReturnFrequencyForm" should {
    "Bind successfully for a monthly selection" in {
      val data = Map(
        RETURN_FREQUENCY -> "monthly"
      )
      form.bind(data).get mustBe monthly
    }

    "Bind successfully for a quarterly selection" in {
      val data = Map(
        RETURN_FREQUENCY -> "quarterly"
      )
      form.bind(data).get mustBe quarterly
    }

    "Fail to bind successfully for an invalid selection" in {
      val data = Map(
        RETURN_FREQUENCY -> "invalidSelection"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe RETURN_FREQUENCY
      bound.errors.head.message mustBe returnFrequencyEmptyKey
    }

    "Fail to bind successfully if empty" in {
      val data = Map(
        RETURN_FREQUENCY -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe RETURN_FREQUENCY
      bound.errors.head.message mustBe returnFrequencyInvalidKey
    }
  }
}
