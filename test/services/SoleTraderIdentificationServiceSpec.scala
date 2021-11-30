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

import connectors.mocks.MockSoleTraderIdConnector
import models.api.Individual
import models.external.soletraderid.SoleTraderIdJourneyConfig
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.Future

class SoleTraderIdentificationServiceSpec extends VatRegSpec
  with MockSoleTraderIdConnector {

  class Setup {
    val testContinueUrl = "/continue-url"
    val testServiceName = "testServiceName"
    val testDeskproId = "testDeskproId"
    val testSignOutUrl = "/test-sign-out-url"
    val testAccessibilityUrl = "/test-accessibility-url"
    val testJourneyUrl = "/testJourneyUrl"
    val regime = "VATC"
    val partyType = Individual
    val testJourneyConfig = SoleTraderIdJourneyConfig(testContinueUrl, Some(testServiceName), testDeskproId, testSignOutUrl, testAccessibilityUrl, regime)

    object Service extends SoleTraderIdentificationService(mockSoleTraderIdConnector)

  }

  "startSoleTraderJourney" must {
    "return a journeyId when provided with config" in new Setup {
      mockStartSoleTraderJourney(testJourneyConfig, partyType)(Future.successful(testJourneyUrl))

      val res = await(Service.startSoleTraderJourney(testJourneyConfig, partyType))

      res mustBe testJourneyUrl
    }
    "throw an exception if the call to STI fails" in new Setup {
      mockStartSoleTraderJourney(testJourneyConfig, partyType)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException] {
        await(Service.startSoleTraderJourney(testJourneyConfig, partyType))
      }
    }
  }

  "startIndividualJourney" must {
    "return a journeyId when provided with config" in new Setup {
      mockStartIndividualJourney(testJourneyConfig)(Future.successful(testJourneyUrl))

      val res = await(Service.startIndividualJourney(testJourneyConfig))

      res mustBe testJourneyUrl
    }
    "throw an exception if the call to STI fails" in new Setup {
      mockStartIndividualJourney(testJourneyConfig)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException] {
        await(Service.startIndividualJourney(testJourneyConfig))
      }
    }
  }

  "retrieveSoleTraderDetails" must {
    "return sole trader details" in new Setup {
      mockRetrieveSoleTraderDetails(testJourneyUrl)(Future.successful((testPersonalDetails, testSoleTrader)))

      val res = await(Service.retrieveSoleTraderDetails(testJourneyUrl))

      res mustBe(testPersonalDetails, testSoleTrader)
    }
  }

  "retrieveIndividualDetails" must {
    "return individual details" in new Setup {
      mockRetrieveIndividualDetails(testJourneyUrl)(Future.successful(testPersonalDetails))

      val res = await(Service.retrieveIndividualDetails(testJourneyUrl))

      res mustBe testPersonalDetails
    }
  }

}
