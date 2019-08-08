/*
 * Copyright 2019 HM Revenue & Customs
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

import connectors.ConfigConnector
import features.frs.services.FlatRateService
import features.officer.services.LodgingOfficerService
import features.sicAndCompliance.services.SicAndComplianceService
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call

import scala.concurrent.Future

class SummaryServiceSpec extends VatRegSpec with VatRegistrationFixture  {

  class Setup {
    val testService = new SummaryService {
      override val vrs: RegistrationService                         = mockVatRegistrationService
      override val lodgingOfficerService: LodgingOfficerService     = mockLodgingOfficerService
      override val sicAndComplianceService: SicAndComplianceService = mockSicAndComplianceService
      override val flatRateService: FlatRateService                 = mockFlatRateService
      override val configConnector: ConfigConnector                 = mockConfigConnector
      override val vatRegEFEUrl: String                             = "http://vatRegEFEUrl"
      override val vatRegEFEQuestionUri                             = "/question"
    }
  }

  "eligibilityCall" should {
    "return a full url" in new Setup {
     val res = testService.eligibilityCall("page1OfEligibility")
      res.url mustBe "http://vatRegEFEUrl/question?pageId=page1OfEligibility"
      res mustBe Call("GET", "http://vatRegEFEUrl/question?pageId=page1OfEligibility")
    }
  }

  "getRegistrationSummary" should {
    "map a valid VatScheme object to a Summary object" in new Setup {
      when(mockVatRegistrationService.getVatScheme(any(), any()))
        .thenReturn(Future.successful(validVatScheme.copy(threshold = optMandatoryRegistrationThirtyDays)))
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any()))
        .thenReturn(Future.successful(validFullLodgingOfficer))

    testService.getRegistrationSummary.map(summary => summary.sections.length mustEqual 2)
    }
  }

  "registrationToSummary" should {
    "map valid VatScheme object to a Summary object" in new Setup {
      when(mockConfigConnector.getBusinessTypeDetails(any()))
        .thenReturn(("test business type", BigDecimal(6.32)))

      testService.registrationToSummary(validVatScheme.copy(threshold = optMandatoryRegistrationThirtyDays)).sections.length mustEqual 10
    }
    "map a valid empty VatScheme object to a Summary object" in new Setup {
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any()))
        .thenReturn(Future.successful(validFullLodgingOfficer))

      testService.registrationToSummary(emptyVatSchemeWithAccountingPeriodFrequency.copy(threshold = optMandatoryRegistrationThirtyDays)).sections.length mustEqual 10

    }
  }

  "getEligibilitySummary" should {
    "return a Summary when valid json is returned from vatregservice" in new Setup {
      when(mockVatRegistrationService.getEligibilityData(any(),any())) thenReturn Future.successful(fullEligibilityDataJson.as[JsObject])

      await(testService.getEligibilityDataSummary) mustBe fullSummaryModelFromFullEligiblityJson
    }
    "return an exception when json is invalid returned from vatregservice" in new Setup {
      when(mockVatRegistrationService.getEligibilityData(any(),any())) thenReturn Future.successful(Json.obj("foo" -> "bar"))

      intercept[Exception](await(testService.getEligibilityDataSummary))
    }
  }
}