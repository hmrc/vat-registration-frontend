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

package services

import _root_.models.api.Address
import config.FrontendAppConfig
import connectors.mocks.MockRegistrationApiConnector
import models._
import org.mockito.Mockito._
import org.mockito.{ArgumentMatchers => matchers}
import play.api.libs.json.Json
import services.BusinessContactService.{HasWebsiteAnswer, Website, Email => BusinessContactEmail, TelephoneNumber => BusinessContactTelephoneNumber}
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class BusinessContactServiceSpec extends VatRegSpec with MockRegistrationApiConnector {

  val testService: BusinessContactService = new BusinessContactService(
    mockRegistrationApiConnector,
    mockS4LService
  )

  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val dummyCacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson("")))

  "getBusinessContact" should {
    "return a populated BusinessContact model" when {
      "there is a model in S4L" in {
        val businessContact = BusinessContact(
          ppobAddress = Some(testAddress),
          companyContactDetails = Some(CompanyContactDetails("test@test.com", None, None, None)),
          contactPreference = Some(Letter)
        )

        when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
          .thenReturn(Future.successful(Some(businessContact)))

        val result = await(testService.getBusinessContact)
        result mustBe businessContact
      }

      "there is data in the backend" in {
        val businessContact = BusinessContact(
          ppobAddress = Some(Address(
            line1 = "testLine1",
            line2 = Some("testLine2"),
            line3 = Some("testLine3"),
            line4 = Some("testLine4"),
            postcode = Some("TE57 7ET"),
            addressValidated = true
          )),
          companyContactDetails = Some(CompanyContactDetails(
            email = "test@test.com",
            phoneNumber = Some("1234567890"),
            mobileNumber = Some("9876545678"),
            website = Some("/test/url")
          )),
          contactPreference = Some(Email)
        )

        when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
          .thenReturn(Future.successful(None))

        mockGetSection[BusinessContact](testRegId, Some(businessContact))

        when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
          .thenReturn(Future.successful(dummyCacheMap))

        val result = await(testService.getBusinessContact)
        result mustBe businessContact
      }
    }

    "return an empty model" when {
      "there is no data in either S4L or the backend" in {
        val businessContact = BusinessContact(
          ppobAddress = None,
          companyContactDetails = None,
          contactPreference = None
        )

        when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
          .thenReturn(Future.successful(None))

        mockGetSection[BusinessContact](testRegId, None)

        when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
          .thenReturn(Future.successful(dummyCacheMap))

        val result = await(testService.getBusinessContact)
        result mustBe businessContact
      }
    }
  }

  "updateBusinessContact" should {
    "determine that the model is incomplete and save in S4L - nothing pre-populated, update ppobAddress" in {
      val businessContact = BusinessContact(
        ppobAddress = None,
        companyContactDetails = None,
        contactPreference = None
      )

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockGetSection(testRegId, None)

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[Address](testAddress))
      result mustBe testAddress
    }

    "determine that the model is incomplete and save in S4L - nothing pre-populated, update companyContactDetails" in {
      val businessContact = BusinessContact(
        ppobAddress = None,
        companyContactDetails = None,
        contactPreference = None
      )

      val companyContactDetails = CompanyContactDetails("test@test.com", None, None, None)

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockGetSection(testRegId, None)

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[CompanyContactDetails](companyContactDetails))
      result mustBe companyContactDetails
    }

    "determine that the model is incomplete and save in S4L - nothing pre-populated, update contactPreference" in {
      val businessContact = BusinessContact(
        ppobAddress = None,
        companyContactDetails = None,
        contactPreference = None
      )

      val contactPreference = Email

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockGetSection(testRegId, None)

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[ContactPreference](contactPreference))
      result mustBe contactPreference
    }

    "determine that the model is incomplete and save in S4L - nothing pre-populated, update email" in {
      val businessContact = BusinessContact(
        ppobAddress = None,
        email = None,
        telephoneNumber = None,
        hasWebsite = None,
        website = None,
        companyContactDetails = None,
        contactPreference = None
      )

      val email = BusinessContactEmail("test@test.com")

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockGetSection(testRegId, None)

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[BusinessContactEmail](email))
      result mustBe email
    }

    "determine that the model is incomplete and save in S4L - nothing pre-populated, update telephoneNumber" in {
      val businessContact = BusinessContact(
        ppobAddress = None,
        email = None,
        telephoneNumber = None,
        hasWebsite = None,
        website = None,
        companyContactDetails = None,
        contactPreference = None
      )

      val telephoneNumber = BusinessContactTelephoneNumber("123456789")

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockGetSection(testRegId, None)

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[BusinessContactTelephoneNumber](telephoneNumber))
      result mustBe telephoneNumber
    }

    "determine that the model is incomplete and save in S4L - nothing pre-populated, update hasWebsite" in {
      val businessContact = BusinessContact(
        ppobAddress = None,
        email = None,
        telephoneNumber = None,
        hasWebsite = None,
        website = None,
        companyContactDetails = None,
        contactPreference = None
      )

      val hasWebsite = HasWebsiteAnswer(true)

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockGetSection(testRegId, None)

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[HasWebsiteAnswer](hasWebsite))
      result mustBe hasWebsite
    }

    "determine that the model is incomplete and save in S4L - nothing pre-populated, update website" in {
      val businessContact = BusinessContact(
        ppobAddress = None,
        email = None,
        telephoneNumber = None,
        hasWebsite = None,
        website = None,
        companyContactDetails = None,
        contactPreference = None
      )

      val website = Website("test.com")

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockGetSection(testRegId, None)

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[Website](website))
      result mustBe website
    }


    "determine that the model is incomplete and save in S4L - ppobAddress pre-populated, update companyContactDetails" in {
      val businessContact = BusinessContact(
        ppobAddress = Some(testAddress),
        companyContactDetails = None,
        contactPreference = None
      )

      val companyContactDetails = CompanyContactDetails("test@test.com", None, None, None)

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[CompanyContactDetails](companyContactDetails))
      result mustBe companyContactDetails
    }

    "determine that the model is incomplete and save in S4L - ppobAddress pre-populated, update contactPreference" in {
      val businessContact = BusinessContact(
        ppobAddress = Some(testAddress),
        companyContactDetails = None,
        contactPreference = None
      )

      val contactPreference = Email

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[ContactPreference](contactPreference))
      result mustBe contactPreference
    }

    "determine that the model is incomplete and save in S4L - companyContactDetails pre-populated, update ppobAddress" in {
      val businessContact = BusinessContact(
        ppobAddress = None,
        companyContactDetails = Some(CompanyContactDetails("test@test.com", None, None, None)),
        contactPreference = None
      )

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[Address](testAddress))
      result mustBe testAddress
    }

    "determine that the model is incomplete and save in S4L - companyContactDetails pre-populated, update contactPreference" in {
      val businessContact = BusinessContact(
        ppobAddress = None,
        companyContactDetails = Some(CompanyContactDetails("test@test.com", None, None, None)),
        contactPreference = None
      )

      val contactPreference = Email

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[ContactPreference](contactPreference))
      result mustBe contactPreference
    }

    "determine that the model is incomplete and save in S4L - contactPreference pre-populated, update ppobAddress" in {
      val businessContact = BusinessContact(
        ppobAddress = None,
        companyContactDetails = None,
        contactPreference = Some(Letter)
      )

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[Address](testAddress))
      result mustBe testAddress
    }

    "determine that the model is incomplete and save in S4L - contactPreference pre-populated, update companyContactDetails" in {
      val businessContact = BusinessContact(
        ppobAddress = None,
        companyContactDetails = None,
        contactPreference = Some(Email)
      )

      val companyContactDetails = CompanyContactDetails("test@test.com", None, None, None)

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[CompanyContactDetails](companyContactDetails))
      result mustBe companyContactDetails
    }


    "determine that the model is complete and save in the backend - update companyContactDetails" in {
      val businessContact = BusinessContact(
        ppobAddress = Some(testAddress),
        companyContactDetails = None,
        contactPreference = Some(Email)
      )

      val companyContactDetails = CompanyContactDetails("test@test.com", None, None, None)

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockReplaceSection[BusinessContact](testRegId, businessContact.copy(companyContactDetails = Some(companyContactDetails)))

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[CompanyContactDetails](companyContactDetails))
      result mustBe companyContactDetails
    }

    "determine that the model is complete and save in the backend - update business contact email" in {
      val businessContact = BusinessContact(
        ppobAddress = Some(testAddress),
        companyContactDetails = Some(CompanyContactDetails("test@test.com", None, None, None)),
        email = None,
        telephoneNumber = Some("123456789"),
        hasWebsite = Some(true),
        website = Some("test.com"),
        contactPreference = Some(Email)
      )

      val email = BusinessContactEmail("test@test.com")

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockReplaceSection[BusinessContact](testRegId, businessContact.copy(email = Some(email.answer)))

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[BusinessContactEmail](email))
      result mustBe email
    }

    "determine that the model is complete and save in the backend - update business contact telephoneNumber" in {
      val businessContact = BusinessContact(
        ppobAddress = Some(testAddress),
        companyContactDetails = Some(CompanyContactDetails("test@test.com", None, None, None)),
        email = Some("test@test.com"),
        telephoneNumber = None,
        hasWebsite = Some(true),
        website = Some("test.com"),
        contactPreference = Some(Email)
      )

      val telephoneNumber = BusinessContactTelephoneNumber("123456789")

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockReplaceSection[BusinessContact](testRegId, businessContact.copy(telephoneNumber = Some(telephoneNumber.answer)))

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[BusinessContactTelephoneNumber](telephoneNumber))
      result mustBe telephoneNumber
    }

    "determine that the model is complete and save in the backend - update business contact hasWebsite" in {
      val businessContact = BusinessContact(
        ppobAddress = Some(testAddress),
        companyContactDetails = Some(CompanyContactDetails("test@test.com", None, None, None)),
        email = Some("test@test.com"),
        telephoneNumber = Some("123456789"),
        hasWebsite = None,
        website = Some("test.com"),
        contactPreference = Some(Email)
      )

      val hasWebsite = HasWebsiteAnswer(true)

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockReplaceSection[BusinessContact](testRegId, businessContact.copy(hasWebsite = Some(hasWebsite.answer)))

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[HasWebsiteAnswer](hasWebsite))
      result mustBe hasWebsite
    }

    "determine that the model is complete and save in the backend - update business contact website" in {
      val businessContact = BusinessContact(
        ppobAddress = Some(testAddress),
        companyContactDetails = Some(CompanyContactDetails("test@test.com", None, None, None)),
        email = Some("test@test.com"),
        telephoneNumber = Some("123456789"),
        hasWebsite = Some(true),
        website = None,
        contactPreference = Some(Email)
      )

      val website = Website("test.com")

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockReplaceSection[BusinessContact](testRegId, businessContact.copy(website = Some(website.answer)))

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[Website](website))
      result mustBe website
    }

    "determine that the model is complete and save in the backend - reset website to null when user selects no for hasWebsite" in {
      val businessContact = BusinessContact(
        ppobAddress = Some(testAddress),
        companyContactDetails = Some(CompanyContactDetails("test@test.com", None, None, None)),
        email = Some("test@test.com"),
        telephoneNumber = Some("123456789"),
        hasWebsite = Some(true),
        website = Some("test.com"),
        contactPreference = Some(Email)
      )

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockReplaceSection[BusinessContact](testRegId, businessContact.copy(hasWebsite = Some(false), website = None))

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[HasWebsiteAnswer](HasWebsiteAnswer(false)))
      result mustBe HasWebsiteAnswer(false)
    }

    "determine that the model is complete and save in the backend - update ppobAddress" in {
      val businessContact = BusinessContact(
        ppobAddress = None,
        companyContactDetails = Some(CompanyContactDetails("test@test.com", None, None, None)),
        contactPreference = Some(Email)
      )

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockReplaceSection[BusinessContact](testRegId, businessContact.copy(ppobAddress = Some(testAddress)))

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[Address](testAddress))
      result mustBe testAddress
    }

    "determine that the model is complete and save in the backend - update contactPreference" in {
      val businessContact = BusinessContact(
        ppobAddress = Some(testAddress),
        companyContactDetails = Some(CompanyContactDetails("test@test.com", None, None, None)),
        contactPreference = None
      )

      val contactPreference = Letter

      when(mockS4LService.fetchAndGet[BusinessContact](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(businessContact)))

      mockReplaceSection[BusinessContact](testRegId, businessContact.copy(contactPreference = Some(contactPreference)))

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusinessContact[ContactPreference](contactPreference))
      result mustBe contactPreference
    }

  }
}
