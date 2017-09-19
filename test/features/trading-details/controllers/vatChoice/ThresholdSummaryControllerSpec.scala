/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.vatTradingDetails.vatChoice

import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.ModelKeys.INCORPORATION_STATUS
import models.{CurrentProfile, S4LTradingDetails}
import models.external.IncorporationInfo
import models.view.vatTradingDetails.vatChoice.OverThresholdView
import org.mockito.Matchers
import org.mockito.Mockito.when
import play.api.test.FakeRequest

import scala.concurrent.Future

class ThresholdSummaryControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object TestThresholdSummaryController extends ThresholdSummaryController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.vatTradingDetails.vatChoice.routes.ThresholdSummaryController.show())

  "Calling threshold summary to show the threshold summary page" should {
    "return HTML with a valid threshold summary view" in {
      save4laterReturns(S4LTradingDetails(
        overThreshold = Some(OverThresholdView(false, None))
      ))
      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, Some(testIncorporationInfo))

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(TestThresholdSummaryController.show)(_ includesText "Check and confirm your answers")
    }

    "getVatThresholdPostIncorp returns a valid VatThresholdPostIncorp" in {
      save4laterReturns(S4LTradingDetails(
        overThreshold = Some(OverThresholdView(false, None))
      ))

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      TestThresholdSummaryController.getVatThresholdPostIncorp() returns validVatThresholdPostIncorp
    }

    "getThresholdSummary maps a valid VatThresholdSummary object to a Summary object" in {
      save4laterReturns(S4LTradingDetails(
        overThreshold = Some(OverThresholdView(false, None))
      ))

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      TestThresholdSummaryController.getThresholdSummary().map(summary => summary.sections.length mustEqual 1)
    }
  }

  s"POST ${controllers.vatTradingDetails.vatChoice.routes.ThresholdSummaryController.submit()}" should {
    "redirect the user to the voluntary registration page if all answers to threshold questions are no" in {
      save4laterReturns(S4LTradingDetails(
        overThreshold = Some(OverThresholdView(false, None))
      ))

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(TestThresholdSummaryController.submit) {
        _ redirectsTo s"$contextRoot/do-you-want-to-register-voluntarily"
      }
    }

    "redirect the user to the completion capacity page if any answers to threshold questions are yes" in {
      save4laterReturns(S4LTradingDetails(
        overThreshold = Some(OverThresholdView(true, Some(testDate)))
      ))

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(TestThresholdSummaryController.submit) {
        _ redirectsTo s"$contextRoot/who-is-registering-the-company-for-vat"
      }
    }
  }
}
