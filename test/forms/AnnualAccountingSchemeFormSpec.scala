/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.AnnualAccountingSchemeForm._
import testHelpers.VatRegSpec

class AnnualAccountingSchemeFormSpec extends VatRegSpec {

  val form = AnnualAccountingSchemeForm.form

  "Binding AnnualAccountingSchemeForm" should {
    "Bind successfully for a yes selection" in {
      val data = Map(
        ANNUAL_ACCOUNTING_SCHEME -> "true"
      )
      form.bind(data).get mustBe true
    }

    "Bind successfully for a no selection" in {
      val data = Map(
        ANNUAL_ACCOUNTING_SCHEME -> "false"
      )
      form.bind(data).get mustBe false
    }

    "Fail to bind successfully for an invalid selection" in {
      val data = Map(
        ANNUAL_ACCOUNTING_SCHEME -> "invalidSelection"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe ANNUAL_ACCOUNTING_SCHEME
      bound.errors.head.message mustBe errorMsg
    }

    "Fail to bind successfully if empty" in {
      val data = Map(
        ANNUAL_ACCOUNTING_SCHEME -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe ANNUAL_ACCOUNTING_SCHEME
      bound.errors.head.message mustBe errorMsg
    }
  }
}
