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

import featureswitch.core.config.{FeatureSwitching, OtherBusinessInvolvement, TaskList}
import fixtures.VatRegistrationFixture
import models.ModelKeys.SIC_CODES_KEY
import models.api.{NonUkNonEstablished, SicCode, UkCompany}
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import services.mocks.MockVatRegistrationService
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.Future

class BusinessActivitiesResolverControllerSpec extends ControllerSpec with FutureAssertions with FeatureSwitching
  with MockVatRegistrationService with VatRegistrationFixture {

  class Setup {
    val controller: BusinessActivitiesResolverController = new BusinessActivitiesResolverController(
      mockSessionService,
      mockAuthClientConnector,
      mockBusinessService,
      vatRegistrationServiceMock
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
          status(res) mustBe 303
          res redirectsTo controllers.sicandcompliance.routes.MainBusinessActivityController.show.url
      }
    }

    "redirect to ImportsOrExports if session cache has multiple codes and is main business activity submission flow" in new Setup {
      disable(TaskList)
      disable(OtherBusinessInvolvement)

      mockPartyType(Future.successful(UkCompany))
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(List(sicCode, sicCode.copy(code = "81222"))))

      val requestWithReferer = FakeRequest().withHeaders(
        REFERER -> controllers.sicandcompliance.routes.MainBusinessActivityController.submit.url
      )

      requestWithAuthorisedUser(controller.resolve, requestWithReferer) {
        res =>
          status(res) mustBe 303
          res redirectsTo controllers.vatapplication.routes.ImportsOrExportsController.show.url
      }
    }

    "redirect to compliance handler if sic code need compliance questions" in new Setup {
      val sicCodes: List[SicCode] = List(sicCode)
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(sicCodes))
      when(mockBusinessService.needComplianceQuestions(sicCodes)).thenReturn(true)

      callAuthorised(controller.resolve) {
        res =>
          status(res) mustBe 303
          res redirectsTo controllers.business.routes.ComplianceIntroductionController.show.url
      }
    }

    "redirect to task-list if sic code doesn't need compliance questions, if task-list FS enabled" in new Setup {
      enable(TaskList)

      val sicCodes: List[SicCode] = List(sicCode)
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(sicCodes))
      when(mockBusinessService.needComplianceQuestions(sicCodes)).thenReturn(false)

      callAuthorised(controller.resolve) {
        res =>
          status(res) mustBe 303
          res redirectsTo controllers.routes.TaskListController.show.url
      }

      disable(TaskList)
    }

    "redirect to OBI if sic code doesn't need compliance questions and task-list FS disabled and OBI enabled" in new Setup {
      enable(OtherBusinessInvolvement)

      val sicCodes: List[SicCode] = List(sicCode)
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(sicCodes))
      when(mockBusinessService.needComplianceQuestions(sicCodes)).thenReturn(false)

      callAuthorised(controller.resolve) {
        res =>
          status(res) mustBe 303
          res redirectsTo controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url
      }

      disable(OtherBusinessInvolvement)
    }

    "redirect to turn-over estimate if task-list and OBI FS disabled and party type is NonUK" in new Setup {
      val sicCodes: List[SicCode] = List(sicCode)
      mockPartyType(Future.successful(NonUkNonEstablished))
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(sicCodes))
      when(mockBusinessService.needComplianceQuestions(sicCodes)).thenReturn(false)

      callAuthorised(controller.resolve) {
        res =>
          status(res) mustBe 303
          res redirectsTo controllers.vatapplication.routes.TurnoverEstimateController.show.url
      }
    }

    "redirect to imports and exports if task-list and OBI FS disabled and party type is UK bound" in new Setup {
      val sicCodes: List[SicCode] = List(sicCode)
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(sicCodes))
      when(mockBusinessService.needComplianceQuestions(sicCodes)).thenReturn(false)

      callAuthorised(controller.resolve) {
        res =>
          status(res) mustBe 303
          res redirectsTo controllers.vatapplication.routes.TurnoverEstimateController.show.url
      }
    }

    "return exception if no sic codes available" in new Setup {
      mockSessionFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)
      intercept[InternalServerException](callAuthorised(controller.resolve) {
        res => status(res) mustBe 500
      })
    }
  }

}
