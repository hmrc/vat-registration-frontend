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

package viewmodels.tasklist

import fixtures.VatRegistrationFixture
import testHelpers.VatRegSpec
import viewmodels.tasklist.RegistrationReasonTaskList

class RegistrationReasonTaskListSpec extends VatRegSpec with VatRegistrationFixture {

  val section = app.injector.instanceOf[RegistrationReasonTaskList]

  "registrationReasonRow checks" when {
    "the eligibility block is undefined" must {
      "return false" in {
        section.registrationReasonRow(testRegId).checks(emptyVatScheme) mustBe Seq(false)
      }
    }
    "the elgibility block is defined" must {
      "return true" in {
        val scheme = emptyVatScheme.copy(eligibilitySubmissionData = Some(validEligibilitySubmissionData))
        section.registrationReasonRow(testRegId).checks(scheme) mustBe Seq(true)
      }
    }
  }

  "registrationReasonRow prerequisites" must {
    "return true" in {
      section.registrationReasonRow(testRegId).prerequisites(emptyVatScheme).forall(_.isComplete(emptyVatScheme)) mustBe true
    }
  }

}
