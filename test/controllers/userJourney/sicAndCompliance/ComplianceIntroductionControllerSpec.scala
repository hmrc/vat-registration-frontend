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

package controllers.userJourney.sicAndCompliance

import controllers.userJourney.sicAndCompliance
import helpers.VatRegSpec
import models.view.test.SicStub
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class ComplianceIntroductionControllerSpec extends VatRegSpec {

  object ComplianceIntroductionController extends ComplianceIntroductionController(mockS4LService, ds) {
    implicit val headerCarrier = HeaderCarrier()

    override val authConnector = mockAuthConnector
  }

  s"GET ${sicAndCompliance.routes.ComplianceIntroductionController.show()}" should {

    "display the introduction page to a set of compliance questions" in {
      callAuthorised(ComplianceIntroductionController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Tell us more")
      }
    }
  }

  s"POST ${sicAndCompliance.routes.ComplianceIntroductionController.submit()}" should {

    "redirect the user to the next page in the flow" in {
      when(mockS4LService.fetchAndGet[SicStub]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(SicStub(Some("12345678"), None, None, None))))
      callAuthorised(ComplianceIntroductionController.submit, mockAuthConnector) {
        result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(
            controllers.userJourney.vatFinancials.routes.CompanyBankAccountController.show().url
          )
      }
    }
  }

  s"POST ${sicAndCompliance.routes.ComplianceIntroductionController.submit()} with no SIC code selection" should {

    "redirect the user to the SIC code selection page" in {
      when(mockS4LService.fetchAndGet[SicStub]()(any(), any(), any())).thenReturn(Future.successful(None))
      callAuthorised(ComplianceIntroductionController.submit, mockAuthConnector) {
        result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.test.routes.SicStubController.show().url)
      }
    }
  }

  s"POST ${sicAndCompliance.routes.ComplianceIntroductionController.submit()} with cultural SIC code selection" should {

    "redirect the user to the first question about cultural compliance" in {
      when(mockS4LService.fetchAndGet[SicStub]()(any(), any(), any())).thenReturn(Future.successful(
        Some(SicStub(Some("90010123"), Some("90020123"), None, None))
      ))
      callAuthorised(ComplianceIntroductionController.submit, mockAuthConnector) {
        result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(
            controllers.userJourney.sicAndCompliance.cultural.routes.NotForProfitController.show().url
          )
      }
    }
  }

  s"POST ${sicAndCompliance.routes.ComplianceIntroductionController.submit()} with labour SIC code selection" should {

    "redirect the user to the first question about labour compliance" in {
      when(mockS4LService.fetchAndGet[SicStub]()(any(), any(), any())).thenReturn(Future.successful(
        Some(SicStub(Some("42110123"), Some("42910123"), None, None))
      ))
      callAuthorised(ComplianceIntroductionController.submit, mockAuthConnector) {
        result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some( //TODO below should be routing to a different controller, once we have that controller
            controllers.userJourney.sicAndCompliance.cultural.routes.NotForProfitController.show().url
          )
      }
    }
  }
}
