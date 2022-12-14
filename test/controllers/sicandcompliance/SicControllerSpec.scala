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

package controllers.sicandcompliance

import featureswitch.core.config.{FeatureSwitching, StubIcl}
import fixtures.VatRegistrationFixture
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.sicandcompliance._

import scala.concurrent.Future

class SicControllerSpec extends ControllerSpec with FutureAssertions with VatRegistrationFixture with FeatureSwitching {

  val mockAboutToConfirmSicView: about_to_confirm_sic = app.injector.instanceOf[about_to_confirm_sic]

  class Setup {
    val controller: SicController = new SicController(
      mockAuthClientConnector,
      mockSessionService,
      mockBusinessService,
      mockFlatRateService,
      mockICLService,
      mockAboutToConfirmSicView
    ) {
      override val iclFEurlwww: String = "www-url"
    }

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "showHaltPage should return a 200" in new Setup {
    callAuthorised(controller.show) {
      status(_) mustBe 200
    }
  }

  "submitHaltPage" should {
    "redirect to SIC stub if feature switch is true" in new Setup {
      enable(StubIcl)
      callAuthorised(controller.startICLJourney) {
        res =>
          status(res) mustBe 303
          res redirectsTo controllers.test.routes.SicStubController.show.url
      }
    }
    "redirect to ICL if feature switch is false" in new Setup {
      disable(StubIcl)
      when(mockICLService.journeySetup(any(), any())(any[HeaderCarrier](), any()))
        .thenReturn(Future.successful("/url"))

      callAuthorised(controller.startICLJourney) {
        res =>
          status(res) mustBe 303
          res redirectsTo "www-url/url"
      }
    }
    "return exception" in new Setup {
      enable(StubIcl)
      when(mockICLService.journeySetup(any(), any())(any[HeaderCarrier](), any()))
        .thenReturn(Future.failed(new Exception))
      intercept[Exception](callAuthorised(controller.startICLJourney)(_ => 1 mustBe 2))
    }
  }

  "saveIclSicCodes" should {
    "redirect and save" when {
      "returning from ICL with multiple codes" in new Setup {
        val codes = List(sicCode, sicCode)

        when(mockICLService.getICLSICCodes()(any[HeaderCarrier](), any())).thenReturn(Future.successful(codes))
        when(mockBusinessService.submitSicCodes(any())(any(), any()))
          .thenReturn(Future.successful(validBusiness))
        when(mockSessionService.cache(any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map())))

        callAuthorised(controller.saveICLCodes) {
          res =>
            status(res) mustBe 303
            res redirectsTo controllers.sicandcompliance.routes.MainBusinessActivityController.show.url
        }
      }

      "returning from ICL with multiple codes including compliance" in new Setup {
        val codes = List(sicCode, sicCode.copy(code = "81222"))

        when(mockICLService.getICLSICCodes()(any[HeaderCarrier](), any())).thenReturn(Future.successful(codes))
        when(mockBusinessService.submitSicCodes(any())(any(), any()))
          .thenReturn(Future.successful(validBusiness))
        when(mockSessionService.cache(any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map())))

        callAuthorised(controller.saveICLCodes) {
          res =>
            status(res) mustBe 303
            res redirectsTo controllers.sicandcompliance.routes.MainBusinessActivityController.show.url
        }
      }

      "returning from ICL with one code" in new Setup {
        val codes = List(sicCode)

        when(mockICLService.getICLSICCodes()(any[HeaderCarrier](), any())).thenReturn(Future.successful(codes))
        when(mockBusinessService.submitSicCodes(any())(any(), any()))
          .thenReturn(Future.successful(validBusiness))
        when(mockSessionService.cache(any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map())))

        callAuthorised(controller.saveICLCodes) {
          res =>
            status(res) mustBe 303
            res redirectsTo controllers.sicandcompliance.routes.BusinessActivitiesResolverController.resolve.url
        }
      }
    }
  }



  "startICLJourney" should {
    "take the user to ICL stub" when {
      "hitting change (for SIC codes) on the summary" in new Setup {
        enable(StubIcl)
        callAuthorised(controller.startICLJourney) {
          res =>
            status(res) mustBe 303
            res redirectsTo controllers.test.routes.SicStubController.show.url
        }
      }
    }
    "take the user to ICL" when {
      "hitting change (for SIC codes) on the summary" in new Setup {
        disable(StubIcl)
        when(mockICLService.journeySetup(any(), any())(any[HeaderCarrier](), any()))
          .thenReturn(Future.successful("/url"))

        callAuthorised(controller.startICLJourney) {
          res =>
            status(res) mustBe 303
            res redirectsTo "www-url/url"
        }
      }
    }
    "return exception" in new Setup {
      enable(StubIcl)
      when(mockICLService.journeySetup(any(), any())(any[HeaderCarrier](), any()))
        .thenReturn(Future.failed(new Exception))
      intercept[Exception](callAuthorised(controller.startICLJourney)(_ => 1 mustBe 2))
    }
  }

}
