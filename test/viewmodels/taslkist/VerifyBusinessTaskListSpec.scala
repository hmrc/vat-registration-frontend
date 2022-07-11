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

package viewmodels.taslkist

import fixtures.VatRegistrationFixture
import testHelpers.VatRegSpec
import viewmodels.tasklist.VerifyBusinessTaskList

class VerifyBusinessTaskListSpec extends VatRegSpec with VatRegistrationFixture {

  val section = app.injector.instanceOf[VerifyBusinessTaskList]

  "The checks for the business info row" when {
    "the Applicant block is undefined" must {
      "return false" in {
        section.businessInfoRow.checks(emptyVatScheme) mustBe Seq(false)
      }
    }
    "the entity part of the Applicant block is undefined" must {
      "return true" in {
        section.businessInfoRow.checks(emptyVatScheme.copy(applicantDetails = Some(completeApplicantDetails.copy(entity = None)))) mustBe Seq(false)
      }
    }
    "the entity part of the Applicant block is defined" must {
      "return true" in {
        section.businessInfoRow.checks(emptyVatScheme.copy(applicantDetails = Some(completeApplicantDetails))) mustBe Seq(true)
      }
    }
  }

  "the prerequisites for the business" when {
    "the prerequisites are complete" must {
      "return true" in {
        val scheme = emptyVatScheme.copy(eligibilitySubmissionData = Some(validEligibilitySubmissionData))
        section.businessInfoRow.prerequisites(scheme).forall(_.isComplete(scheme)) mustBe true
      }
    }
    "the prerequisites aren't complete" must {
      "return false" in {
        val scheme = emptyVatScheme
        section.businessInfoRow.prerequisites(scheme).forall(_.isComplete(scheme)) mustBe false
      }
    }
  }

}
