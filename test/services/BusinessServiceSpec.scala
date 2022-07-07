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
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class BusinessServiceSpec extends VatRegSpec with MockRegistrationApiConnector {

  val testService: BusinessService = new BusinessService(
    mockRegistrationApiConnector,
    mockS4LService
  )

  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val dummyCacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson("")))

  "getbusiness" should {
    "return a populated Business model" when {
      "there is a model in S4L" in {
        val business = Business(
          ppobAddress = Some(testAddress),
          email = Some("test@test.com"),
          contactPreference = Some(Letter)
        )

        when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
          .thenReturn(Future.successful(Some(business)))

        val result = await(testService.getBusiness)
        result mustBe business
      }

      "there is data in the backend" in {
        val business = Business(
          ppobAddress = Some(Address(
            line1 = "testLine1",
            line2 = Some("testLine2"),
            line3 = Some("testLine3"),
            line4 = Some("testLine4"),
            postcode = Some("TE57 7ET"),
            addressValidated = true
          )),
          email = Some("test@test.com"),
          telephoneNumber = Some("1234567890"),
          website = Some("/test/url"),
          contactPreference = Some(Email)
        )

        when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
          .thenReturn(Future.successful(None))

        mockGetSection[Business](testRegId, Some(business))

        when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
          .thenReturn(Future.successful(dummyCacheMap))

        val result = await(testService.getBusiness)
        result mustBe business
      }
    }

    "return an empty model" when {
      "there is no data in either S4L or the backend" in {
        val business = Business(ppobAddress = None, contactPreference = None)

        when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
          .thenReturn(Future.successful(None))

        mockGetSection[Business](testRegId, None)

        when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
          .thenReturn(Future.successful(dummyCacheMap))

        val result = await(testService.getBusiness)
        result mustBe business
      }
    }
  }

  "updateBusiness" should {
    "determine that the model is incomplete and save in S4L - nothing pre-populated, update ppobAddress" in {
      val business = Business(ppobAddress = None, contactPreference = None)

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      mockGetSection(testRegId, None)

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](business.copy(ppobAddress = Some(testAddress))))
      result.ppobAddress mustBe Some(testAddress)
    }

    "determine that the model is incomplete and save in S4L - nothing pre-populated, update contactPreference" in {
      val business = Business(ppobAddress = None, contactPreference = None)

      val updatedContactPreference = Some(Email)

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      mockGetSection(testRegId, None)

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](business.copy(contactPreference = updatedContactPreference)))
      result.contactPreference mustBe updatedContactPreference
    }

    "determine that the model is incomplete and save in S4L - nothing pre-populated, update email" in {
      val business = Business(
        ppobAddress = None,
        email = None,
        telephoneNumber = None,
        hasWebsite = None,
        website = None,
        contactPreference = None
      )

      val updatedEmail = Some("test@test.com")

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      mockGetSection(testRegId, None)

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](business.copy(email = updatedEmail)))
      result.email mustBe updatedEmail
    }

    "determine that the model is incomplete and save in S4L - nothing pre-populated, update telephoneNumber" in {
      val business = Business(
        ppobAddress = None,
        email = None,
        telephoneNumber = None,
        hasWebsite = None,
        website = None,
        contactPreference = None
      )

      val updatedTelephoneNumber = Some("123456789")

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      mockGetSection(testRegId, None)

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](business.copy(telephoneNumber = updatedTelephoneNumber)))
      result.telephoneNumber mustBe updatedTelephoneNumber
    }

    "determine that the model is incomplete and save in S4L - nothing pre-populated, update hasWebsite" in {
      val business = Business(
        ppobAddress = None,
        email = None,
        telephoneNumber = None,
        hasWebsite = None,
        website = None,
        contactPreference = None
      )

      val updatedHasWebsiteAnswer = Some(true)

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      mockGetSection(testRegId, None)

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](business.copy(hasWebsite = updatedHasWebsiteAnswer)))
      result.hasWebsite mustBe updatedHasWebsiteAnswer
    }

    "determine that the model is incomplete and save in S4L - nothing pre-populated, update website" in {
      val business = Business(
        ppobAddress = None,
        email = None,
        telephoneNumber = None,
        hasWebsite = None,
        website = None,
        contactPreference = None
      )

      val updatedWebsiteAnswer = Some("test.com")

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      mockGetSection(testRegId, None)

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](business.copy(website = updatedWebsiteAnswer)))
      result.website mustBe updatedWebsiteAnswer
    }


    "determine that the model is incomplete and save in S4L - ppobAddress pre-populated, update contactPreference" in {
      val business = Business(
        ppobAddress = Some(testAddress),
        contactPreference = None
      )

      val updatedContactPreference = Some(Email)

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](business.copy(contactPreference = updatedContactPreference)))
      result.contactPreference mustBe updatedContactPreference
    }

    "determine that the model is incomplete and save in S4L - companyContactDetails pre-populated, update ppobAddress" in {
      val business = Business(
        ppobAddress = None,
        email = Some("test@test.com"),
        contactPreference = None
      )

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](business.copy(ppobAddress = Some(testAddress))))
      result.ppobAddress mustBe Some(testAddress)
    }

    "determine that the model is incomplete and save in S4L - companyContactDetails pre-populated, update contactPreference" in {
      val business = Business(
        ppobAddress = None,
        email = Some("test@test.com"),
        contactPreference = None
      )

      val updatedContactPreference = Some(Email)

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](business.copy(contactPreference = updatedContactPreference)))
      result.contactPreference mustBe updatedContactPreference
    }

    "determine that the model is incomplete and save in S4L - contactPreference pre-populated, update ppobAddress" in {
      val business = Business(
        ppobAddress = None,
        contactPreference = Some(Letter)
      )

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      when(mockS4LService.save(matchers.any())(matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](business.copy(ppobAddress = Some(testAddress))))
      result.ppobAddress mustBe Some(testAddress)
    }

    "determine that the model is complete and save in the backend - update business email" in {
      val business = Business(
        ppobAddress = Some(testAddress),
        email = Some("test@test.com"),
        telephoneNumber = Some("123456789"),
        hasWebsite = Some(true),
        website = Some("test.com"),
        contactPreference = Some(Email),
        businessDescription = Some("test desc"),
        mainBusinessActivity = Some(sicCode),
        businessActivities = Some(List(sicCode)),
        labourCompliance = Some(complianceWithLabour)
      )

      val email = Some("test1@test.com")
      val updatedBusinessDetails = business.copy(email = email)

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      mockReplaceSection[Business](testRegId, updatedBusinessDetails)

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](updatedBusinessDetails))
      result.email mustBe email
    }

    "determine that the model is complete and save in the backend - update business telephoneNumber" in {
      val business = Business(
        ppobAddress = Some(testAddress),
        email = Some("test@test.com"),
        telephoneNumber = None,
        hasWebsite = Some(true),
        website = Some("test.com"),
        contactPreference = Some(Email),
        businessDescription = Some("test desc"),
        mainBusinessActivity = Some(sicCode),
        businessActivities = Some(List(sicCode)),
        labourCompliance = Some(complianceWithLabour)
      )

      val telephoneNumber = Some("123456789")
      val updatedBusinessDetails = business.copy(telephoneNumber = telephoneNumber)

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      mockReplaceSection[Business](testRegId, updatedBusinessDetails)

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](updatedBusinessDetails))
      result.telephoneNumber mustBe telephoneNumber
    }

    "determine that the model is complete and save in the backend - update business hasWebsite" in {
      val business = Business(
        ppobAddress = Some(testAddress),
        email = Some("test@test.com"),
        telephoneNumber = Some("123456789"),
        hasWebsite = None,
        website = Some("test.com"),
        contactPreference = Some(Email),
        businessDescription = Some("test desc"),
        mainBusinessActivity = Some(sicCode),
        businessActivities = Some(List(sicCode)),
        labourCompliance = Some(complianceWithLabour)
      )

      val hasWebsite = Some(true)
      val updatedBusinessDetails = business.copy(hasWebsite = hasWebsite)

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      mockReplaceSection[Business](testRegId, updatedBusinessDetails)

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](updatedBusinessDetails))
      result.hasWebsite mustBe hasWebsite
    }

    "determine that the model is complete and save in the backend - update business website" in {
      val business = Business(
        ppobAddress = Some(testAddress),
        email = Some("test@test.com"),
        telephoneNumber = Some("123456789"),
        hasWebsite = Some(true),
        website = None,
        contactPreference = Some(Email),
        businessDescription = Some("test desc"),
        mainBusinessActivity = Some(sicCode),
        businessActivities = Some(List(sicCode)),
        labourCompliance = Some(complianceWithLabour)
      )

      val website = Some("test.com")
      val updatedBusinessDetails = business.copy(website = website)

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      mockReplaceSection[Business](testRegId, updatedBusinessDetails)

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](updatedBusinessDetails))
      result.website mustBe website
    }

    "determine that the model is complete and save in the backend - reset website to null when user selects no for hasWebsite" in {
      val business = Business(
        ppobAddress = Some(testAddress),
        email = Some("test@test.com"),
        telephoneNumber = Some("123456789"),
        hasWebsite = Some(true),
        website = Some("test.com"),
        contactPreference = Some(Email),
        businessDescription = Some("test desc"),
        mainBusinessActivity = Some(sicCode),
        businessActivities = Some(List(sicCode)),
        labourCompliance = Some(complianceWithLabour)
      )

      val updatedBusinessDetails = business.copy(hasWebsite = Some(false), website = None)

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      mockReplaceSection[Business](testRegId, updatedBusinessDetails)

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](updatedBusinessDetails))
      result.hasWebsite mustBe Some(false)
      result.website mustBe None
    }

    "determine that the model is complete and save in the backend - update ppobAddress" in {
      val business = Business(
        ppobAddress = None,
        email = Some("test@test.com"),
        telephoneNumber = Some("123456789"),
        hasWebsite = Some(true),
        website = Some("test.com"),
        contactPreference = Some(Email),
        businessDescription = Some("test desc"),
        mainBusinessActivity = Some(sicCode),
        businessActivities = Some(List(sicCode)),
        labourCompliance = Some(complianceWithLabour)
      )

      val updatedBusinessDetails = business.copy(ppobAddress = Some(testAddress))

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      mockReplaceSection[Business](testRegId, updatedBusinessDetails)

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](updatedBusinessDetails))
      result.ppobAddress mustBe Some(testAddress)
    }

    "determine that the model is complete and save in the backend - update contactPreference" in {
      val business = Business(
        ppobAddress = Some(testAddress),
        email = Some("test@test.com"),
        telephoneNumber = Some("123456789"),
        hasWebsite = Some(true),
        website = Some("test.com"),
        contactPreference = None,
        businessDescription = Some("test desc"),
        mainBusinessActivity = Some(sicCode),
        businessActivities = Some(List(sicCode)),
        labourCompliance = Some(complianceWithLabour)
      )

      val contactPreference = Some(Letter)
      val updatedBusinessDetails = business.copy(contactPreference = contactPreference)

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      mockReplaceSection[Business](testRegId, updatedBusinessDetails)

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](updatedBusinessDetails))
      result.contactPreference mustBe contactPreference
    }

    "determine that the model is complete and save in the backend when compliance questions are not needed" in {
      val business = Business(
        ppobAddress = Some(testAddress),
        email = Some("test@test.com"),
        telephoneNumber = Some("123456789"),
        hasWebsite = Some(true),
        website = Some("test.com"),
        contactPreference = Some(Email),
        mainBusinessActivity = Some(sicCode),
        businessActivities = Some(List(sicCode))
      )

      val businessDescription = Some("test desc")
      val updatedBusinessDetails = business.copy(businessDescription = businessDescription)

      when(mockS4LService.fetchAndGet[Business](matchers.any(), matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(Some(business)))

      mockReplaceSection[Business](testRegId, updatedBusinessDetails)

      when(mockS4LService.clearKey(matchers.any(), matchers.any(), matchers.any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result = await(testService.updateBusiness[Business](updatedBusinessDetails))
      result.businessDescription mustBe businessDescription
    }
  }
}