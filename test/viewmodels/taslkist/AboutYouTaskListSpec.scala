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

import featureswitch.core.config.{FeatureSwitching, UseSoleTraderIdentification}
import fixtures.VatRegistrationFixture
import models.{ApplicantDetails, PartnerEntity}
import models.api._
import models.external.Name
import models.view.{FormerNameDateView, PreviousAddressView}
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.InternalServerException
import viewmodels.tasklist._

class AboutYouTaskListSpec extends VatRegSpec with VatRegistrationFixture with FeatureSwitching {

  val section = app.injector.instanceOf[AboutYouTaskList]
  implicit val profile = currentProfile

  val testPhoneNumber = "012345678"
  val testEmail = "test@test.com"

  "the personal details row" when {
    "the user is a NETP" must {
      "be not started if no required answers are present" in {
        val scheme = emptyVatScheme.copy(
          applicantDetails = Some(ApplicantDetails(entity = Some(testNetpSoleTrader))),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            isTransactor = false
          ))
        )

        val res = section.personalDetailsRow.build(scheme)

        res.status mustBe TLNotStarted
        res.url mustBe controllers.applicant.routes.FormerNameController.show.url
      }
      "be complete if the user anssers No to former name" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testNetpSoleTrader),
            hasFormerName = Some(false)
          ))
        )

        val res = section.personalDetailsRow.build(scheme)

        res.status mustBe TLCompleted
        res.url mustBe controllers.applicant.routes.FormerNameController.show.url
      }
      "be incomplete if the user answers Yes to former name and hasn't provided other answers" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testNetpSoleTrader),
            hasFormerName = Some(true)
          ))
        )

        val res = section.personalDetailsRow.build(scheme)

        res.status mustBe TLInProgress
        res.url mustBe controllers.applicant.routes.FormerNameController.show.url
      }
      "be complete if the user answers Yes to former name and has provided other answers" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testNetpSoleTrader),
            hasFormerName = Some(true),
            formerName = Some(Name(first = Some(testFirstName), last = testLastName)),
            formerNameDate = Some(FormerNameDateView(testDate))
          ))
        )

        val res = section.personalDetailsRow.build(scheme)

        res.status mustBe TLCompleted
        res.url mustBe controllers.applicant.routes.FormerNameController.show.url
      }
    }
    "the user is a sole trader" must {
      "be not started if no required answers are present" in {
        val scheme = emptyVatScheme.copy(
          applicantDetails = Some(ApplicantDetails(entity = Some(testSoleTrader))),
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = Individual,
            isTransactor = false
          ))
        )

        val res = section.personalDetailsRow.build(scheme)

        res.status mustBe TLNotStarted
        res.url mustBe controllers.applicant.routes.FormerNameController.show.url
      }
      "be complete if the user answers No to former name" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = Individual,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testSoleTrader),
            hasFormerName = Some(false)))
        )

        val res = section.personalDetailsRow.build(scheme)

        res.status mustBe TLCompleted
        res.url mustBe controllers.applicant.routes.FormerNameController.show.url
      }
      "be incomplete if the user answers Yes to former name and hasn't provided other answers" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = Individual,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testSoleTrader),
            hasFormerName = Some(true)
          ))
        )

        val res = section.personalDetailsRow.build(scheme)

        res.status mustBe TLInProgress
        res.url mustBe controllers.applicant.routes.FormerNameController.show.url
      }
      "be complete if the user answers Yes to former name and has provided other answers" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = Individual,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testSoleTrader),
            hasFormerName = Some(true),
            formerName = Some(Name(first = Some(testFirstName), last = testLastName)),
            formerNameDate = Some(FormerNameDateView(testDate))
          ))
        )

        val res = section.personalDetailsRow.build(scheme)

        res.status mustBe TLCompleted
        res.url mustBe controllers.applicant.routes.FormerNameController.show.url
      }
    }
    "the user is a partnership" when {
      "the lead partner is an 'individual' type, i.e. Sole Trader or NETP" must {
        "be complete when the former name answers are set" in {
          val scheme = emptyVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
              partyType = Partnership,
              isTransactor = false
            )),
            applicantDetails = Some(ApplicantDetails(
              entity = Some(testSoleTrader),
              personalDetails = Some(testPersonalDetails),
              roleInTheBusiness = testRole,
              hasFormerName = Some(true),
              formerName = Some(Name(first = Some(testFirstName), last = testLastName)),
              formerNameDate = Some(FormerNameDateView(testDate))
            )),
            partners = Some(List(PartnerEntity(testSoleTrader, Individual, isLeadPartner = true)))
          )

          val res = section.personalDetailsRow.build(scheme)

          res.status mustBe TLCompleted
          res.url mustBe controllers.applicant.routes.FormerNameController.show.url
        }
        "be not started when the former name answers are missing" in {
          val scheme = emptyVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
              partyType = Partnership,
              isTransactor = false
            )),
            applicantDetails = Some(ApplicantDetails(
              entity = Some(testSoleTrader),
              personalDetails = Some(testPersonalDetails)
            )),
            partners = Some(List(PartnerEntity(testSoleTrader, Individual, isLeadPartner = true)))
          )

          val res = section.personalDetailsRow.build(scheme)

          res.status mustBe TLNotStarted
          res.url mustBe controllers.applicant.routes.FormerNameController.show.url
        }
      }
      "the lead partner is anything else" must {
        "be complete when the former name answers are set" in {
          val scheme = emptyVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
              partyType = Partnership,
              isTransactor = false
            )),
            applicantDetails = Some(ApplicantDetails(
              entity = Some(testSoleTrader),
              personalDetails = Some(testPersonalDetails),
              roleInTheBusiness = testRole,
              hasFormerName = Some(true),
              formerName = Some(Name(first = Some(testFirstName), last = testLastName)),
              formerNameDate = Some(FormerNameDateView(testDate))
            )),
            partners = Some(List(PartnerEntity(testSoleTrader, UkCompany, isLeadPartner = true)))
          )

          val res = section.personalDetailsRow.build(scheme)

          res.status mustBe TLCompleted
          res.url mustBe controllers.applicant.routes.IndividualIdentificationController.startJourney.url
        }
        "be not started when the former name answers are missing" in {
          val scheme = emptyVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
              partyType = Partnership,
              isTransactor = false
            )),
            applicantDetails = Some(ApplicantDetails(
              entity = Some(testSoleTrader),
              personalDetails = Some(testPersonalDetails),
              hasFormerName = Some(false),
              roleInTheBusiness = testRole
            )),
            partners = Some(List(PartnerEntity(testSoleTrader, UkCompany, isLeadPartner = true)))
          )

          val res = section.personalDetailsRow.build(scheme)

          res.status mustBe TLCompleted
          res.url mustBe controllers.applicant.routes.IndividualIdentificationController.startJourney.url
        }
      }
    }
    "the user is an LLP" must {
      "be complete when personal details and former name are set" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = LtdLiabilityPartnership,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testSoleTrader),
            personalDetails = Some(testPersonalDetails),
            roleInTheBusiness = testRole,
            hasFormerName = Some(true),
            formerName = Some(Name(first = Some(testFirstName), last = testLastName)),
            formerNameDate = Some(FormerNameDateView(testDate))
          ))
        )
        val res = section.personalDetailsRow.build(scheme)
        res.status mustBe TLCompleted
        res.url mustBe controllers.applicant.routes.IndividualIdentificationController.startJourney.url
      }
      "be not started when personal details is missing" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = LtdLiabilityPartnership,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testSoleTrader),
            personalDetails = None
          ))
        )
        val res = section.personalDetailsRow.build(scheme)
        res.status mustBe TLNotStarted
        res.url mustBe controllers.applicant.routes.IndividualIdentificationController.startJourney.url
      }
    }
    "the user is a Uk Company" when {
      "the UseSoleTraderIdentification feature switch is enabled" must {
        "use the Sole Trader journey url" in {
          enable(UseSoleTraderIdentification)

          val scheme = emptyVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
              partyType = UkCompany,
              isTransactor = false
            )),
            applicantDetails = Some(ApplicantDetails(
              entity = Some(testSoleTrader),
              personalDetails = Some(testPersonalDetails),
              roleInTheBusiness = testRole,
              hasFormerName = Some(true),
              formerName = Some(Name(first = Some(testFirstName), last = testLastName)),
              formerNameDate = Some(FormerNameDateView(testDate))
            ))
          )
          val res = section.personalDetailsRow.build(scheme)
          res.status mustBe TLCompleted
          res.url mustBe controllers.applicant.routes.IndividualIdentificationController.startJourney.url
        }
      }
      "the UseSoleTraderIdentification feature switch is disabled" must {
        "use the PDV url" in {
          disable(UseSoleTraderIdentification)

          val scheme = emptyVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
              partyType = UkCompany,
              isTransactor = false
            )),
            applicantDetails = Some(ApplicantDetails(
              entity = Some(testSoleTrader),
              personalDetails = Some(testPersonalDetails),
              roleInTheBusiness = testRole,
              hasFormerName = Some(true),
              formerName = Some(Name(first = Some(testFirstName), last = testLastName)),
              formerNameDate = Some(FormerNameDateView(testDate))
            ))
          )
          val res = section.personalDetailsRow.build(scheme)
          res.status mustBe TLCompleted
          res.url mustBe controllers.applicant.routes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney().url
        }
      }
    }
    "the user is anything else" when {
      "must be completed if all answers have been provided" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = RegSociety,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testSoleTrader),
            personalDetails = Some(testPersonalDetails),
            roleInTheBusiness = testRole,
            hasFormerName = Some(true),
            formerName = Some(Name(first = Some(testFirstName), last = testLastName)),
            formerNameDate = Some(FormerNameDateView(testDate))
          ))
        )
        val res = section.personalDetailsRow.build(scheme)
        res.status mustBe TLCompleted
        res.url mustBe controllers.applicant.routes.IndividualIdentificationController.startJourney.url
      }
      "must be complete if the user doesn't have a former name and everything else has been provided" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = RegSociety,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testSoleTrader),
            personalDetails = Some(testPersonalDetails),
            roleInTheBusiness = testRole,
            hasFormerName = Some(false)
          ))
        )

        val res = section.personalDetailsRow.build(scheme)

        res.status mustBe TLCompleted
        res.url mustBe controllers.applicant.routes.IndividualIdentificationController.startJourney.url
      }
      "must be in progress if some required answers are missing" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = RegSociety,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testSoleTrader),
            personalDetails = Some(testPersonalDetails),
            roleInTheBusiness = testRole
          ))
        )

        val res = section.personalDetailsRow.build(scheme)

        res.status mustBe TLInProgress
        res.url mustBe controllers.applicant.routes.IndividualIdentificationController.startJourney.url
      }
      "must be not started if some all answers are missing" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = RegSociety,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(entity = Some(testSoleTrader)))
        )

        val res = section.personalDetailsRow.build(scheme)

        res.status mustBe TLNotStarted
        res.url mustBe controllers.applicant.routes.IndividualIdentificationController.startJourney.url
      }
    }
    "some or all pre-requisites are not met" must {
      "have the status Cannot Start Yet" in {
        val scheme = emptyVatScheme

        val res = section.personalDetailsRow.build(scheme)

        res.status mustBe TLCannotStart

      }
    }
  }

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

  "prerequisites for lead partner details" when {
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

  "address details task list row" when {
    "built" must {
      "throw INTERNAL_SERVER_ERROR if party type is missing" in {
        intercept[InternalServerException] {
          section.addressDetailsRow.build(emptyVatScheme)
        }
      }
    }
  }

  "prerequisites for address details" when {
    "complete" must {
      "return true" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          applicantDetails = Some(completeApplicantDetails)
        )
        section.addressDetailsRow.prerequisites(scheme).forall(_.isComplete(scheme)) mustBe true
      }
    }
    "not complete" must {
      "return false" in {
        val scheme = emptyVatScheme
        section.addressDetailsRow.prerequisites(scheme).forall(_.isComplete(scheme)) mustBe false
      }
    }
  }

  "checks for the address details row" when {
    "home address available and has been there for more than 3 years" must {
      "return true" in {
        section.addressDetailsRow.checks(
          validVatScheme.copy(
            applicantDetails = Some(completeApplicantDetails.copy(previousAddress = Some(PreviousAddressView(true, None))))
          )
        ) mustBe Seq(true)
      }
    }
    "home address less than 3 years and previous address available" must {
      "return true" in {
        section.addressDetailsRow.checks(validVatScheme) mustBe Seq(true, true)
      }
    }
    "home address less than 3 years and previous address not available" must {
      "return false" in {
        section.addressDetailsRow.checks(
          validVatScheme.copy(
            applicantDetails = Some(completeApplicantDetails.copy(previousAddress = Some(PreviousAddressView(false, None))))
          )
        ) mustBe Seq(true, false)
      }
    }
    "home address not available" must {
      "return false" in {
        section.addressDetailsRow.checks(
          validVatScheme.copy(
            applicantDetails = Some(completeApplicantDetails.copy(homeAddress = None))
          )
        ) mustBe Seq(false, true)
      }
    }
  }
}
