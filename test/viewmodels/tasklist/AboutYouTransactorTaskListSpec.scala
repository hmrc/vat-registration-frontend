/*
 * Copyright 2023 HM Revenue & Customs
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

import featureswitch.core.config.FeatureSwitching
import fixtures.VatRegistrationFixture
import models._
import models.api._
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.InternalServerException

class AboutYouTransactorTaskListSpec extends VatRegSpec with VatRegistrationFixture with FeatureSwitching {

  val section: AboutYouTransactorTaskList = app.injector.instanceOf[AboutYouTransactorTaskList]
  val testArn = "testArn"

  "the user has logged in as an Agent" must {
    "pass all checks when the personal details section exists" in {
      implicit val cp: CurrentProfile = currentProfile.copy(agentReferenceNumber = Some(testArn))

      val scheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(isTransactor = true)),
        applicantDetails = Some(ApplicantDetails(entity = Some(testLimitedCompany))),
        transactorDetails = Some(TransactorDetails(
          personalDetails = Some(testPersonalDetails),
          declarationCapacity = Some(DeclarationCapacityAnswer(AccountantAgent))
        ))
      )

      val res = section.transactorPersonalDetailsRow.build(scheme)

      res.status mustBe TLCompleted
      res.url mustBe controllers.transactor.routes.AgentNameController.show.url
    }
    "be Not Started when personal details is missing" in {
      implicit val cp: CurrentProfile = currentProfile.copy(agentReferenceNumber = Some(testArn))

      val scheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(isTransactor = true)),
        applicantDetails = Some(ApplicantDetails(entity = Some(testLimitedCompany)))
      )

      val res = section.transactorPersonalDetailsRow.build(scheme)

      res.status mustBe TLNotStarted
      res.url mustBe controllers.transactor.routes.AgentNameController.show.url
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
            organisationName = Some(testCompanyName),
            declarationCapacity = Some(DeclarationCapacityAnswer(AccountantAgent))
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

  "checks for transactor address details task list row" when {

    "party type not available" must {
      "throw INTERNAL_SERVER_ERROR if party type is missing" in {
        intercept[InternalServerException] {
          section.transactorAddressDetailsRow.build(emptyVatScheme)
        }
      }
    }

    "person details not available or incomplete from prerequisite" must {
      "return TLCannotStart" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = RegSociety,
            isTransactor = true
          ))
        )

        val sectionRow = section.transactorAddressDetailsRow.build(scheme)
        sectionRow.status mustBe TLCannotStart
      }
    }

    "all person details are available from prerequisite but address capture hasn't started" must {
      "return TLNotStarted" in {

        def verifySectionRowUrl(partyType: PartyType, url: String) = {
          val scheme = emptyVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
              partyType = partyType,
              isTransactor = true
            )),
            transactorDetails = Some(validTransactorDetails.copy(address = None))
          )

          val sectionRow = section.transactorAddressDetailsRow.build(scheme)
          sectionRow.status mustBe TLNotStarted
          sectionRow.url mustBe url
        }

        verifySectionRowUrl(RegSociety, controllers.transactor.routes.TransactorHomeAddressController.redirectToAlf.url)
        verifySectionRowUrl(NonUkNonEstablished, controllers.transactor.routes.TransactorInternationalAddressController.show.url)
      }
    }

    "all person details are available from prerequisite and address details captured with just arn" must {
      "return TLCompleted" in {

        def verifySectionRowUrl(partyType: PartyType, url: String) = {
          val scheme = emptyVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
              partyType = partyType,
              isTransactor = true
            )),
            transactorDetails = Some(validTransactorDetails.copy(
              address = None,
              personalDetails = Some(testPersonalDetails.copy(arn = Some("arn")))
            ))
          )

          val sectionRow = section.transactorAddressDetailsRow.build(scheme)
          sectionRow.status mustBe TLCompleted
          sectionRow.url mustBe url
        }

        verifySectionRowUrl(RegSociety, controllers.transactor.routes.TransactorHomeAddressController.redirectToAlf.url)
        verifySectionRowUrl(NonUkNonEstablished, controllers.transactor.routes.TransactorInternationalAddressController.show.url)
      }
    }

    "all person details are available from prerequisite and address details captured with no arn but address available" must {
      "return TLCompleted" in {

        def verifySectionRowUrl(partyType: PartyType, url: String) = {
          val scheme = emptyVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
              partyType = partyType,
              isTransactor = true
            )),
            transactorDetails = Some(validTransactorDetails)
          )

          val sectionRow = section.transactorAddressDetailsRow.build(scheme)
          sectionRow.status mustBe TLCompleted
          sectionRow.url mustBe url
        }

        verifySectionRowUrl(RegSociety, controllers.transactor.routes.TransactorHomeAddressController.redirectToAlf.url)
        verifySectionRowUrl(NonUkNonEstablished, controllers.transactor.routes.TransactorInternationalAddressController.show.url)
      }
    }
  }

  "checks for transactor contact details task list row" when {
    "address details not available or incomplete from prerequisite" must {
      "return TLCannotStart" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = RegSociety,
            isTransactor = false
          ))
        )

        val sectionRow = section.transactorContactDetailsRow.build(scheme)
        sectionRow.status mustBe TLCannotStart
      }
    }

    "address details available from prerequisite but contact details capture hasn't started" must {
      "return TLNotStarted" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = LtdPartnership,
            isTransactor = true
          )),
          transactorDetails = Some(validTransactorDetails.copy(email = None, emailVerified = None, telephone = None))
        )

        val sectionRow = section.transactorContactDetailsRow.build(scheme)
        sectionRow.status mustBe TLNotStarted
        sectionRow.url mustBe controllers.transactor.routes.TelephoneNumberController.show.url
      }
    }

    "address details available from prerequisite but contact details still in progress" must {
      "return TLInProgress" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = Partnership,
            isTransactor = false
          )),
          transactorDetails = Some(validTransactorDetails.copy(email = None, emailVerified = Some(true)))
        )

        val sectionRow = section.transactorContactDetailsRow.build(scheme)
        sectionRow.status mustBe TLInProgress
        sectionRow.url mustBe controllers.transactor.routes.TelephoneNumberController.show.url
      }
    }

    "address details are available from prerequisite and contact details captured" must {
      "return TLCompleted" in {

        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = LtdPartnership,
            isTransactor = false
          )),
          transactorDetails = Some(validTransactorDetails)
        )

        val sectionRow = section.transactorContactDetailsRow.build(scheme)
        sectionRow.status mustBe TLCompleted
        sectionRow.url mustBe controllers.transactor.routes.TelephoneNumberController.show.url
      }
    }
  }
}
