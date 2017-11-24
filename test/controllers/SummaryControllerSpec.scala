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

package controllers

import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.ModelKeys.INCORPORATION_STATUS
import models.external.IncorporationInfo
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class SummaryControllerSpec extends VatRegSpec with VatRegistrationFixture {

  object TestSummaryController extends SummaryController(ds,mockVATFeatureSwitch)(mockS4LService, mockVatRegistrationService) {
    override val authConnector: AuthConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  "Calling summary to show the summary page" should {

    "return HTML with a valid summary view pre-incorp" in {
      when(mockS4LService.clear()(any(), any())).thenReturn(validHttpResponse.pure)
      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, Some(testIncorporationInfo))
      when(mockVatRegistrationService.getVatScheme()(any(),any())).thenReturn(Future.successful(validVatScheme))
      mockGetCurrentProfile()
      when(mockVATFeatureSwitch.disableEligibilityFrontend).thenReturn(enabledFeatureSwitch)
      callAuthorised(TestSummaryController.show)(_ includesText "Check and confirm your answers")
    }

    "return HTML with a valid summary view post-incorp" in {
      when(mockS4LService.clear()(any(),any())).thenReturn(validHttpResponse.pure)
      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, None)
      when(mockVatRegistrationService.getVatScheme()(any(),any())).thenReturn(validVatScheme.pure)
      mockGetCurrentProfile()
      when(mockVATFeatureSwitch.disableEligibilityFrontend).thenReturn(enabledFeatureSwitch)
      callAuthorised(TestSummaryController.show)(_ includesText "Check and confirm your answers")
    }

    "getRegistrationSummary maps a valid VatScheme object to a Summary object" in {
      when(mockVatRegistrationService.getVatScheme()(any(),any())).thenReturn(validVatScheme.pure)
      implicit val cp = currentProfile()
      when(mockVATFeatureSwitch.disableEligibilityFrontend).thenReturn(enabledFeatureSwitch)
      TestSummaryController.getRegistrationSummary().map(summary => summary.sections.length mustEqual 2)
    }

    "registrationToSummary maps a valid VatScheme object to a Summary object" in {
      TestSummaryController.registrationToSummary(validVatScheme).sections.length mustEqual 11
    }

    "registrationToSummary maps a valid empty VatScheme object to a Summary object" in {
      TestSummaryController.registrationToSummary(emptyVatSchemeWithAccountingPeriodFrequency).sections.length mustEqual 11
    }

  }

}
