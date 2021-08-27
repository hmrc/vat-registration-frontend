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

import models.api.returns._
import play.api.data.Form
import testHelpers.VatRegSpec

class AccountingPeriodFormSpec extends VatRegSpec {

  val form: Form[QuarterlyStagger] = AccountingPeriodForm.form

  "Binding AccountingPeriodForm" should {
    "Bind successfully for a jan, apr, jul, oct selection" in {
      val data = Map(
        "value" -> "jan"
      )
      form.bind(data).get mustBe JanuaryStagger
    }

    "Bind successfully for a feb, may, aug, nov selection" in {
      val data = Map(
        "value" -> "feb"
      )
      form.bind(data).get mustBe FebruaryStagger
    }

    "Bind successfully for a mar, jun, sep, dec selection" in {
      val data = Map(
        "value" -> "mar"
      )
      form.bind(data).get mustBe MarchStagger
    }

    "Fail to bind successfully for an invalid selection" in {
      val data = Map(
        "value" -> "invalidSelection"
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "value"
      bound.errors.head.message mustBe "validation.accounting.period.missing"
    }

    "Fail to bind successfully if empty" in {
      val data = Map(
        "value" -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe "value"
      bound.errors.head.message mustBe "validation.accounting.period.missing"
    }
  }
}
