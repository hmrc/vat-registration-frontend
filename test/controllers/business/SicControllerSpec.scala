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

package controllers.business

import featureswitch.core.config.{FeatureSwitching, OtherBusinessInvolvement, StubIcl}
import fixtures.VatRegistrationFixture
import models.ModelKeys.SIC_CODES_KEY
import models.api.{SicCode, UkCompany}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import services.mocks.MockVatRegistrationService
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.sicandcompliance._

import scala.concurrent.Future

class SicControllerSpec extends ControllerSpec with FutureAssertions with VatRegistrationFixture with FeatureSwitching
  with MockVatRegistrationService {

  val mockAboutToConfirmSicView: about_to_confirm_sic = app.injector.instanceOf[about_to_confirm_sic]
  val mockMainBusinessActivityView: main_business_activity = app.injector.instanceOf[main_business_activity]

  class Setup {
    val controller: SicController = new SicController(
      mockAuthClientConnector,
      mockSessionService,
      mockBusinessService,
      mockFlatRateService,
      mockICLService,
      vatRegistrationServiceMock,
      mockAboutToConfirmSicView,
      mockMainBusinessActivityView
    ) {
      override val iclFEurlwww: String = "www-url"
    }

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val validLabourSicCode: SicCode = SicCode("81221001", "BarFoo", "BarFoo")
  val validNoCompliance: SicCode = SicCode("12345678", "fooBar", "FooBar")

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(OtherBusinessInvolvement)
  }
  override def afterEach(): Unit = {
    super.afterEach()
    disable(OtherBusinessInvolvement)
  }

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
          res redirectsTo controllers.test.routes.SicStubController.show.url
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
        when(mockBusinessService.submitSicCodes(any())(any(), any()))
          .thenReturn(Future.successful(validBusiness))
        when(mockSessionService.cache(any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map())))
        mockPartyType(Future.successful(UkCompany))

        callAuthorised(controller.saveIclCodes) {
          res =>
            status(res) mustBe 303
            res redirectsTo controllers.business.routes.SicController.showMainBusinessActivity.url
        }
      }

      "returning from ICL with multiple codes including compliance" in new Setup {
        val codes = List(sicCode, sicCode.copy(code = "81222"))

        when(mockICLService.getICLSICCodes()(any[HeaderCarrier](), any())).thenReturn(Future.successful(codes))
        when(mockBusinessService.submitSicCodes(any())(any(), any()))
          .thenReturn(Future.successful(validBusiness))
        when(mockSessionService.cache(any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map())))
        mockPartyType(Future.successful(UkCompany))

        callAuthorised(controller.saveIclCodes) {
          res =>
            status(res) mustBe 303
            res redirectsTo controllers.business.routes.SicController.showMainBusinessActivity.url
        }
      }

      "returning from ICL with one code" in new Setup {
        val codes = List(sicCode)

        when(mockICLService.getICLSICCodes()(any[HeaderCarrier](), any())).thenReturn(Future.successful(codes))
        when(mockBusinessService.submitSicCodes(any())(any(), any()))
          .thenReturn(Future.successful(validBusiness))
        when(mockSessionService.cache(any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map())))
        mockPartyType(Future.successful(UkCompany))

        callAuthorised(controller.saveIclCodes) {
          res =>
            status(res) mustBe 303
            res redirectsTo controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url
        }
      }

      "returning from ICL with one compliance question SIC code" in new Setup {
        val codes = List(sicCode.copy(code = "81222"))

        when(mockICLService.getICLSICCodes()(any[HeaderCarrier](), any())).thenReturn(Future.successful(codes))
        when(mockBusinessService.submitSicCodes(any())(any(), any()))
          .thenReturn(Future.successful(validBusiness))
        when(mockBusinessService.needComplianceQuestions(any())).thenReturn(true)
        when(mockSessionService.cache(any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map())))
        mockPartyType(Future.successful(UkCompany))

        callAuthorised(controller.saveIclCodes) {
          res =>
            status(res) mustBe 303
            res redirectsTo controllers.business.routes.ComplianceIntroductionController.show.url
        }
      }
    }
  }

  s"GET ${controllers.business.routes.SicController.showMainBusinessActivity}" should {
    "return OK when view present in S4L" in new Setup {
      mockGetBusiness(Future.successful(validBusiness))
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      callAuthorised(controller.showMainBusinessActivity) {
        status(_) mustBe OK
      }
    }

    "return HTML where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetBusiness(Future.successful(validBusinessWithNoDescriptionAndLabour))
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      callAuthorised(controller.showMainBusinessActivity) { result =>
        status(result) mustBe OK
      }
    }
  }

  s"POST ${controllers.business.routes.SicController.submitMainBusinessActivity}" should {
    val fakeRequest = FakeRequest(controllers.business.routes.SicController.showMainBusinessActivity)

    "return 400" in new Setup {
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)
      mockPartyType(Future.successful(UkCompany))

      submitAuthorised(controller.submitMainBusinessActivity, fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }

    "return 400 with selected sicCode but no sicCode list in keystore" in new Setup {
      mockUpdateBusiness(Future.successful(validBusiness))
      mockSessionFetchAndGet(SIC_CODES_KEY, Option.empty[List[SicCode]])
      mockPartyType(Future.successful(UkCompany))

      submitAuthorised(controller.submitMainBusinessActivity,
        fakeRequest.withFormUrlEncodedBody("value" -> sicCode.code)
      )(_ isA 400)

    }

    "return 303 with selected sicCode" in new Setup {
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(List(validLabourSicCode)))
      mockPartyType(Future.successful(UkCompany))

      when(mockBusinessService.updateBusiness(any())(any(), any()))
        .thenReturn(Future.successful(validBusiness))
      when(mockFlatRateService.resetFRSForSAC(any())(any(), any())).thenReturn(Future.successful(sicCode))
      when(mockBusinessService.needComplianceQuestions(any())).thenReturn(true)

      submitAuthorised(controller.submitMainBusinessActivity,
        fakeRequest.withFormUrlEncodedBody("value" -> validLabourSicCode.code)
      )(_ redirectsTo s"$contextRoot/tell-us-more-about-the-business")

    }

    "return 303 with selected sicCode (noCompliance) and sicCode list in keystore" in new Setup {
      mockSessionFetchAndGet(SIC_CODES_KEY, Some(List(validNoCompliance)))
      mockPartyType(Future.successful(UkCompany))

      when(mockBusinessService.updateBusiness(any())(any(), any()))
        .thenReturn(Future.successful(validBusiness))
      when(mockFlatRateService.resetFRSForSAC(any())(any(), any())).thenReturn(Future.successful(sicCode))
      when(mockBusinessService.needComplianceQuestions(any())).thenReturn(false)

      submitAuthorised(controller.submitMainBusinessActivity,
        fakeRequest.withFormUrlEncodedBody("value" -> validNoCompliance.code)
      )(_ redirectsTo controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url)
    }
  }

  "returnToICL" should {
    "take the user to ICL stub" when {
      "hitting change (for SIC codes) on the summary" in new Setup {
        enable(StubIcl)
        callAuthorised(controller.returnToICL) {
          res =>
            status(res) mustBe 303
            res redirectsTo controllers.test.routes.SicStubController.show.url
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
