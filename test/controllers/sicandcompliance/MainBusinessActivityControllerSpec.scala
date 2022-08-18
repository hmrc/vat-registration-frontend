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

import featureswitch.core.config.{FeatureSwitching, OtherBusinessInvolvement}
import fixtures.VatRegistrationFixture
import models.ModelKeys.SIC_CODES_KEY
import models.api.{SicCode, UkCompany}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import services.mocks.MockVatRegistrationService
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.sicandcompliance.main_business_activity

import scala.concurrent.Future

class MainBusinessActivityControllerSpec extends ControllerSpec with FutureAssertions with VatRegistrationFixture with FeatureSwitching
with MockVatRegistrationService {


  val mockMainBusinessActivityView: main_business_activity = app.injector.instanceOf[main_business_activity]

  class Setup {
    val controller: MainBusinessActivityController = new MainBusinessActivityController(
      mockAuthClientConnector,
      mockSessionService,
      mockBusinessService,
      mockFlatRateService,
      vatRegistrationServiceMock,
      mockMainBusinessActivityView
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(OtherBusinessInvolvement)
  }
  override def afterEach(): Unit = {
    super.afterEach()
    disable(OtherBusinessInvolvement)
  }

  s"GET ${controllers.sicandcompliance.routes.MainBusinessActivityController.show}" should {
    "return OK when view present in S4L" in new Setup {
      mockGetBusiness(Future.successful(validBusiness))
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }

    "return HTML where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetBusiness(Future.successful(validBusinessWithNoDescriptionAndLabour))
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      callAuthorised(controller.show) { result =>
        status(result) mustBe OK
      }
    }
  }

  s"POST ${controllers.sicandcompliance.routes.MainBusinessActivityController.submit}" should {
    val fakeRequest = FakeRequest(controllers.sicandcompliance.routes.MainBusinessActivityController.show)

    "return 400" in new Setup {
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)
      mockPartyType(Future.successful(UkCompany))

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }

    "return 400 with selected sicCode but no sicCode list in keystore" in new Setup {
      mockUpdateBusiness(Future.successful(validBusiness))
      mockSessionFetchAndGet(SIC_CODES_KEY, Option.empty[List[SicCode]])
      mockPartyType(Future.successful(UkCompany))

      submitAuthorised(controller.submit,
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

      submitAuthorised(controller.submit,
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

      submitAuthorised(controller.submit,
        fakeRequest.withFormUrlEncodedBody("value" -> validNoCompliance.code)
      )(_ redirectsTo controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url)
    }
  }

}
