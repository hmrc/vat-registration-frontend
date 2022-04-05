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

package controllers.registration.sicandcompliance

import featureswitch.core.config.{FeatureSwitching, OtherBusinessInvolvement}
import featureswitch.core.models.FeatureSwitch
import fixtures.VatRegistrationFixture
import models.api.{Individual, UkCompany}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.mocks.{MockVatRegistrationService, SicAndComplianceServiceMock}
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.labour.workers

import scala.concurrent.Future

class WorkersControllerSpec extends ControllerSpec with FutureAwaits with FutureAssertions with DefaultAwaitTimeout
  with VatRegistrationFixture with SicAndComplianceServiceMock with MockVatRegistrationService with FeatureSwitching {

  trait Setup {
    val view = app.injector.instanceOf[workers]
    val controller: WorkersController = new WorkersController(
      mockAuthClientConnector,
      mockSessionService,
      mockSicAndComplianceService,
      vatRegistrationServiceMock,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  s"GET ${controllers.registration.sicandcompliance.routes.WorkersController.show}" should {
    "return OK when there's a Workers model in S4L" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockIsTransactor(Future.successful(true))

      callAuthorised(controller.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
      }
    }
    "return OK where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithoutLabour))
      mockIsTransactor(Future.successful(true))

      callAuthorised(controller.show) { result =>
        status(result) mustBe OK
      }
    }
  }

  s"POST ${controllers.registration.sicandcompliance.routes.WorkersController.submit}" should {
    val fakeRequest = FakeRequest(controllers.registration.sicandcompliance.routes.WorkersController.show)

    "return BAD_REQUEST with Empty data" in new Setup {
      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody(
      )) {
        result => status(result) mustBe BAD_REQUEST
      }
    }
    "redirect to the Party type resolver for UkCompany" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockIsTransactor(Future.successful(true))
      when(mockVatRegistrationService.partyType(any(), any())).thenReturn(Future.successful(UkCompany))
      verifyFeatureSwitchFlow(controller, fakeRequest)
    }

    "redirect to the party type resolver for sole trader" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockIsTransactor(Future.successful(true))
      when(mockVatRegistrationService.partyType(any(), any())).thenReturn(Future.successful(Individual))
      verifyFeatureSwitchFlow(controller, fakeRequest)
    }
  }

  private def verifyFeatureSwitchFlow(controller: WorkersController,
                                      fakeRequest: FakeRequest[AnyContentAsEmpty.type]): Unit = {

    def verifyRedirectLocation(featureSwitchFn: FeatureSwitch => Unit, resolvedLocation: Call): Unit = {
      featureSwitchFn(OtherBusinessInvolvement)
      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody(
        "numberOfWorkers" -> "5"
      ))( _ redirectsTo resolvedLocation.url)
    }

    verifyRedirectLocation(disable, controllers.routes.TradingNameResolverController.resolve)
    verifyRedirectLocation(enable, controllers.registration.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show)
  }
}