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

package controllers

import featureswitch.core.config.{FeatureSwitching, StubIcl}
import fixtures.VatRegistrationFixture
import models.ModelKeys.SIC_CODES_KEY
import models.SicAndCompliance
import models.api.{Individual, SicCode, UkCompany}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.about_to_confirm_sic

import scala.concurrent.Future

class SicAndComplianceControllerSpec extends ControllerSpec with FutureAssertions with VatRegistrationFixture with FeatureSwitching {

  val mockAboutToConfirmSicView: about_to_confirm_sic = app.injector.instanceOf[about_to_confirm_sic]

  class Setup {
    val controller: SicAndComplianceController = new SicAndComplianceController(
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockSicAndComplianceService,
      mockFlatRateService,
      mockICLService,
      mockAboutToConfirmSicView,
      mockVatRegistrationService
    ) {
      override val iclFEurlwww: String = "www-url"
    }

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val validLabourSicCode = SicCode("81221001", "BarFoo", "BarFoo")
  val validNoCompliance = SicCode("12345678", "fooBar", "FooBar")

  "showHaltPage should return a 200" in new Setup {
    callAuthorised(controller.showSicHalt) {
      status(_) mustBe 200
    }
  }

  "submitHaltPage" should {
    "redirect to SIC stub if feature switch is true" in new Setup {
      enable(StubIcl)
      callAuthorised(controller.submitSicHalt) {
        res =>
          status(res) mustBe 303
          res redirectsTo controllers.test.routes.SicStubController.show().url
      }
    }
    "redirect to ICL if feature switch is false" in new Setup {
      disable(StubIcl)
      when(mockICLService.journeySetup(any())(any[HeaderCarrier](), any()))
        .thenReturn(Future.successful("/url"))

      callAuthorised(controller.submitSicHalt) {
        res =>
          status(res) mustBe 303
          res redirectsTo "www-url/url"
      }
    }
    "return exception" in new Setup {
      enable(StubIcl)
      when(mockICLService.journeySetup(any())(any[HeaderCarrier](), any()))
        .thenReturn(Future.failed(new Exception))
      intercept[Exception](callAuthorised(controller.submitSicHalt)(_ => 1 mustBe 2))
    }
  }

  "saveIclSicCodes" should {
    "redirect and save" when {
      "returning from ICL with multiple codes" in new Setup {
        val codes = List(sicCode, sicCode)

        when(mockICLService.getICLSICCodes()(any[HeaderCarrier](), any())).thenReturn(Future.successful(codes))
        when(mockSicAndComplianceService.submitSicCodes(any())(any(), any()))
          .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))
        when(mockKeystoreConnector.cache(any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map())))
        when(mockVatRegistrationService.partyType(any(), any())).thenReturn(Future.successful(Individual))

        callAuthorised(controller.saveIclCodes) {
          res =>
            status(res) mustBe 303
            res redirectsTo routes.SicAndComplianceController.showMainBusinessActivity().url
        }
      }

      "returning from ICL with multiple codes including compliance" in new Setup {
        val codes = List(sicCode, sicCode.copy(code = "81222"))

        when(mockICLService.getICLSICCodes()(any[HeaderCarrier](), any())).thenReturn(Future.successful(codes))
        when(mockSicAndComplianceService.submitSicCodes(any())(any(), any()))
          .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))
        when(mockKeystoreConnector.cache(any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map())))
        when(mockVatRegistrationService.partyType(any(), any())).thenReturn(Future.successful(Individual))

        callAuthorised(controller.saveIclCodes) {
          res =>
            status(res) mustBe 303
            res redirectsTo routes.SicAndComplianceController.showMainBusinessActivity().url
        }
      }

      "returning from ICL with one code" in new Setup {
        val codes = List(sicCode)

        when(mockICLService.getICLSICCodes()(any[HeaderCarrier](), any())).thenReturn(Future.successful(codes))
        when(mockSicAndComplianceService.submitSicCodes(any())(any(), any()))
          .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))
        when(mockKeystoreConnector.cache(any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map())))
        when(mockVatRegistrationService.partyType(any(), any())).thenReturn(Future.successful(UkCompany))

        callAuthorised(controller.saveIclCodes) {
          res =>
            status(res) mustBe 303
            res redirectsTo controllers.registration.business.routes.TradingNameController.show().url
        }
      }

      "returning from ICL with one compliance question SIC code" in new Setup {
        val codes = List(sicCode.copy(code = "81222"))

        when(mockICLService.getICLSICCodes()(any[HeaderCarrier](), any())).thenReturn(Future.successful(codes))
        when(mockSicAndComplianceService.submitSicCodes(any())(any(), any()))
          .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))
        when(mockSicAndComplianceService.needComplianceQuestions(any())).thenReturn(true)
        when(mockKeystoreConnector.cache(any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map())))
        when(mockVatRegistrationService.partyType(any(), any())).thenReturn(Future.successful(Individual))

        callAuthorised(controller.saveIclCodes) {
          res =>
            status(res) mustBe 303
            res redirectsTo controllers.routes.ComplianceIntroductionController.show().url
        }
      }
    }
  }

  s"GET ${routes.SicAndComplianceController.showMainBusinessActivity()}" should {
    "return OK when view present in S4L" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      callAuthorised(controller.showMainBusinessActivity()) {
        status(_) mustBe OK
      }
    }

    "return HTML where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetSicAndCompliance(Future.successful(SicAndCompliance()))
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      callAuthorised(controller.showMainBusinessActivity) { result =>
        status(result) mustBe OK
      }
    }
  }

  s"POST ${routes.SicAndComplianceController.submitMainBusinessActivity()}" should {
    val fakeRequest = FakeRequest(routes.SicAndComplianceController.showMainBusinessActivity())

    "return 400" in new Setup {
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      submitAuthorised(controller.submitMainBusinessActivity(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }

    "return 400 with selected sicCode but no sicCode list in keystore" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockKeystoreFetchAndGet(SIC_CODES_KEY, Option.empty[List[SicCode]])
      when(mockVatRegistrationService.partyType(any(), any())).thenReturn(Future.successful(Individual))

      submitAuthorised(controller.submitMainBusinessActivity(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> sicCode.code)
      )(_ isA 400)

    }

    "return 303 with selected sicCode" in new Setup {
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(List(validLabourSicCode)))

      when(mockSicAndComplianceService.updateSicAndCompliance(any())(any(), any()))
        .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))
      when(mockFlatRateService.resetFRSForSAC(any())(any(), any())).thenReturn(Future.successful(sicCode))
      when(mockSicAndComplianceService.needComplianceQuestions(any())).thenReturn(true)
      when(mockVatRegistrationService.partyType(any(), any())).thenReturn(Future.successful(Individual))

      submitAuthorised(controller.submitMainBusinessActivity(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> validLabourSicCode.code)
      )(_ redirectsTo s"$contextRoot/tell-us-more-about-the-company")

    }
    "return 303 with selected sicCode (noCompliance) and sicCode list in keystore" in new Setup {
      mockKeystoreFetchAndGet(SIC_CODES_KEY, Some(List(validNoCompliance)))

      when(mockSicAndComplianceService.updateSicAndCompliance(any())(any(), any()))
        .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))
      when(mockFlatRateService.resetFRSForSAC(any())(any(), any())).thenReturn(Future.successful(sicCode))
      when(mockVatRegistrationService.partyType(any(), any())).thenReturn(Future.successful(UkCompany))
      when(mockSicAndComplianceService.needComplianceQuestions(any())).thenReturn(false)

      submitAuthorised(controller.submitMainBusinessActivity(),

        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> validNoCompliance.code)
      )(_ redirectsTo s"$contextRoot/trading-name")
    }
  }

  "returnToICL" should {
    "take the user to ICL stub" when {
      "hitting change (for SIC codes) on the summary" in new Setup {
        enable(StubIcl)
        callAuthorised(controller.returnToICL) {
          res =>
            status(res) mustBe 303
            res redirectsTo controllers.test.routes.SicStubController.show().url
        }
      }
    }
    "take the user to ICL" when {
      "hitting change (for SIC codes) on the summary" in new Setup {
        disable(StubIcl)
        when(mockICLService.journeySetup(any())(any[HeaderCarrier](), any()))
          .thenReturn(Future.successful("/url"))

        callAuthorised(controller.returnToICL) {
          res =>
            status(res) mustBe 303
            res redirectsTo "www-url/url"
        }
      }
    }
    "return exception" in new Setup {
      enable(StubIcl)
      when(mockICLService.journeySetup(any())(any[HeaderCarrier](), any()))
        .thenReturn(Future.failed(new Exception))
      intercept[Exception](callAuthorised(controller.returnToICL)(_ => 1 mustBe 2))
    }
  }

}
