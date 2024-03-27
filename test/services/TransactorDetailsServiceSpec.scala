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

package services

import common.enums.VatRegStatus
import connectors.mocks.MockRegistrationApiConnector
import fixtures.ApplicantDetailsFixtures
import models._
import play.api.test.FakeRequest
import services.TransactorDetailsService.{OrganisationName, PartOfOrganisation, Telephone, TransactorEmail}
import testHelpers.VatRegSpec

class TransactorDetailsServiceSpec extends VatRegSpec with ApplicantDetailsFixtures with MockRegistrationApiConnector {
  override val testRegId = "testRegId"
  override implicit val currentProfile: CurrentProfile = CurrentProfile(testRegId, VatRegStatus.draft)
  implicit val request = FakeRequest()

  val testOrganisationName = "testOrganisationName"
  val testTelephone = "1234567890"
  val testEmail = "test@email.com"
  val testTransactorDetails: TransactorDetails = TransactorDetails(
    personalDetails = Some(testPersonalDetails),
    isPartOfOrganisation = Some(true),
    organisationName = Some(testOrganisationName),
    telephone = Some(testTelephone),
    email = Some(testEmail),
    emailVerified = Some(true),
    address = Some(testAddress),
    declarationCapacity = Some(DeclarationCapacityAnswer(AuthorisedEmployee))
  )

  val service = new TransactorDetailsService(
    mockRegistrationApiConnector
  )

  "Calling getTransactorDetails" must {
    "return a full TransactorDetails model" in {
      mockGetSection[TransactorDetails](testRegId, Some(testTransactorDetails))

      service.getTransactorDetails returns testTransactorDetails
    }
  }

  "Calling updateTransactorDetails" must {
    "return a TransactorDetails model if storing personalDetails" when {
      "the TransactorDetails model is incomplete" in {
        val incompleteTransactorDetails = TransactorDetails(personalDetails = Some(testPersonalDetails))
        mockGetSection[TransactorDetails](testRegId, None)
        mockReplaceSection[TransactorDetails](testRegId, incompleteTransactorDetails)

        service.saveTransactorDetails(testPersonalDetails) returns incompleteTransactorDetails
      }

      "the TransactorDetails model is complete" in {
        mockGetSection[TransactorDetails](testRegId, Some(testTransactorDetails))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails)

        service.saveTransactorDetails(testPersonalDetails) returns testTransactorDetails
      }

      "the TransactorDetails model is complete but user changed to an agent cred" in {
        val agentPersonalDetails = testPersonalDetails.copy(arn = Some("123"))
        val agentTransactorDetails = testTransactorDetails.copy(
          personalDetails = Some(agentPersonalDetails),
          organisationName = None,
          isPartOfOrganisation = None,
          address = None
        )
        mockGetSection[TransactorDetails](testRegId, Some(testTransactorDetails))
        mockReplaceSection[TransactorDetails](testRegId, agentTransactorDetails)

        service.saveTransactorDetails(agentPersonalDetails) returns agentTransactorDetails
      }
    }

    "return a TransactorDetails model if storing isPartOfOrganisation" when {
      "the TransactorDetails model is incomplete" in {
        val incompleteTransactorDetails = TransactorDetails(isPartOfOrganisation = Some(true))
        mockGetSection[TransactorDetails](testRegId, None)
        mockReplaceSection[TransactorDetails](testRegId, incompleteTransactorDetails)

        service.saveTransactorDetails(PartOfOrganisation(true)) returns incompleteTransactorDetails
      }

      "the TransactorDetails model is complete" in {
        mockGetSection[TransactorDetails](testRegId, Some(testTransactorDetails))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails)

        service.saveTransactorDetails(PartOfOrganisation(true)) returns testTransactorDetails
      }

      "the TransactorDetails model is complete but user stores false" in {
        mockGetSection[TransactorDetails](testRegId, Some(testTransactorDetails))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails.copy(isPartOfOrganisation = Some(false), organisationName = None))

        service.saveTransactorDetails(PartOfOrganisation(false)) returns testTransactorDetails.copy(isPartOfOrganisation = Some(false), organisationName = None)
      }
    }

    "return a TransactorDetails model if storing organisationName" when {
      "the TransactorDetails model is incomplete" in {
        val incompleteTransactorDetails = TransactorDetails(organisationName = Some(testOrganisationName))
        mockGetSection[TransactorDetails](testRegId, None)
        mockReplaceSection[TransactorDetails](testRegId, incompleteTransactorDetails)

        service.saveTransactorDetails(OrganisationName(testOrganisationName)) returns incompleteTransactorDetails
      }

      "the TransactorDetails model is complete" in {
        mockGetSection[TransactorDetails](testRegId, Some(testTransactorDetails))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails)

        service.saveTransactorDetails(OrganisationName(testOrganisationName)) returns testTransactorDetails
      }
    }

    "return a TransactorDetails model if storing telephone" when {
      "the TransactorDetails model is incomplete" in {
        val incompleteTransactorDetails = TransactorDetails(telephone = Some(testTelephone))
        mockGetSection[TransactorDetails](testRegId, None)
        mockReplaceSection[TransactorDetails](testRegId, incompleteTransactorDetails)

        service.saveTransactorDetails(Telephone(testTelephone)) returns incompleteTransactorDetails
      }

      "the TransactorDetails model is complete" in {
        mockGetSection[TransactorDetails](testRegId, Some(testTransactorDetails))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails)

        service.saveTransactorDetails(Telephone(testTelephone)) returns testTransactorDetails
      }
    }

    "return a TransactorDetails model if storing email" when {
      "the TransactorDetails model is incomplete" in {
        val incompleteTransactorDetails = TransactorDetails(email = Some(testEmail))
        mockGetSection[TransactorDetails](testRegId, None)
        mockReplaceSection[TransactorDetails](testRegId, incompleteTransactorDetails)

        service.saveTransactorDetails(TransactorEmail(testEmail)) returns incompleteTransactorDetails
      }

      "the TransactorDetails model is complete" in {
        mockGetSection[TransactorDetails](testRegId, Some(testTransactorDetails))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails)

        service.saveTransactorDetails(TransactorEmail(testEmail)) returns testTransactorDetails
      }
    }

    "return a TransactorDetails model if storing address" when {
      "the TransactorDetails model is incomplete" in {
        val incompleteTransactorDetails = TransactorDetails(address = Some(testAddress))
        mockGetSection[TransactorDetails](testRegId, None)
        mockReplaceSection[TransactorDetails](testRegId, incompleteTransactorDetails)

        service.saveTransactorDetails(testAddress) returns incompleteTransactorDetails
      }

      "the TransactorDetails model is complete" in {
        mockGetSection[TransactorDetails](testRegId, Some(testTransactorDetails))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails)

        service.saveTransactorDetails(testAddress) returns testTransactorDetails
      }
    }

    "return a TransactorDetails model if storing declarationCapacity" when {
      "the TransactorDetails model is incomplete" in {
        val incompleteTransactorDetails = TransactorDetails(declarationCapacity = Some(DeclarationCapacityAnswer(AuthorisedEmployee)))
        mockGetSection[TransactorDetails](testRegId, None)
        mockReplaceSection[TransactorDetails](testRegId, incompleteTransactorDetails)

        service.saveTransactorDetails(DeclarationCapacityAnswer(AuthorisedEmployee)) returns incompleteTransactorDetails
      }

      "the TransactorDetails model is complete" in {
        mockGetSection[TransactorDetails](testRegId, Some(testTransactorDetails))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails)

        service.saveTransactorDetails(DeclarationCapacityAnswer(AuthorisedEmployee)) returns testTransactorDetails
      }
    }
  }
}
