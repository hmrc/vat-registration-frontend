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

package controllers.test

import featureswitch.core.config.{FeatureSwitching, OtherBusinessInvolvement}
import featureswitch.core.models.FeatureSwitch
import fixtures.VatRegistrationFixture
import models.api.SicCode
import models.test.SicStub
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.test.SicStubPage

import scala.concurrent.Future

class SicStubControllerSpec extends ControllerSpec with FutureAwaits with FutureAssertions with DefaultAwaitTimeout
  with VatRegistrationFixture with FeatureSwitching {

  trait Setup {
    val controller: SicStubController = new SicStubController(
      mockConfigConnector,
      mockSessionService,
      mockS4LService,
      mockSicAndComplianceService,
      mockAuthClientConnector,
      app.injector.instanceOf[SicStubPage]
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  s"GET test-only${routes.SicStubController.show}" should {
    "return OK for Sic Stub page with no data in the form" in new Setup {
      when(mockS4LService.fetchAndGet[SicStub](any(), any(), any(), any())).thenReturn(Future.successful(None))

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }
  }

  s"POST test-only${routes.SicStubController.submit} with Empty data" should {
    val fakeRequest = FakeRequest(routes.SicStubController.show)
    val dummyCacheMap = CacheMap("", Map.empty)
    val dummySicCode = SicCode("tests", "tests", "tests")

    "return 303 and correct redirection with many sic code selected" in new Setup {
      when(mockS4LService.save[SicStub](any())(any(), any(), any(), any())).thenReturn(Future.successful(dummyCacheMap))
      when(mockConfigConnector.getSicCodeDetails(any())).thenReturn(dummySicCode)
      when(mockSessionService.cache(any(), any())(any(), any())).thenReturn(Future.successful(dummyCacheMap))
      when(mockSicAndComplianceService.submitSicCodes(any())(any(), any())).thenReturn(Future.successful(s4lVatSicAndComplianceWithoutLabour))
      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody("sicCode1" -> "66666", "sicCode2" -> "88888")) {
        _ redirectsTo controllers.routes.SicAndComplianceController.showMainBusinessActivity.url
      }
    }

    "return 303 and correct redirection with only one sic code selected" in new Setup {
      enable(OtherBusinessInvolvement)
      when(mockS4LService.save[SicStub](any())(any(), any(), any(), any())).thenReturn(Future.successful(dummyCacheMap))
      when(mockConfigConnector.getSicCodeDetails(any())).thenReturn(dummySicCode)
      when(mockSessionService.cache(any(), any())(any(), any())).thenReturn(Future.successful(dummyCacheMap))
      when(mockSicAndComplianceService.submitSicCodes(any())(any(), any())).thenReturn(Future.successful(s4lVatSicAndComplianceWithoutLabour))
      when(mockSicAndComplianceService.needComplianceQuestions(any())).thenReturn(false)

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody("sicCode1" -> "66666")
      verifyFeatureSwitchFlow(disable, controller, request, controllers.routes.TradingNameResolverController.resolve)
      verifyFeatureSwitchFlow(enable, controller, request, controllers.registration.sicandcompliance.routes.OtherBusinessInvolvementController.show)
    }

    "return 303 and correct redirection with only one labour sic code selected" in new Setup {
      when(mockS4LService.save[SicStub](any())(any(), any(), any(), any())).thenReturn(Future.successful(dummyCacheMap))
      when(mockConfigConnector.getSicCodeDetails(any())).thenReturn(dummySicCode)
      when(mockSessionService.cache(any(), any())(any(), any())).thenReturn(Future.successful(dummyCacheMap))
      when(mockSicAndComplianceService.submitSicCodes(any())(any(), any())).thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))
      when(mockSicAndComplianceService.needComplianceQuestions(any())).thenReturn(true)
      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody("sicCode1" -> "01610")) {
        _ redirectsTo controllers.routes.ComplianceIntroductionController.show.url
      }
    }

    def verifyFeatureSwitchFlow(featureSwitchFn: FeatureSwitch => Unit, controller: SicStubController,
                                request: FakeRequest[AnyContentAsFormUrlEncoded], resolvedCall: Call): Unit = {

      featureSwitchFn(OtherBusinessInvolvement)
      submitAuthorised(controller.submit, request) {
        _ redirectsTo resolvedCall.url
      }
    }
  }
}
