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
import models.view.test.SicStub
import org.mockito.Matchers.any
import org.mockito.Mockito._

import scala.concurrent.Future

class ComplianceIntroductionControllerSpec extends VatRegSpec with VatRegistrationFixture {

  object ComplianceIntroductionController extends ComplianceIntroductionController(mockS4LService, ds) {
    override val authConnector = mockAuthConnector
  }

  s"GET ${sicAndCompliance.routes.ComplianceIntroductionController.show()}" should {

    "display the introduction page to a set of compliance questions" in {
      callAuthorised(ComplianceIntroductionController.show) {
        _ includesText "Tell us more"
      }
    }
  }

  s"POST ${sicAndCompliance.routes.ComplianceIntroductionController.submit()}" should {

    "redirect the user to the next page in the flow" in {
      when(mockS4LService.fetchAndGet[SicStub]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(SicStub(Some("12345678"), None, None, None))))
      callAuthorised(ComplianceIntroductionController.submit) {
        result =>
          result redirectsTo s"$contextRoot/company-bank-account"
      }
    }
  }

  s"POST ${sicAndCompliance.routes.ComplianceIntroductionController.submit()} with no SIC code selection" should {

    "redirect the user to the SIC code selection page" in {
      when(mockS4LService.fetchAndGet[SicStub]()(any(), any(), any())).thenReturn(Future.successful(None))
      callAuthorised(ComplianceIntroductionController.submit) {
        result =>
          result redirectsTo "/sic-stub"
      }
    }
  }

  s"POST ${sicAndCompliance.routes.ComplianceIntroductionController.submit()} with cultural SIC code selection" should {

    "redirect the user to the first question about cultural compliance" in {
      when(mockS4LService.fetchAndGet[SicStub]()(any(), any(), any())).thenReturn(Future.successful(
        Some(SicStub(Some("90010123"), Some("90020123"), None, None))
      ))
      callAuthorised(ComplianceIntroductionController.submit) {
        result =>
          result redirectsTo s"$contextRoot/compliance/not-for-profit"
      }
    }
  }

  s"POST ${sicAndCompliance.routes.ComplianceIntroductionController.submit()} with labour SIC code selection" should {

    "redirect the user to the first question about labour compliance" in {
      when(mockS4LService.fetchAndGet[SicStub]()(any(), any(), any())).thenReturn(Future.successful(
        Some(SicStub(Some("42110123"), Some("42910123"), None, None))
      ))
      callAuthorised(ComplianceIntroductionController.submit) {
        result =>
          result redirectsTo s"$contextRoot/compliance/provide-workers"
      }
    }
  }

  s"POST ${sicAndCompliance.routes.ComplianceIntroductionController.submit()} with financial SIC code selection" should {

    "redirect the user to the first question about financial compliance" in {
      when(mockS4LService.fetchAndGet[SicStub]()(any(), any(), any())).thenReturn(Future.successful(
        Some(SicStub(Some("70221123"), Some("64921123"), None, None))
      ))
      callAuthorised(ComplianceIntroductionController.submit) {
        result =>
          result redirectsTo s"$contextRoot/compliance/advice-or-consultancy"
      }
    }
  }
}
