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

package controllers.registration.applicant

import java.time.LocalDate

import _root_.models._
import config.FrontendAppConfig
import featureswitch.core.config.{FeatureSwitching, StubPersonalDetailsValidation}
import fixtures.VatRegistrationFixture
import mocks.TimeServiceMock
import mocks.mockservices.MockApplicantDetailsService
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}

import scala.concurrent.Future

class PersonalDetailsValidationControllerSpec extends ControllerSpec
  with VatRegistrationFixture
  with TimeServiceMock
  with FutureAssertions
  with FeatureSwitching
  with MockApplicantDetailsService {

  class Setup(cp: Option[CurrentProfile] = Some(currentProfile)) {
    val testController: PersonalDetailsValidationController = new PersonalDetailsValidationController(
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockPersonalDetailsValidationService,
      mockApplicantDetailsService
    )

    mockAuthenticated()
    mockWithCurrentProfile(cp)
  }

  "startPersonalDetailsValidationJourney" when {
    "the stub personal details validation journey feature switch is disabled" should {
      "redirect to the personal details validation service" in new Setup {
        disable(StubPersonalDetailsValidation)

        implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

        lazy val res: Future[Result] = testController.startPersonalDetailsValidationJourney()(fakeRequest)

        status(res) mustBe SEE_OTHER
        lazy val expectedRedirectUrl = "http://localhost:9968/personal-details-validation/start?completionUrl=http://localhost:9895/register-for-vat/personal-details-validation-callback"

        redirectLocation(res) mustBe Some(expectedRedirectUrl)
      }
    }
    "the stub personal details validation journey feature switch is enabled" should {
      "redirect directly to the callback URL" in new Setup {
        enable(StubPersonalDetailsValidation)

        implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

        lazy val res: Future[Result] = testController.startPersonalDetailsValidationJourney()(fakeRequest)

        status(res) mustBe SEE_OTHER
        lazy val expectedRedirectUrl = "/register-for-vat/personal-details-validation-callback?validationId=testValidationId?completionUrl=http://localhost:9895/register-for-vat/personal-details-validation-callback"

        redirectLocation(res) mustBe Some(expectedRedirectUrl)
      }
    }
  }

  "personalDetailsValidationCallback" when {
    "the transactor details successfully returned" should {
      "return OK with the retrieved transactor details" in new Setup { //TODO update to store the retrieved data and redirect to the correct page
        implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
        val testValidationId = "testValidationId"

        mockRetrieveValidationResult(testValidationId)(Future.successful(testTransactorDetails))
        mockSaveApplicantDetails(testTransactorDetails)(completeApplicantDetails)

        lazy val res: Future[Result] = testController.personalDetailsValidationCallback(testValidationId)(fakeRequest)

        status(res) mustBe SEE_OTHER
        redirectLocation(res) must contain(controllers.registration.applicant.routes.FormerNameController.show().url)
      }
    }
  }

}
