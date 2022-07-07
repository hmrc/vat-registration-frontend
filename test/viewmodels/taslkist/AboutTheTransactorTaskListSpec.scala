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

import featureswitch.core.config.{FeatureSwitching, FullAgentJourney}
import fixtures.VatRegistrationFixture
import models.{AccountantAgent, ApplicantDetails, CurrentProfile, DeclarationCapacityAnswer, TransactorDetails}
import testHelpers.VatRegSpec
import viewmodels.tasklist.{AboutYouTransactorTaskList, TLCompleted, TLInProgress, TLNotStarted}

class AboutYouTransactorTaskListSpec extends VatRegSpec with VatRegistrationFixture with FeatureSwitching {

  val section = app.injector.instanceOf[AboutYouTransactorTaskList]
  val testArn = "testArn"

  "the user has logged in as an Agent" when {
    "the FullAgentJourney feature switch is enabled" must {
      "pass all checks when the personal details section exists" in {
        enable(FullAgentJourney)
        implicit val cp: CurrentProfile = currentProfile.copy(agentReferenceNumber = Some(testArn))

        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(isTransactor = true)),
          applicantDetails = Some(ApplicantDetails(entity = Some(testLimitedCompany))),
          transactorDetails = Some(TransactorDetails(personalDetails = Some(testPersonalDetails)))
        )

        val res = section.transactorPersonalDetailsRow.build(scheme)

        res.status mustBe TLCompleted
        res.url mustBe controllers.transactor.routes.AgentNameController.show.url
      }
      "be Not Started when personal details is missing" in {
        enable(FullAgentJourney)
        implicit val cp: CurrentProfile = currentProfile.copy(agentReferenceNumber = Some(testArn))

        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(isTransactor = true)),
          applicantDetails = Some(ApplicantDetails(entity = Some(testLimitedCompany)))
        )

        val res = section.transactorPersonalDetailsRow.build(scheme)

        res.status mustBe TLNotStarted
        res.url mustBe controllers.transactor.routes.AgentNameController.show.url

        disable(FullAgentJourney)
      }
    }
  }
  "the user is a transactor" when {
    "all required answers are missing" must {
      "be not started" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(isTransactor = true)),
          applicantDetails = Some(ApplicantDetails(entity = Some(testLimitedCompany)))
        )

        val res = section.transactorPersonalDetailsRow.build(scheme)

        res.status mustBe TLNotStarted
        res.url mustBe controllers.transactor.routes.PartOfOrganisationController.show.url
      }
    }
    "isPartOfOrganisation is true" must {
      "pass all checks for the same answers as an Organisation transactor" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(isTransactor = true)),
          applicantDetails = Some(ApplicantDetails(entity = Some(testLimitedCompany))),
          transactorDetails = Some(TransactorDetails(
            personalDetails = Some(testPersonalDetails),
            isPartOfOrganisation = Some(true),
            organisationName = Some(testCompanyName)
          ))
        )

        val res = section.transactorPersonalDetailsRow.build(scheme)

        res.status mustBe TLCompleted
        res.url mustBe controllers.transactor.routes.PartOfOrganisationController.show.url
      }
      "be in progress when some required answers are missing" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(isTransactor = true)),
          applicantDetails = Some(ApplicantDetails(entity = Some(testLimitedCompany))),
          transactorDetails = Some(TransactorDetails(
            personalDetails = Some(testPersonalDetails),
            isPartOfOrganisation = Some(true)
          ))
        )

        val res = section.transactorPersonalDetailsRow.build(scheme)

        res.status mustBe TLInProgress
        res.url mustBe controllers.transactor.routes.PartOfOrganisationController.show.url
      }
    }
    "isPartOfOrganisation is false" must {
      "pass all checks for the same answers as an Organisation transactor" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(isTransactor = true)),
          applicantDetails = Some(ApplicantDetails(entity = Some(testLimitedCompany))),
          transactorDetails = Some(TransactorDetails(
            personalDetails = Some(testPersonalDetails),
            isPartOfOrganisation = Some(false),
            declarationCapacity = Some(DeclarationCapacityAnswer(AccountantAgent))
          ))
        )

        val res = section.transactorPersonalDetailsRow.build(scheme)

        res.status mustBe TLCompleted
        res.url mustBe controllers.transactor.routes.PartOfOrganisationController.show.url
      }
      "be in progress when some of the required answers are missing" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(isTransactor = true)),
          applicantDetails = Some(ApplicantDetails(entity = Some(testLimitedCompany))),
          transactorDetails = Some(TransactorDetails(
            personalDetails = Some(testPersonalDetails),
            isPartOfOrganisation = Some(false)
          ))
        )

        val res = section.transactorPersonalDetailsRow.build(scheme)

        res.status mustBe TLInProgress
        res.url mustBe controllers.transactor.routes.PartOfOrganisationController.show.url
      }
    }
  }

}
