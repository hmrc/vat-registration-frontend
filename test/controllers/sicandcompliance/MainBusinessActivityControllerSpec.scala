/*
 * Copyright 2023 HM Revenue & Customs
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

import fixtures.VatRegistrationFixture
import models.api.SicCode.SIC_CODES_KEY
import models.api.SicCode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.sicandcompliance.MainBusinessActivity

import scala.concurrent.Future

class MainBusinessActivityControllerSpec extends ControllerSpec with FutureAssertions
  with VatRegistrationFixture {

  val mockMainBusinessActivityView: MainBusinessActivity = app.injector.instanceOf[MainBusinessActivity]

  class Setup {
    val controller: MainBusinessActivityController = new MainBusinessActivityController(
      mockAuthClientConnector,
      mockSessionService,
      mockBusinessService,
      mockFlatRateService,
      mockMainBusinessActivityView
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }


  s"GET ${controllers.sicandcompliance.routes.MainBusinessActivityController.show}" should {
    "return OK when view present in backend" in new Setup {
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

      submitAuthorised(controller.submit, fakeRequest.withMethod("POST").withFormUrlEncodedBody()
      )(result => result isA 400)
    }

    "return 400 with selected sicCode but no sicCode list in keystore" in new Setup {
      mockUpdateBusiness(Future.successful(validBusiness))
      mockSessionFetchAndGet(SIC_CODES_KEY, Option.empty[List[SicCode]])

      submitAuthorised(controller.submit,
        fakeRequest.withMethod("POST").withFormUrlEncodedBody("value" -> sicCode.code)
      )(_ isA 400)

    }

    "return 303 with selected sicCode" in new Setup {
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(List(validLabourSicCode)))
      when(mockBusinessService.updateBusiness(any())(any(), any(), any())).thenReturn(Future.successful(validBusiness))
      when(mockFlatRateService.resetFRSForSAC(any())(any(), any(), any())).thenReturn(Future.successful(sicCode))

      submitAuthorised(controller.submit,
        fakeRequest.withMethod("POST").withFormUrlEncodedBody("value" -> validLabourSicCode.code)
      )(_ redirectsTo controllers.sicandcompliance.routes.BusinessActivitiesResolverController.resolve.url)
    }
  }

}
