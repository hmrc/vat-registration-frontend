/*
 * Copyright 2024 HM Revenue & Customs
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

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}

class TotalTaxTurnoverEstimateFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val totalTaxTurnoverEstimateForm: Form[Boolean] = new TotalTaxTurnoverEstimateForm()()
  lazy val emptyForm: Map[String, String] = Map[String, String]()
  val fieldName = "value"
  val requiredError = "validation.ttEstimate.error"

  "The totalTaxTurnoverEstimateForm" must {
    "validate whether the form is valid" in {
      val form = totalTaxTurnoverEstimateForm.bind(emptyForm)
      form.errors must contain(FormError(fieldName, requiredError))
    }

    "validate that missing yesNo answer fails" in {
      val form = totalTaxTurnoverEstimateForm.bind(Map(fieldName -> ""))

      form.errors must contain(FormError(fieldName, requiredError))
    }

    "validate form with yes option checked" in {
      val form = totalTaxTurnoverEstimateForm.bind(Map(fieldName -> "true"))

      form.value mustBe Some(true)
    }

    "validate form with no option checked" in {
      val form = totalTaxTurnoverEstimateForm.bind(Map(fieldName -> "false"))

      form.value mustBe Some(false)
    }
  }
}
