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

import features.financials.models.Stagger._
import forms.AccountingPeriodForm
import forms.AccountingPeriodForm._
import helpers.VatRegSpec

class AccountingPeriodFormSpec extends VatRegSpec {

  val form = AccountingPeriodForm.form

  "Binding AccountingPeriodForm" should {
    "Bind successfully for a jan, apr, jul, oct selection" in {
      val data = Map(
        ACCOUNTING_PERIOD -> "jan"
      )
      form.bind(data).get mustBe jan
    }

    "Bind successfully for a feb, may, aug, nov selection" in {
      val data = Map(
        ACCOUNTING_PERIOD -> "feb"
      )
      form.bind(data).get mustBe feb
    }

    "Bind successfully for a mar, jun, sep, dec selection" in {
      val data = Map(
        ACCOUNTING_PERIOD -> "mar"
      )
      form.bind(data).get mustBe mar
    }

    "Fail to bind successfully for an invalid selection" in {
      val data = Map(
        ACCOUNTING_PERIOD -> "invalidSelection"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe ACCOUNTING_PERIOD
      bound.errors.head.message mustBe accountingPeriodEmptyKey
    }

    "Fail to bind successfully if empty" in {
      val data = Map(
        ACCOUNTING_PERIOD -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe ACCOUNTING_PERIOD
      bound.errors.head.message mustBe accountingPeriodInvalidKey
    }
  }
}
