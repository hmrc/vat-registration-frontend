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

package controllers.transactor

import featureswitch.core.config.StubEmailVerification
import itutil.ControllerISpec
import models.TransactorDetails
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._


class TransactorCaptureEmailPasscodeControllerISpec extends ControllerISpec {

  private val testEmail = "test@test.com"
  private val testPasscode = "123456"

  val testTransactor = TransactorDetails(email = Some(testEmail))

  "GET /transactor-details/enter-the-verification-code" must {
    "show the view correctly" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[TransactorDetails].isEmpty
        .registrationApi.getSection[TransactorDetails](Some(testTransactor))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(routes.TransactorCaptureEmailPasscodeController.show.url).get)

      res.status mustBe OK
    }
  }


  "GET /email-address-verification-retry" must {
    "show the view after requesting a passcode successfully" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[TransactorDetails].isEmpty
        .registrationApi.getSection[TransactorDetails](Some(testTransactor))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      stubPost("/email-verification/request-passcode", CREATED, Json.obj().toString)

      val res: WSResponse = await(buildClient(routes.TransactorCaptureEmailPasscodeController.requestNew.url).get())

      res.status mustBe OK
    }

    "redirect to email verified after requesting a passcode and getting an email verified response" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[TransactorDetails].isEmpty
        .registrationApi.getSection[TransactorDetails](Some(testTransactor))
        .registrationApi.replaceSection[TransactorDetails](testTransactor.copy(emailVerified = Some(true)))
        .s4lContainer[TransactorDetails].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      stubPost("/email-verification/request-passcode", CONFLICT, Json.obj().toString)

      val res: WSResponse = await(buildClient(routes.TransactorCaptureEmailPasscodeController.requestNew.url).get())

      res.header("LOCATION") mustBe Some(routes.TransactorEmailAddressVerifiedController.show.url)
      res.status mustBe SEE_OTHER
    }

    "return INTERNAL_SERVER_ERROR if email missing" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[TransactorDetails].isEmpty
        .registrationApi.getSection[TransactorDetails](Some(testTransactor.copy(email = None)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(routes.TransactorCaptureEmailPasscodeController.requestNew.url).get())

      res.status mustBe INTERNAL_SERVER_ERROR
    }
  }

  "POST /transactor-details/enter-the-verification-code" when {
    "the email verification feature switch is enabled" must {
      "verify the entered passcode and redirect to Transactor Email Verified page" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised()
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](Some(testTransactor))
          .registrationApi.replaceSection[TransactorDetails](testTransactor.copy(emailVerified = Some(true)))
          .s4lContainer[TransactorDetails].clearedByKey

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", CREATED, Json.obj("passcode" -> testPasscode).toString)

        val res: WSResponse = await(buildClient(routes.TransactorCaptureEmailPasscodeController.submit(false).url).post(Map("email-passcode" -> testPasscode)))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(routes.TransactorEmailAddressVerifiedController.show.url)
      }

      "return BAD_REQUEST for an incorrect passcode" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised()
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](Some(testTransactor))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", NOT_FOUND, Json.obj("code" -> "PASSCODE_MISMATCH").toString)

        val res: WSResponse = await(buildClient(routes.TransactorCaptureEmailPasscodeController.submit(false).url).post(Map("email-passcode" -> testPasscode)))

        res.status mustBe BAD_REQUEST
      }

      "redirect to passcode not found error page if the passcode can't be found" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised()
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](Some(testTransactor))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", NOT_FOUND, Json.obj("code" -> "PASSCODE_NOT_FOUND").toString)

        val res: WSResponse = await(buildClient(routes.TransactorCaptureEmailPasscodeController.submit(false).url).post(Map("email-passcode" -> testPasscode)))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.errors.routes.EmailPasscodeNotFoundController.show(
          routes.TransactorCaptureEmailAddressController.show.url
        ).url)
      }

      "redirect to error page for exceeding the maximum number of passcode attempts" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised()
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](Some(testTransactor))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", FORBIDDEN, Json.obj("code" -> "MAX_PASSCODE_ATTEMPTS_EXCEEDED").toString)

        val res: WSResponse = await(buildClient(routes.TransactorCaptureEmailPasscodeController.submit(false).url).post(Map("email-passcode" -> testPasscode)))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.errors.routes.EmailPasscodesMaxAttemptsExceededController.show.url)
      }

      "return BAD_REQUEST for invalid passcode submitted" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised()
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](Some(testTransactor))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(routes.TransactorCaptureEmailPasscodeController.submit(false).url).post(Map("email-passcode" -> "p" * 10)))

        res.status mustBe BAD_REQUEST
      }

      "return INTERNAL_SERVER for valid passcode submitted with no email available" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised()
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](Some(testTransactor.copy(email = None)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", CREATED, Json.obj("passcode" -> testPasscode).toString)

        val res: WSResponse = await(buildClient(routes.TransactorCaptureEmailPasscodeController.submit(false).url).post(Map("email-passcode" -> testPasscode)))

        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
