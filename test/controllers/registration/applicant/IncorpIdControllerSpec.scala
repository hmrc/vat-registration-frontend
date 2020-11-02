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

package controllers.registration.applicant

import _root_.models._
import controllers.registration.applicant.{routes => applicantRoutes}
import fixtures.VatRegistrationFixture
import mocks.TimeServiceMock
import mocks.mockservices.MockApplicantDetailsService
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}

import scala.concurrent.Future
import controllers.registration.applicant.{routes => applicantRoutes}

class IncorpIdControllerSpec extends ControllerSpec
  with VatRegistrationFixture
  with TimeServiceMock
  with FutureAssertions
  with MockApplicantDetailsService {

  val testJourneyId = "testJourneyId"

  class Setup(cp: Option[CurrentProfile] = Some(currentProfile)) {
    val testController: IncorpIdController = new IncorpIdController(
      messagesControllerComponents,
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockIncorpIdService,
      mockApplicantDetailsService
    )

    mockAuthenticated()
    mockWithCurrentProfile(cp)
  }

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  "startIncorpIdJourney" should {
    "redirect to the journeyStartUrl" in new Setup {
      lazy val testJourneyStartUrl = "/test"
      mockCreateJourney(appConfig.incorpIdCallbackUrl, "Register for VAT", "vrs", appConfig.feedbackUrl)(Future.successful(testJourneyStartUrl))

      lazy val res: Future[Result] = testController.startIncorpIdJourney()(fakeRequest)

      status(res) mustBe SEE_OTHER
      redirectLocation(res) mustBe Some(testJourneyStartUrl)
    }
  }

  "incorpIdCallback" should {
    "store the incorporation details and redirect to the next page when the response is valid" in new Setup {
      val onwardUrl = applicantRoutes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney().url
      mockGetDetails(testJourneyId)(Future.successful(testIncorpDetails))
      mockSaveApplicantDetails(testIncorpDetails)(completeApplicantDetails)

      val res = testController.incorpIdCallback(testJourneyId)(fakeRequest)

      status(res) mustBe SEE_OTHER
      redirectLocation(res) must contain(onwardUrl)
    }
  }

}
