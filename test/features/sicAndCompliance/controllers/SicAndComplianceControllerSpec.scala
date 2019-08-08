/*
 * Copyright 2019 HM Revenue & Customs
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

package features.sicAndCompliance.controllers

import connectors.KeystoreConnector
import features.frs.services.FlatRateService
import features.sicAndCompliance.models.SicAndCompliance
import features.sicAndCompliance.services.{CustomICLMessages, ICLService, SicAndComplianceService}
import fixtures.VatRegistrationFixture
import helpers._
import models.ModelKeys.SIC_CODES_KEY
import models.api.SicCode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.VATRegFeatureSwitches

import scala.concurrent.Future


class SicAndComplianceControllerSpec extends ControllerSpec with MockMessages with FutureAssertions with VatRegistrationFixture {

  class Setup (iclStubbed:Boolean = false){
    val controller: SicAndComplianceController = new SicAndComplianceController {
      override val keystoreConnector: KeystoreConnector       = mockKeystoreConnector
      override val sicAndCompService: SicAndComplianceService = mockSicAndComplianceService
      override val frsService: FlatRateService                = mockFlatRateService
      val messagesApi: MessagesApi                            = mockMessagesAPI
      val authConnector: AuthConnector                        = mockAuthClientConnector
      override val vatRegFeatureSwitch: VATRegFeatureSwitches = mockFeatureSwitches
      override val useICLStub                                 = iclStubbed
      override val iclService: ICLService                     = mockICLService
      override val iclFEurlwww: String                        = "www-url"
    }

    mockAllMessages
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
    "redirect to SIC stub if feature switch is true" in new Setup (true) {
      callAuthorised(controller.submitSicHalt) {
        res =>
          status(res) mustBe 303
          res redirectsTo test.routes.SicStubController.show().url
      }
    }
    "redirect to ICL if feature switch is false" in new Setup {
      when(mockICLService.journeySetup(any())(any[HeaderCarrier](), any()))
        .thenReturn(Future.successful("/url"))

      callAuthorised(controller.submitSicHalt) {
        res =>
          status(res) mustBe 303
          res redirectsTo "www-url/url"
      }
    }
    "return exception" in new Setup (true) {
      when(mockICLService.journeySetup(any())(any[HeaderCarrier](), any()))
        .thenReturn(Future.failed(new Exception))
      intercept[Exception](callAuthorised(controller.submitSicHalt)(_ =>1 mustBe 2))
    }
  }

  "saveIclSicCodes" should {
    "redirect and save" when {
      "returning from ICL with multiple codes" in new Setup {
        val codes = List(sicCode, sicCode)

        when(mockICLService.getICLSICCodes()(any[HeaderCarrier](), any()))
          .thenReturn(Future.successful(codes))
        when(mockSicAndComplianceService.submitSicCodes(any())(any(), any()))
          .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))
        when(mockKeystoreConnector.cache(any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map())))

        callAuthorised(controller.saveIclCodes) {
          res =>
            status(res) mustBe 303
            res redirectsTo routes.SicAndComplianceController.showMainBusinessActivity().url
        }
      }
      "returning from ICL with one code" in new Setup {
        val codes = List(sicCode)

        when(mockICLService.getICLSICCodes()(any[HeaderCarrier](), any()))
          .thenReturn(Future.successful(codes))
        when(mockSicAndComplianceService.submitSicCodes(any())(any(), any()))
          .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))
        when(mockKeystoreConnector.cache(any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map())))

        callAuthorised(controller.saveIclCodes) {
          res =>
            status(res) mustBe 303
            res redirectsTo controllers.routes.TradingDetailsController.tradingNamePage().url
        }
      }
      "returning from ICL with compliance question SIC codes" in new Setup {
        val codes = List(sicCode, sicCode.copy(code = "81222"))

        when(mockICLService.getICLSICCodes()(any[HeaderCarrier](), any()))
          .thenReturn(Future.successful(codes))
        when(mockSicAndComplianceService.submitSicCodes(any())(any(), any()))
          .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))
        when(mockSicAndComplianceService.needComplianceQuestions(any()))
          .thenReturn(true)
        when(mockKeystoreConnector.cache(any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map())))

        callAuthorised(controller.saveIclCodes) {
          res =>
            status(res) mustBe 303
            res redirectsTo features.sicAndCompliance.controllers.routes.SicAndComplianceController.showComplianceIntro().url
        }
      }
    }
  }

  s"GET ${routes.SicAndComplianceController.showBusinessActivityDescription()}" should {
    "return HTML Business Activity Description page with no data in the form" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      callAuthorised(controller.showBusinessActivityDescription) {
        _ includesText MOCKED_MESSAGE
      }
    }

    "return HTML where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetSicAndCompliance(Future.successful(SicAndCompliance()))

      callAuthorised(controller.showBusinessActivityDescription) { result =>
        result includesText MOCKED_MESSAGE
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.SicAndComplianceController.submitBusinessActivityDescription()} with Empty data" should {
    val fakeRequest = FakeRequest(routes.SicAndComplianceController.showBusinessActivityDescription())

    "return 400" in new Setup {
      submitAuthorised(controller.submitBusinessActivityDescription(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

    "return 303" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      submitAuthorised(controller.submitBusinessActivityDescription(), fakeRequest.withFormUrlEncodedBody("description" -> "Testing")) {
        _ redirectsTo s"$contextRoot/choose-standard-industry-classification-codes"
      }
    }
  }

  s"GET ${routes.SicAndComplianceController.showComplianceIntro()}" should {
    "display the introduction page to a set of compliance questions" in new Setup {
      callAuthorised(controller.showComplianceIntro) {
        _ includesText MOCKED_MESSAGE
      }
    }
  }

  s"POST ${routes.SicAndComplianceController.submitComplianceIntro()}" should {
    "redirect the user to the SIC code selection page" in new Setup {
      callAuthorised(controller.submitComplianceIntro) {
        result =>
          result redirectsTo routes.LabourComplianceController.showProvideWorkers().url
      }
    }
  }

  s"GET ${routes.SicAndComplianceController.showMainBusinessActivity()}" should {
    "return HTML when view present in S4L" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      callAuthorised(controller.showMainBusinessActivity()) {
        _ includesText MOCKED_MESSAGE
      }
    }

    "return HTML where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetSicAndCompliance(Future.successful(SicAndCompliance()))
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      callAuthorised(controller.showMainBusinessActivity) { result =>
        result includesText MOCKED_MESSAGE
        status(result) mustBe 200
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

      submitAuthorised(controller.submitMainBusinessActivity(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> sicCode.code)
      )(_ isA 400)

    }

    "return 303 with selected sicCode" in new Setup {
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(List(validLabourSicCode)))

      when(mockSicAndComplianceService.updateSicAndCompliance(any())(any(), any()))
        .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))


      when(mockFlatRateService.resetFRSForSAC(any())(any(), any()))
        .thenReturn(Future.successful(sicCode))

      when(mockSicAndComplianceService.needComplianceQuestions(any()))
        .thenReturn(true)

      submitAuthorised(controller.submitMainBusinessActivity(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> validLabourSicCode.code)
      )(_ redirectsTo s"$contextRoot/tell-us-more-about-the-company")

    }
    "return 303 with selected sicCode (noCompliance) and sicCode list in keystore" in new Setup {

      mockKeystoreFetchAndGet(SIC_CODES_KEY, Some(List(validNoCompliance)))

      when(mockSicAndComplianceService.updateSicAndCompliance(any())(any(), any()))
        .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))


      when(mockFlatRateService.resetFRSForSAC(any())(any(), any()))
        .thenReturn(Future.successful(sicCode))

      when(mockSicAndComplianceService.needComplianceQuestions(any()))
        .thenReturn(false)

      submitAuthorised(controller.submitMainBusinessActivity(),

        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> validNoCompliance.code)
      )(_ redirectsTo s"$contextRoot/trading-name")
    }
  }

  "returnToICL" should {
    "take the user to ICL stub" when {
      "hitting change (for SIC codes) on the summary" in new Setup(iclStubbed = true) {
        callAuthorised(controller.returnToICL) {
          res =>
            status(res) mustBe 303
            res redirectsTo test.routes.SicStubController.show().url
        }
      }
    }
    "take the user to ICL" when {
      "hitting change (for SIC codes) on the summary" in new Setup {
        when(mockICLService.journeySetup(any())(any[HeaderCarrier](), any()))
          .thenReturn(Future.successful("/url"))

        callAuthorised(controller.returnToICL) {
          res =>
            status(res) mustBe 303
            res redirectsTo "www-url/url"
        }
      }
    }
    "return exception" in new Setup (true) {
      when(mockICLService.journeySetup(any())(any[HeaderCarrier](), any()))
        .thenReturn(Future.failed(new Exception))
      intercept[Exception](callAuthorised(controller.returnToICL)(_ =>1 mustBe 2))
    }
  }

}
