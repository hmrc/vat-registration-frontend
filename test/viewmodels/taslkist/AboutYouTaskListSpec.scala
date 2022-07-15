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
import models.PartnerEntity
import models.api.{Individual, Partnership, Trust}
import testHelpers.VatRegSpec
import viewmodels.tasklist.AboutYouTaskList

class AboutYouTaskListSpec extends VatRegSpec with VatRegistrationFixture {

  val section: AboutYouTaskList = app.injector.instanceOf[AboutYouTaskList]

  "checks for the lead partner details row" when {
    "lead partner entity is not available" must {
      "return false" in {
        section.leadPartnerDetailsRow.checks(emptyVatScheme) mustBe Seq(false)
      }
    }
    "lead partner entity is available and applicable for lead partner section" must {
      "return true" in {
        section.leadPartnerDetailsRow.checks(
          emptyVatScheme.copy(partners = Some(List(PartnerEntity(testSoleTrader, Partnership, isLeadPartner = true))))
        ) mustBe Seq(true)
      }
    }
    "lead partner entity is available and not a lead partner" must {
      "return true" in {
        section.leadPartnerDetailsRow.checks(
          emptyVatScheme.copy(partners = Some(List(PartnerEntity(testSoleTrader, Partnership, isLeadPartner = false))))
        ) mustBe Seq(false)
      }
    }
  }

  "the prerequisites for the business" when {
    "complete" must {
      "return true" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          applicantDetails = Some(completeApplicantDetails)
        )
        section.leadPartnerDetailsRow.prerequisites(scheme).forall(_.isComplete(scheme)) mustBe true
      }
    }
    "not complete" must {
      "return false" in {
        val scheme = emptyVatScheme
        section.leadPartnerDetailsRow.prerequisites(scheme).forall(_.isComplete(scheme)) mustBe false
      }
    }
  }

}
