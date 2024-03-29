/*
 * Copyright 2024 HM Revenue & Customs
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

import featuretoggle.FeatureToggleSupport
import fixtures.VatRegistrationFixture
import models.api.SicCode.SIC_CODES_KEY
import models.api.{SicCode, UkCompany}
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import services.mocks.MockVatRegistrationService
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class BusinessActivitiesResolverControllerSpec extends ControllerSpec with FutureAssertions with FeatureToggleSupport
  with MockVatRegistrationService with VatRegistrationFixture {

  class Setup {
    val controller: BusinessActivitiesResolverController = new BusinessActivitiesResolverController(
      mockSessionService,
      mockAuthClientConnector,
      mockBusinessService
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "resolve" should {
    "redirect to MainBusinessActivityController if session cache has multiple codes" in new Setup {
      mockPartyType(Future.successful(UkCompany))
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(List(sicCode, sicCode.copy(code = "81222"))))

      callAuthorised(controller.resolve) {
        res =>
          status(res) mustBe SEE_OTHER
          res redirectsTo controllers.sicandcompliance.routes.MainBusinessActivityController.show.url
      }
    }

    "redirect to compliance handler if sic code need compliance questions" in new Setup {
      val sicCodes: List[SicCode] = List(sicCode)
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(sicCodes))
      when(mockBusinessService.needComplianceQuestions(sicCodes)).thenReturn(true)

      callAuthorised(controller.resolve) {
        res =>
          status(res) mustBe SEE_OTHER
          res redirectsTo controllers.business.routes.ComplianceIntroductionController.show.url
      }
    }

    "redirect to task-list if journey is main business activity submission" in new Setup {
      implicit val testRequest: FakeRequest[_] = FakeRequest().withHeaders(
        REFERER -> controllers.sicandcompliance.routes.MainBusinessActivityController.submit.url
      )

      val sicCodes: List[SicCode] = List(sicCode)
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(sicCodes))
      when(mockBusinessService.needComplianceQuestions(sicCodes)).thenReturn(false)

      callAuthorised(controller.resolve) {
        res =>
          status(res) mustBe SEE_OTHER
          res redirectsTo controllers.routes.TaskListController.show.url
      }
    }

    "redirect to task-list if sic code doesn't need compliance questions" in new Setup {
      val sicCodes: List[SicCode] = List(sicCode)
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(sicCodes))
      when(mockBusinessService.needComplianceQuestions(sicCodes)).thenReturn(false)

      callAuthorised(controller.resolve) {
        res =>
          status(res) mustBe SEE_OTHER
          res redirectsTo controllers.routes.TaskListController.show.url
      }
    }

    "return exception if no sic codes available" in new Setup {
      mockAuthenticated()
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)
      mockSessionCache("missingAnswer", "tasklist.aboutTheBusiness.businessActivities")(Future.successful(CacheMap("", Map())))

      callAuthorised(controller.resolve) {
        res =>
          status(res) mustBe SEE_OTHER
          res redirectsTo controllers.errors.routes.ErrorController.missingAnswer.url
      }
    }
  }

}
