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

package controllers.sicAndCompliance

import controllers.sicAndCompliance
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.CurrentProfile
import models.view.test.SicStub
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._

import scala.concurrent.Future

class ComplianceIntroductionControllerSpec extends VatRegSpec with VatRegistrationFixture {

  object ComplianceIntroductionController extends ComplianceIntroductionController(mockS4LService, ds) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector = mockKeystoreConnector
  }

  s"GET ${sicAndCompliance.routes.ComplianceIntroductionController.show()}" should {
    "display the introduction page to a set of compliance questions" in {
      mockGetCurrentProfile()
      callAuthorised(ComplianceIntroductionController.show) {
        _ includesText "Tell us more"
      }
    }
  }

  s"POST ${sicAndCompliance.routes.ComplianceIntroductionController.submit()}" should {
    "redirect the user to the next page in the flow" in {
      when(mockS4LService.fetchAndGet[SicStub]()(any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(SicStub(Some("12345678"), None, None, None))))
      mockGetCurrentProfile()
      callAuthorised(ComplianceIntroductionController.submit) {
        result =>
          result redirectsTo s"$contextRoot/trade-goods-services-with-countries-outside-uk"
      }
    }

    "redirect the user to the SIC code selection page" in {
      when(mockS4LService.fetchAndGet[SicStub]()(any(), any(), any(), any())).thenReturn(Future.successful(None))
      mockGetCurrentProfile()
      callAuthorised(ComplianceIntroductionController.submit) {
        result =>
          result redirectsTo "/sic-stub"
      }
    }

    "redirect the user to the first question about cultural compliance" in {
      mockGetCurrentProfile()
      when(mockS4LService.fetchAndGet[SicStub]()(any(), any(), any(), any())).thenReturn(Future.successful(
        Some(SicStub(Some("90010123"), Some("90020123"), None, None))
      ))
      callAuthorised(ComplianceIntroductionController.submit) {
        result =>
          result redirectsTo s"$contextRoot/not-for-profit-or-public-body"
      }
    }

    "redirect the user to the first question about labour compliance" in {
      mockGetCurrentProfile()
      when(mockS4LService.fetchAndGet[SicStub]()(any(), any(), any(), any())).thenReturn(Future.successful(
        Some(SicStub(Some("42110123"), Some("42910123"), None, None))
      ))
      callAuthorised(ComplianceIntroductionController.submit) {
        result =>
          result redirectsTo s"$contextRoot/provides-workers-to-other-employers"
      }
    }

    "redirect the user to the first question about financial compliance" in {
      mockGetCurrentProfile()
      when(mockS4LService.fetchAndGet[SicStub]()(any(), any(), any(), any())).thenReturn(Future.successful(
        Some(SicStub(Some("70221123"), Some("64921123"), None, None))
      ))
      callAuthorised(ComplianceIntroductionController.submit) {
        result =>
          result redirectsTo s"$contextRoot/provides-advice-only-or-consultancy-services"
      }
    }
  }
}
