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

import java.time.LocalDate

import features.returns.DateSelection
import features.returns.DateSelection._
import forms.MandatoryDateForm.{MANDATORY_DATE, MANDATORY_SELECTION}
import forms.VoluntaryDateFormIncorp
import forms.VoluntaryDateFormIncorp._
import helpers.VatRegSpec
import play.api.data.Form

class VoluntaryDateFormIncorpSpec extends VatRegSpec {

  val incorpDate: LocalDate = LocalDate.of(2018, 1, 1)

  val form: Form[(DateSelection.Value, Option[LocalDate])] = VoluntaryDateFormIncorp.form(incorpDate)

  "Binding MandatoryDateForm" should {
    "Bind successfully for an incorp date selection" in {
      val data = Map(
        VOLUNTARY_SELECTION -> "company_registration_date",
        VOLUNTARY_DATE -> ""
      )
      form.bind(data).get mustBe (company_registration_date, None)
    }

    "Bind successfully for a business start date selection" in {
      val data = Map(
        VOLUNTARY_SELECTION -> "business_start_date",
        VOLUNTARY_DATE -> ""
      )
      form.bind(data).get mustBe (business_start_date, None)
    }

    "Bind successfully for a valid specific date selection" in {
      val data = Map(
        MANDATORY_SELECTION -> "specific_date",
        s"$MANDATORY_DATE.day" -> "5",
        s"$MANDATORY_DATE.month" -> "1",
        s"$MANDATORY_DATE.year" -> "2018"
      )
      form.bind(data).get mustBe(specific_date, Some(LocalDate.of(2018, 1, 5)))
    }

    "Fail to bind successfully for no selection" in {
      val data = Map(
        VOLUNTARY_SELECTION -> "",
        VOLUNTARY_DATE -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe VOLUNTARY_SELECTION
      bound.errors.head.message mustBe voluntarySelectionInvalidKey
    }

    "Fail to bind successfully for an invalid selection" in {
      val data = Map(
        VOLUNTARY_SELECTION -> "invalidSelection",
        VOLUNTARY_DATE -> ""
      )
      val bound = form.bind(data)
      bound.errors.size mustBe 1
      bound.errors.head.key mustBe VOLUNTARY_SELECTION
      bound.errors.head.message mustBe voluntarySelectionInvalidKey
    }
  }
}