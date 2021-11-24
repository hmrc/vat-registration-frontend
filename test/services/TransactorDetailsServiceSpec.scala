/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.mocks.{MockRegistrationApiConnector, MockS4lConnector}
import fixtures.ApplicantDetailsFixtures
import models._
import org.mockito.Mockito.when
import services.TransactorDetailsService.{OrganisationName, PartOfOrganisation, Telephone, TransactorEmail}
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class TransactorDetailsServiceSpec extends VatRegSpec with ApplicantDetailsFixtures with MockS4lConnector with MockRegistrationApiConnector {
  override val testRegId = "testRegId"
  override implicit val currentProfile = CurrentProfile(testRegId, VatRegStatus.draft)

  val testOrganisationName = "testOrganisationName"
  val testTelephone = "1234567890"
  val testEmail = "test@email.com"
  val testTransactorDetails = TransactorDetails(
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
    mockS4LService,
    mockRegistrationApiConnector
  )

  "Calling getTransactorDetails" should {
    "return a full TransactorDetails model from s4l" in {
      when(mockS4LService.fetchAndGet[TransactorDetails])
        .thenReturn(Future.successful(Some(testTransactorDetails)))

      service.getTransactorDetails returns testTransactorDetails
    }
  }

  "Calling updateTransactorDetails" should {
    "return a TransactorDetails model if storing personalDetails" when {
      "the TransactorDetails model is incomplete" in {
        val incompleteTransactorDetails = TransactorDetails(personalDetails = Some(testPersonalDetails))
        when(mockS4LService.fetchAndGet[TransactorDetails])
          .thenReturn(Future.successful(None))
        mockGetSection[TransactorDetails](testRegId, None)
        when(mockS4LService.save[TransactorDetails](incompleteTransactorDetails))
          .thenReturn(Future.successful(CacheMap("", Map())))

        service.saveTransactorDetails(testPersonalDetails) returns incompleteTransactorDetails
      }

      "the TransactorDetails model is complete" in {
        when(mockS4LService.fetchAndGet[TransactorDetails])
          .thenReturn(Future.successful(Some(testTransactorDetails)))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails)
        when(mockS4LService.clearKey[TransactorDetails])
          .thenReturn(Future.successful(CacheMap("", Map())))

        service.saveTransactorDetails(testPersonalDetails) returns testTransactorDetails
      }
    }

    "return a TransactorDetails model if storing isPartOfOrganisation" when {
      "the TransactorDetails model is incomplete" in {
        val incompleteTransactorDetails = TransactorDetails(isPartOfOrganisation = Some(true))
        when(mockS4LService.fetchAndGet[TransactorDetails])
          .thenReturn(Future.successful(None))
        mockGetSection[TransactorDetails](testRegId, None)
        when(mockS4LService.save[TransactorDetails](incompleteTransactorDetails))
          .thenReturn(Future.successful(CacheMap("", Map())))

        service.saveTransactorDetails(PartOfOrganisation(true)) returns incompleteTransactorDetails
      }

      "the TransactorDetails model is complete" in {
        when(mockS4LService.fetchAndGet[TransactorDetails])
          .thenReturn(Future.successful(Some(testTransactorDetails)))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails)
        when(mockS4LService.clearKey[TransactorDetails])
          .thenReturn(Future.successful(CacheMap("", Map())))

        service.saveTransactorDetails(PartOfOrganisation(true)) returns testTransactorDetails
      }
    }

    "return a TransactorDetails model if storing organisationName" when {
      "the TransactorDetails model is incomplete" in {
        val incompleteTransactorDetails = TransactorDetails(organisationName = Some(testOrganisationName))
        when(mockS4LService.fetchAndGet[TransactorDetails])
          .thenReturn(Future.successful(None))
        mockGetSection[TransactorDetails](testRegId, None)
        when(mockS4LService.save[TransactorDetails](incompleteTransactorDetails))
          .thenReturn(Future.successful(CacheMap("", Map())))

        service.saveTransactorDetails(OrganisationName(testOrganisationName)) returns incompleteTransactorDetails
      }

      "the TransactorDetails model is complete" in {
        when(mockS4LService.fetchAndGet[TransactorDetails])
          .thenReturn(Future.successful(Some(testTransactorDetails)))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails)
        when(mockS4LService.clearKey[TransactorDetails])
          .thenReturn(Future.successful(CacheMap("", Map())))

        service.saveTransactorDetails(OrganisationName(testOrganisationName)) returns testTransactorDetails
      }
    }

    "return a TransactorDetails model if storing telephone" when {
      "the TransactorDetails model is incomplete" in {
        val incompleteTransactorDetails = TransactorDetails(telephone = Some(testTelephone))
        when(mockS4LService.fetchAndGet[TransactorDetails])
          .thenReturn(Future.successful(None))
        mockGetSection[TransactorDetails](testRegId, None)
        when(mockS4LService.save[TransactorDetails](incompleteTransactorDetails))
          .thenReturn(Future.successful(CacheMap("", Map())))

        service.saveTransactorDetails(Telephone(testTelephone)) returns incompleteTransactorDetails
      }

      "the TransactorDetails model is complete" in {
        when(mockS4LService.fetchAndGet[TransactorDetails])
          .thenReturn(Future.successful(Some(testTransactorDetails)))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails)
        when(mockS4LService.clearKey[TransactorDetails])
          .thenReturn(Future.successful(CacheMap("", Map())))

        service.saveTransactorDetails(Telephone(testTelephone)) returns testTransactorDetails
      }
    }

    "return a TransactorDetails model if storing email" when {
      "the TransactorDetails model is incomplete" in {
        val incompleteTransactorDetails = TransactorDetails(email = Some(testEmail))
        when(mockS4LService.fetchAndGet[TransactorDetails])
          .thenReturn(Future.successful(None))
        mockGetSection[TransactorDetails](testRegId, None)
        when(mockS4LService.save[TransactorDetails](incompleteTransactorDetails))
          .thenReturn(Future.successful(CacheMap("", Map())))

        service.saveTransactorDetails(TransactorEmail(testEmail)) returns incompleteTransactorDetails
      }

      "the TransactorDetails model is complete" in {
        when(mockS4LService.fetchAndGet[TransactorDetails])
          .thenReturn(Future.successful(Some(testTransactorDetails)))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails)
        when(mockS4LService.clearKey[TransactorDetails])
          .thenReturn(Future.successful(CacheMap("", Map())))

        service.saveTransactorDetails(TransactorEmail(testEmail)) returns testTransactorDetails
      }
    }

    "return a TransactorDetails model if storing address" when {
      "the TransactorDetails model is incomplete" in {
        val incompleteTransactorDetails = TransactorDetails(address = Some(testAddress))
        when(mockS4LService.fetchAndGet[TransactorDetails])
          .thenReturn(Future.successful(None))
        mockGetSection[TransactorDetails](testRegId, None)
        when(mockS4LService.save[TransactorDetails](incompleteTransactorDetails))
          .thenReturn(Future.successful(CacheMap("", Map())))

        service.saveTransactorDetails(testAddress) returns incompleteTransactorDetails
      }

      "the TransactorDetails model is complete" in {
        when(mockS4LService.fetchAndGet[TransactorDetails])
          .thenReturn(Future.successful(Some(testTransactorDetails)))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails)
        when(mockS4LService.clearKey[TransactorDetails])
          .thenReturn(Future.successful(CacheMap("", Map())))

        service.saveTransactorDetails(testAddress) returns testTransactorDetails
      }
    }

    "return a TransactorDetails model if storing declarationCapacity" when {
      "the TransactorDetails model is incomplete" in {
        val incompleteTransactorDetails = TransactorDetails(declarationCapacity = Some(DeclarationCapacityAnswer(AuthorisedEmployee)))
        when(mockS4LService.fetchAndGet[TransactorDetails])
          .thenReturn(Future.successful(None))
        mockGetSection[TransactorDetails](testRegId, None)
        when(mockS4LService.save[TransactorDetails](incompleteTransactorDetails))
          .thenReturn(Future.successful(CacheMap("", Map())))

        service.saveTransactorDetails(DeclarationCapacityAnswer(AuthorisedEmployee)) returns incompleteTransactorDetails
      }

      "the TransactorDetails model is complete" in {
        when(mockS4LService.fetchAndGet[TransactorDetails])
          .thenReturn(Future.successful(Some(testTransactorDetails)))
        mockReplaceSection[TransactorDetails](testRegId, testTransactorDetails)
        when(mockS4LService.clearKey[TransactorDetails])
          .thenReturn(Future.successful(CacheMap("", Map())))

        service.saveTransactorDetails(DeclarationCapacityAnswer(AuthorisedEmployee)) returns testTransactorDetails
      }
    }
  }
}
