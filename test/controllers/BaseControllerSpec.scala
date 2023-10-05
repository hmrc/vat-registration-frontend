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

package controllers

import config.AuthClientConnector
import featuretoggle.FeatureToggleSupport
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import services.{SessionProfile, SessionService}
import testHelpers.ControllerSpec
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.{ExecutionContext, Future}

class BaseControllerSpec extends ControllerSpec with FeatureToggleSupport {

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  object TestController extends BaseController with SessionProfile {
    override implicit val executionContext: ExecutionContext = ec
    override val sessionService: SessionService = mockSessionService

    val authConnector: AuthClientConnector = mockAuthClientConnector

    def callAuthenticated: Action[AnyContent] = isAuthenticated {
      _ => Future.successful(Ok("ALL GOOD"))
    }

    def callAuthenticatedButError: Action[AnyContent] = isAuthenticated {
      _ => Future.failed(new Exception("Something wrong"))
    }

    def callAuthenticatedWithProfile: Action[AnyContent] =
      isAuthenticatedWithProfile {
        _ =>
          profile =>
            Future.successful(Ok(s"ALL GOOD with profile: ${profile.registrationId}"))
      }

    def callAuthenticatedWithProfileButError: Action[AnyContent] =
      isAuthenticatedWithProfile {
        _ =>
          profile =>
            Future.failed(new Exception(s"Something wrong for profile: ${profile.registrationId}"))
      }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  "isAuthenticated" should {
    "return 200 if user is Authenticated and has org affinity" in {
      mockAuthenticatedOrg()

      val result = TestController.callAuthenticated(FakeRequest())
      status(result) mustBe OK
      contentAsString(result) mustBe "ALL GOOD"
    }

    "return 200 if user is Authenticated and has agent affinity" in {
      mockAuthenticatedAgent()

      val result = TestController.callAuthenticated(FakeRequest())
      status(result) mustBe OK
      contentAsString(result) mustBe "ALL GOOD"
    }

    "redirect to individual affinity kickout page if the user has a Individual affinity" in {
      mockAuthenticatedIndividual()

      val result = TestController.callAuthenticated(FakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/register-for-vat/error/individual-affinity")
    }

    "throw an exception if user is Authenticated and has no affinity group" in {
      mockAuthenticatedWithNoAffinityGroup()

      val exception = intercept[InternalServerException]{
        await(TestController.callAuthenticated(FakeRequest()))
      }
      exception.getMessage must include("User has no affinity group on their credential")
    }

    "return 303 to GG login if user has No Active Session" in {
      mockNoActiveSession()

      val result = TestController.callAuthenticated(FakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("http://localhost:9025/gg/sign-in?accountType=organisation&continue=http%3A%2F%2Flocalhost%3A9895%2Fregister-for-vat%2Fpost-sign-in&origin=vat-registration-frontend")
    }

    "return 500 if user is Not Authenticated" in {
      mockNotAuthenticated()

      val result = TestController.callAuthenticated(FakeRequest())
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return an Exception if something went wrong" in {
      mockAuthenticatedOrg()

      val result = TestController.callAuthenticatedButError(FakeRequest())
      an[Exception] mustBe thrownBy(await(result))
    }
  }

  "isAuthenticatedWithProfile" when {
    "return 200 with a profile if user is Authenticated" in {
      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      val result = TestController.callAuthenticatedWithProfile()(FakeRequest())
      status(result) mustBe OK
      contentAsString(result) mustBe s"ALL GOOD with profile: ${currentProfile.registrationId}"
    }
    "return 303 to GG login if user has No Active Session" in {
      mockNoActiveSession()

      val result = TestController.callAuthenticatedWithProfile()(FakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("http://localhost:9025/gg/sign-in?accountType=organisation&continue=http%3A%2F%2Flocalhost%3A9895%2Fregister-for-vat%2Fpost-sign-in&origin=vat-registration-frontend")
    }
    "return 500 if user is Not Authenticated" in {
      mockNotAuthenticated()

      val result = TestController.callAuthenticatedWithProfile()(FakeRequest())
      status(result) mustBe INTERNAL_SERVER_ERROR
    }
    "return an Exception if something went wrong" in {
      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      val result = TestController.callAuthenticatedWithProfileButError()(FakeRequest())
      an[Exception] mustBe thrownBy(await(result))
    }
  }

  "isAuthenticatedWithProfileNoStatusCheck" should {
    "return 200 with a profile if user is Authenticated" in {
      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      val result = TestController.callAuthenticatedWithProfile()(FakeRequest())
      status(result) mustBe OK
      contentAsString(result) mustBe s"ALL GOOD with profile: ${currentProfile.registrationId}"
    }

    "return 303 to GG login if user has No Active Session" in {
      mockNoActiveSession()

      val result = TestController.callAuthenticatedWithProfile()(FakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("http://localhost:9025/gg/sign-in?accountType=organisation&continue=http%3A%2F%2Flocalhost%3A9895%2Fregister-for-vat%2Fpost-sign-in&origin=vat-registration-frontend")
    }

    "return 500 if user is Not Authenticated" in {
      mockNotAuthenticated()

      val result = TestController.callAuthenticatedWithProfile()(FakeRequest())
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return an Exception if something went wrong" in {
      mockAuthenticatedOrg()
      mockWithCurrentProfile(Some(currentProfile))

      val result = TestController.callAuthenticatedWithProfileButError()(FakeRequest())
      an[Exception] mustBe thrownBy(await(result))
    }
  }


}
