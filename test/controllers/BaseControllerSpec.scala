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

import config.FrontendAppConfig
import connectors.KeystoreConnector
import testHelpers.ControllerSpec
import play.api.test.FakeRequest
import services.SessionProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class BaseControllerSpec extends ControllerSpec {

  object TestController extends BaseController(messagesControllerComponents) with SessionProfile {
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector

    override implicit val appConfig = app.injector.instanceOf[FrontendAppConfig]

    val authConnector = mockAuthClientConnector

    def callAuthenticated = isAuthenticated {
      implicit request =>
        Future.successful(Ok("ALL GOOD"))
    }

    def callAuthenticatedButError = isAuthenticated {
      implicit request =>
        Future.failed(new Exception("Something wrong"))
    }

    def callAuthenticatedWithProfile = isAuthenticatedWithProfile {
      _ =>
        profile =>
          Future.successful(Ok(s"ALL GOOD with profile: ${profile.registrationId}"))
    }

    def callAuthenticatedWithProfileButError = isAuthenticatedWithProfile {
      _ =>
        profile =>
          Future.failed(new Exception(s"Something wrong for profile: ${profile.registrationId}"))
    }
  }

  "isAuthenticated" should {
    "return 200 if user is Authenticated and has org account" in {
      mockAuthenticatedOrg()

      val result = TestController.callAuthenticated(FakeRequest())
      status(result) mustBe OK
      contentAsString(result) mustBe "ALL GOOD"
    }

    "return 303 to post sign in if the user does not have an org affinity" in {
      mockAuthenticatedNonOrg()

      val result = TestController.callAuthenticated(FakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/register-for-vat/post-sign-in")
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

  "isAuthenticatedWithProfile" should {
    "return 200 with a profile if user is Authenticated" in {
      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      val result = TestController.callAuthenticatedWithProfile(FakeRequest())
      status(result) mustBe OK
      contentAsString(result) mustBe s"ALL GOOD with profile: ${currentProfile.registrationId}"
    }

    "return 303 to GG login if user has No Active Session" in {
      mockNoActiveSession()

      val result = TestController.callAuthenticatedWithProfile(FakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("http://localhost:9025/gg/sign-in?accountType=organisation&continue=http%3A%2F%2Flocalhost%3A9895%2Fregister-for-vat%2Fpost-sign-in&origin=vat-registration-frontend")
    }

    "return 500 if user is Not Authenticated" in {
      mockNotAuthenticated()

      val result = TestController.callAuthenticatedWithProfile(FakeRequest())
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return an Exception if something went wrong" in {
      mockAuthenticatedOrg()
      mockWithCurrentProfile(Some(currentProfile))

      val result = TestController.callAuthenticatedWithProfileButError(FakeRequest())
      an[Exception] mustBe thrownBy(await(result))
    }
  }

  "isAuthenticatedWithProfileNoStatusCheck" should {
    "return 200 with a profile if user is Authenticated" in {
      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      val result = TestController.callAuthenticatedWithProfile(FakeRequest())
      status(result) mustBe OK
      contentAsString(result) mustBe s"ALL GOOD with profile: ${currentProfile.registrationId}"
    }

    "return 303 to GG login if user has No Active Session" in {
      mockNoActiveSession()

      val result = TestController.callAuthenticatedWithProfile(FakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("http://localhost:9025/gg/sign-in?accountType=organisation&continue=http%3A%2F%2Flocalhost%3A9895%2Fregister-for-vat%2Fpost-sign-in&origin=vat-registration-frontend")
    }

    "return 500 if user is Not Authenticated" in {
      mockNotAuthenticated()

      val result = TestController.callAuthenticatedWithProfile(FakeRequest())
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return an Exception if something went wrong" in {
      mockAuthenticatedOrg()
      mockWithCurrentProfile(Some(currentProfile))

      val result = TestController.callAuthenticatedWithProfileButError(FakeRequest())
      an[Exception] mustBe thrownBy(await(result))
    }
  }


}
