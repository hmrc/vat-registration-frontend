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
    val testJourneyUrl = "/testJourneyUrl"
    val testJourneyConfig = SoleTraderIdJourneyConfig(testContinueUrl, Some(testServiceName), testDeskproId, testSignOutUrl, enableSautrCheck = false)

    object Service extends SoleTraderIdentificationService(mockSoleTraderIdConnector)
  }

  "startJourney" must {
    "return a journeyId when provided with config" in new Setup {
      mockStartJourney(testJourneyConfig)(Future.successful(testJourneyUrl))

      val res = await(Service.startJourney(testContinueUrl, testServiceName, testDeskproId, testSignOutUrl, enableSautrCheck = false))

      res mustBe testJourneyUrl
    }
    "throw an exception if the call to STI fails" in new Setup {
      mockStartJourney(testJourneyConfig)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException] {
        await(Service.startJourney(testContinueUrl, testServiceName, testDeskproId, testSignOutUrl, enableSautrCheck = false))
      }
    }
  }

  "retrieveSoleTraderDetails" must {
    "return transactor details" in new Setup{
      mockRetrieveSoleTraderDetails(testJourneyUrl)(Future.successful((testTransactorDetails, testSoleTrader)))

      val res = await(Service.retrieveSoleTraderDetails(testJourneyUrl))

      res mustBe (testTransactorDetails, testSoleTrader)
    }
  }

}
