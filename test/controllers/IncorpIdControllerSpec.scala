/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.{LocalDate, LocalDateTime}

import _root_.models._
import fixtures.VatRegistrationFixture
import mocks.TimeServiceMock
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.{Action, AnyContent, AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import services.VoluntaryPageViewModel
import testHelpers.{ControllerSpec, FutureAssertions}

import scala.concurrent.Future
import controllers.registration.applicant.{routes => applicantRoutes}

class IncorpIdControllerSpec extends ControllerSpec with VatRegistrationFixture with TimeServiceMock with FutureAssertions {

  class Setup(cp: Option[CurrentProfile] = Some(currentProfile)) {
    val testController: IncorpIdController = new IncorpIdController(
      messagesControllerComponents,
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockIncorpIdService
    )

    mockAuthenticated()
    mockWithCurrentProfile(cp)
  }

  "startIncorpIdJourney" should {
    "redirect to the journeyStartUrl" in new Setup {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      lazy val testContinueUrl: String = applicantRoutes.FormerNameController.show().absoluteURL()
      lazy val testJourneyStartUrl = "/test"
      mockCreateJourney(testContinueUrl)(Future.successful(testJourneyStartUrl))

      lazy val res: Future[Result] = testController.startIncorpIdJourney()(fakeRequest)

      status(res) mustBe SEE_OTHER
      redirectLocation(res) mustBe Some(testJourneyStartUrl)
    }
  }
}
