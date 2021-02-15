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

package controllers

import config.AuthClientConnector
import connectors.KeystoreConnector
import featureswitch.core.config.{FeatureSwitching, TrafficManagementPredicate}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import services.SessionProfile
import testHelpers.ControllerSpec

import scala.concurrent.{ExecutionContext, Future}

class BaseControllerSpec extends ControllerSpec with FeatureSwitching {

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  object TestController extends BaseController with SessionProfile {
    override implicit val executionContext: ExecutionContext = ec
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector

    val authConnector: AuthClientConnector = mockAuthClientConnector

    def callAuthenticated: Action[AnyContent] = isAuthenticated {
      implicit request =>
        Future.successful(Ok("ALL GOOD"))
    }

    def callAuthenticatedButError: Action[AnyContent] = isAuthenticated {
      implicit request =>
        Future.failed(new Exception("Something wrong"))
    }

    def callAuthenticatedWithProfile(checkTrafficManagement: Boolean = true): Action[AnyContent] =
      isAuthenticatedWithProfile(checkTrafficManagement) {
        _ =>
          profile =>
            Future.successful(Ok(s"ALL GOOD with profile: ${profile.registrationId}"))
      }

    def callAuthenticatedWithProfileButError(checkTrafficManagement: Boolean = true): Action[AnyContent] =
      isAuthenticatedWithProfile(checkTrafficManagement) {
        _ =>
          profile =>
            Future.failed(new Exception(s"Something wrong for profile: ${profile.registrationId}"))
      }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(TrafficManagementPredicate)
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
      mockAuthenticated()

      val result = TestController.callAuthenticatedButError(FakeRequest())
      an[Exception] mustBe thrownBy(await(result))
    }
  }

  "isAuthenticatedWithProfile" when {
    "the traffic management FS is disabled" should {
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

    "the traffic management FS is enabled" when {
      "return 200 with a profile if user is Authenticated and TM check passes" in {
        enable(TrafficManagementPredicate)
        mockAuthenticated()
        mockWithCurrentProfile(Some(currentProfile))
        when(mockTrafficManagementService.passedTrafficManagement(ArgumentMatchers.eq(regId))(ArgumentMatchers.any()))
          .thenReturn(Future.successful(true))

        val result = TestController.callAuthenticatedWithProfile()(FakeRequest())
        status(result) mustBe OK
        contentAsString(result) mustBe s"ALL GOOD with profile: ${currentProfile.registrationId}"
      }

      "return 303 to start of journey if user is Authenticated and TM check fails" in {
        enable(TrafficManagementPredicate)
        mockAuthenticated()
        mockWithCurrentProfile(Some(currentProfile))
        when(mockTrafficManagementService.passedTrafficManagement(ArgumentMatchers.eq(regId))(ArgumentMatchers.any()))
          .thenReturn(Future.successful(false))

        val result = TestController.callAuthenticatedWithProfile()(FakeRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WelcomeController.show().url)
      }

      "return 303 to GG login if user has No Active Session" in {
        enable(TrafficManagementPredicate)
        mockNoActiveSession()

        val result = TestController.callAuthenticatedWithProfile()(FakeRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("http://localhost:9025/gg/sign-in?accountType=organisation&continue=http%3A%2F%2Flocalhost%3A9895%2Fregister-for-vat%2Fpost-sign-in&origin=vat-registration-frontend")
      }

      "return 500 if user is Not Authenticated" in {
        enable(TrafficManagementPredicate)
        mockNotAuthenticated()

        val result = TestController.callAuthenticatedWithProfile()(FakeRequest())
        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "return an Exception if something went wrong" in {
        enable(TrafficManagementPredicate)
        mockAuthenticatedOrg()
        mockWithCurrentProfile(Some(currentProfile))

        val result = TestController.callAuthenticatedWithProfileButError()(FakeRequest())
        an[Exception] mustBe thrownBy(await(result))
      }
    }

    "return 200 with a profile if user is Authenticated with no TM check" in {
      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      val result = TestController.callAuthenticatedWithProfile(checkTrafficManagement = false)(FakeRequest())
      status(result) mustBe OK
      contentAsString(result) mustBe s"ALL GOOD with profile: ${currentProfile.registrationId}"
    }

    "return 303 to GG login if user has No Active Session" in {
      mockNoActiveSession()

      val result = TestController.callAuthenticatedWithProfile(checkTrafficManagement = false)(FakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("http://localhost:9025/gg/sign-in?accountType=organisation&continue=http%3A%2F%2Flocalhost%3A9895%2Fregister-for-vat%2Fpost-sign-in&origin=vat-registration-frontend")
    }

    "return 500 if user is Not Authenticated" in {
      mockNotAuthenticated()

      val result = TestController.callAuthenticatedWithProfile(checkTrafficManagement = false)(FakeRequest())
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return an Exception if something went wrong" in {
      mockAuthenticatedOrg()
      mockWithCurrentProfile(Some(currentProfile))

      val result = TestController.callAuthenticatedWithProfileButError(checkTrafficManagement = false)(FakeRequest())
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
