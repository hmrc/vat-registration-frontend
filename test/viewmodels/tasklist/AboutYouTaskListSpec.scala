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

import featureswitch.core.config.FeatureSwitching
import fixtures.VatRegistrationFixture
import models.api._
import models.external.Name
import models._
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.InternalServerException

class AboutYouTaskListSpec extends VatRegSpec with VatRegistrationFixture with FeatureSwitching {

  val section: AboutYouTaskList = app.injector.instanceOf[AboutYouTaskList]
  implicit val profile: CurrentProfile = currentProfile

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
      "be complete if the user answers No to former name" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = NETP,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testNetpSoleTrader),
            changeOfName = FormerName(
              hasFormerName = Some(false)
            )
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
            changeOfName = FormerName(
              hasFormerName = Some(true)
            )
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
            changeOfName = FormerName(
              hasFormerName = Some(true),
              name = Some(Name(first = Some(testFirstName), last = testLastName)),
              change = Some(testDate)
            )
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
            changeOfName = FormerName(
              hasFormerName = Some(false)
            )))
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
            changeOfName = FormerName(
              hasFormerName = Some(true)
            )
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
            changeOfName = FormerName(
              hasFormerName = Some(true),
              name = Some(Name(first = Some(testFirstName), last = testLastName)),
              change = Some(testDate)
            )
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
              changeOfName = FormerName(
                hasFormerName = Some(true),
                name = Some(Name(first = Some(testFirstName), last = testLastName)),
                change = Some(testDate)
              )
            )),
            entities = Some(List(Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None)))
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
            entities = Some(List(Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None)))
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
              changeOfName = FormerName(
                hasFormerName = Some(true),
                name = Some(Name(first = Some(testFirstName), last = testLastName)),
                change = Some(testDate)
              )
            )),
            entities = Some(List(Entity(Some(testSoleTrader), UkCompany, Some(true), None, None, None, None)))
          )

          val res = section.personalDetailsRow.build(scheme)

          res.status mustBe TLCompleted
          res.url mustBe controllers.grs.routes.IndividualIdController.startJourney.url
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
              changeOfName = FormerName(
                hasFormerName = Some(false)
              ),
              roleInTheBusiness = testRole
            )),
            entities = Some(List(Entity(Some(testSoleTrader), UkCompany, Some(true), None, None, None, None)))
          )

          val res = section.personalDetailsRow.build(scheme)

          res.status mustBe TLCompleted
          res.url mustBe controllers.grs.routes.IndividualIdController.startJourney.url
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
            changeOfName = FormerName(
              hasFormerName = Some(true),
              name = Some(Name(first = Some(testFirstName), last = testLastName)),
              change = Some(testDate)
            )
          ))
        )
        val res = section.personalDetailsRow.build(scheme)
        res.status mustBe TLCompleted
        res.url mustBe controllers.grs.routes.IndividualIdController.startJourney.url
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
        res.url mustBe controllers.grs.routes.IndividualIdController.startJourney.url
      }
      "be not started when personal details is missing but roleInTheBusiness available as Partner" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = LtdPartnership,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testSoleTrader),
            personalDetails = None,
            roleInTheBusiness = testRole
          )),
          entities = Some(List(Entity(Some(testSoleTrader), Partnership, Some(true), None, None, None, None)))
        )
        val res = section.personalDetailsRow.build(scheme)
        res.status mustBe TLNotStarted
        res.url mustBe controllers.grs.routes.IndividualIdController.startJourney.url
      }
    }
    "the user is a Uk Company" when {
      "use the Sole Trader journey url" in {

        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = UkCompany,
            isTransactor = false
          )),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testSoleTrader),
            personalDetails = Some(testPersonalDetails),
            roleInTheBusiness = testRole,
            changeOfName = FormerName(
              hasFormerName = Some(true),
              name = Some(Name(first = Some(testFirstName), last = testLastName)),
              change = Some(testDate)
            )
          ))
        )
        val res = section.personalDetailsRow.build(scheme)
        res.status mustBe TLCompleted
        res.url mustBe controllers.grs.routes.IndividualIdController.startJourney.url
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
            changeOfName = FormerName(
              hasFormerName = Some(true),
              name = Some(Name(first = Some(testFirstName), last = testLastName)),
              change = Some(testDate)
            )
          ))
        )
        val res = section.personalDetailsRow.build(scheme)
        res.status mustBe TLCompleted
        res.url mustBe controllers.grs.routes.IndividualIdController.startJourney.url
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
            changeOfName = FormerName(
              hasFormerName = Some(false)
            )
          ))
        )

        val res = section.personalDetailsRow.build(scheme)

        res.status mustBe TLCompleted
        res.url mustBe controllers.grs.routes.IndividualIdController.startJourney.url
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
        res.url mustBe controllers.grs.routes.IndividualIdController.startJourney.url
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
        res.url mustBe controllers.grs.routes.IndividualIdController.startJourney.url
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
    "party type is available and applicable for lead partner section" must {
      "return TLCompleted  state if prerequisites met" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = LtdPartnership)),
          applicantDetails = Some(completeApplicantDetails),
          entities = Some(List(Entity(Some(testSoleTrader), Partnership, Some(true), None, None, None, None)))
        )
        val sectionRow = section.buildLeadPartnerRow(scheme).get
        sectionRow.status mustBe TLCompleted
        sectionRow.url mustBe controllers.applicant.routes.LeadPartnerEntityController.showLeadPartnerEntityType.url
      }

      "return TLInProgress state if prerequisites met but lead partner not complete" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = LtdPartnership)),
          applicantDetails = Some(completeApplicantDetails),
          entities = Some(List(Entity(None, Partnership, Some(true), None, None, None, None)))
        )
        val sectionRow = section.buildLeadPartnerRow(scheme).get
        sectionRow.status mustBe TLInProgress
        sectionRow.url mustBe controllers.applicant.routes.LeadPartnerEntityController.showLeadPartnerEntityType.url
      }

      "return TLNotStarted state if prerequisites met but lead partner not selected" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = LtdPartnership)),
          applicantDetails = Some(completeApplicantDetails)
        )
        val sectionRow = section.buildLeadPartnerRow(scheme).get
        sectionRow.status mustBe TLNotStarted
        sectionRow.url mustBe controllers.applicant.routes.LeadPartnerEntityController.showLeadPartnerEntityType.url
      }

      "return not TLCannotStart state if prerequisites not met" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(partyType = LtdPartnership))
        )
        val sectionRow = section.buildLeadPartnerRow(scheme).get
        sectionRow.status mustBe TLCannotStart
        sectionRow.url mustBe controllers.applicant.routes.LeadPartnerEntityController.showLeadPartnerEntityType.url
      }
    }

    "party type is not available" must {
      "return false" in {
        section.buildLeadPartnerRow(emptyVatScheme) mustBe None
      }
    }

    "party type is available and not a lead partner" must {
      "return true" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData)
        )
        section.buildLeadPartnerRow(scheme) mustBe None
      }
    }
  }

  "checks for address details task list row" when {

    "party type not available" must {
      "throw INTERNAL_SERVER_ERROR if party type is missing" in {
        intercept[InternalServerException] {
          section.addressDetailsRow.build(emptyVatScheme)
        }
      }
    }

    "person details not available or incomplete from prerequisite" must {
      "return TLCannotStart" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = RegSociety,
            isTransactor = false
          ))
        )

        val sectionRow = section.addressDetailsRow.build(scheme)
        sectionRow.status mustBe TLCannotStart
      }
    }

    "all person details are available from prerequisite but address capture hasn't started" must {
      "return TLNotStarted" in {

        def verifySectionRowUrl(partyType: PartyType, url: String) = {
          val scheme = emptyVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
              partyType = partyType,
              isTransactor = false
            )),
            applicantDetails = Some(ApplicantDetails(
              entity = Some(testSoleTrader),
              personalDetails = Some(testPersonalDetails),
              roleInTheBusiness = testRole,
              changeOfName = FormerName(
                hasFormerName = Some(true),
                name = Some(Name(first = Some(testFirstName), last = testLastName)),
                change = Some(testDate)
              )
            ))
          )

          val sectionRow = section.addressDetailsRow.build(scheme)
          sectionRow.status mustBe TLNotStarted
          sectionRow.url mustBe url
        }

        verifySectionRowUrl(RegSociety, controllers.applicant.routes.HomeAddressController.redirectToAlf.url)
        verifySectionRowUrl(NonUkNonEstablished, controllers.applicant.routes.InternationalHomeAddressController.show.url)
      }
    }

    "all person details are available from prerequisite with partial address details captured" must {
      "return TLInProgress" in {

        def verifySectionRowUrl(partyType: PartyType, url: String) = {
          val scheme = emptyVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
              partyType = partyType,
              isTransactor = false
            )),
            applicantDetails = Some(ApplicantDetails(
              entity = Some(testSoleTrader),
              personalDetails = Some(testPersonalDetails),
              roleInTheBusiness = testRole,
              changeOfName = FormerName(
                hasFormerName = Some(true),
                name = Some(Name(first = Some(testFirstName), last = testLastName)),
                change = Some(testDate)
              ),
              currentAddress = completeApplicantDetails.currentAddress
            ))
          )

          val sectionRow = section.addressDetailsRow.build(scheme)
          sectionRow.status mustBe TLInProgress
          sectionRow.url mustBe url
        }

        verifySectionRowUrl(RegSociety, controllers.applicant.routes.HomeAddressController.redirectToAlf.url)
        verifySectionRowUrl(NonUkNonEstablished, controllers.applicant.routes.InternationalHomeAddressController.show.url)
      }
    }

    "all person details are available from prerequisite and all address details captured" must {
      "return TLCompleted" in {

        def verifySectionRowUrl(partyType: PartyType, url: String) = {
          val scheme = emptyVatScheme.copy(
            eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
              partyType = partyType,
              isTransactor = false
            )),
            applicantDetails = Some(ApplicantDetails(
              entity = Some(testSoleTrader),
              personalDetails = Some(testPersonalDetails),
              roleInTheBusiness = testRole,
              changeOfName = FormerName(
                hasFormerName = Some(true),
                name = Some(Name(first = Some(testFirstName), last = testLastName)),
                change = Some(testDate)
              ),
              currentAddress = completeApplicantDetails.currentAddress,
              noPreviousAddress = Some(true),
              previousAddress = None
            ))
          )

          val sectionRow = section.addressDetailsRow.build(scheme)
          sectionRow.status mustBe TLCompleted
          sectionRow.url mustBe url
        }

        verifySectionRowUrl(RegSociety, controllers.applicant.routes.HomeAddressController.redirectToAlf.url)
        verifySectionRowUrl(NonUkNonEstablished, controllers.applicant.routes.InternationalHomeAddressController.show.url)
      }
    }
  }

  "checks for contact details task list row" when {
    "address details not available or incomplete from prerequisite" must {
      "return TLCannotStart" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
            partyType = RegSociety,
            isTransactor = false
          ))
        )

        val sectionRow = section.contactDetailsRow.build(scheme)
        sectionRow.status mustBe TLCannotStart
      }
    }

    "address details available from prerequisite but contact details capture hasn't started" must {
      "return TLNotStarted" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testSoleTrader),
            personalDetails = Some(testPersonalDetails),
            roleInTheBusiness = testRole,
            changeOfName = FormerName(
              hasFormerName = Some(true),
              name = Some(Name(first = Some(testFirstName), last = testLastName)),
              change = Some(testDate)
            ),
            currentAddress = completeApplicantDetails.currentAddress,
            noPreviousAddress = Some(true),
            previousAddress = None
          ))
        )

        val sectionRow = section.contactDetailsRow.build(scheme)
        sectionRow.status mustBe TLNotStarted
        sectionRow.url mustBe controllers.applicant.routes.CaptureEmailAddressController.show.url
      }
    }

    "address details available from prerequisite but contact details still in progress" must {
      "return TLInProgress" in {
        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          applicantDetails = Some(ApplicantDetails(
            entity = Some(testSoleTrader),
            personalDetails = Some(testPersonalDetails),
            roleInTheBusiness = testRole,
            changeOfName = FormerName(
              hasFormerName = Some(true),
              name = Some(Name(first = Some(testFirstName), last = testLastName)),
              change = Some(testDate)
            ),
            currentAddress = completeApplicantDetails.currentAddress,
            noPreviousAddress = Some(true),
            previousAddress = None,
            contact = Contact(
              email = Some("email")
            )
          ))
        )

        val sectionRow = section.contactDetailsRow.build(scheme)
        sectionRow.status mustBe TLInProgress
        sectionRow.url mustBe controllers.applicant.routes.CaptureEmailAddressController.show.url
      }
    }

    "address details are available from prerequisite and contact details captured" must {
      "return TLCompleted" in {

        val scheme = emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData),
          applicantDetails = Some(completeApplicantDetails)
        )

        val sectionRow = section.contactDetailsRow.build(scheme)
        sectionRow.status mustBe TLCompleted
        sectionRow.url mustBe controllers.applicant.routes.CaptureEmailAddressController.show.url
      }
    }
  }
}
