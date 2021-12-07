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

package controllers.registration.transactor

import featureswitch.core.config.StubEmailVerification
import itutil.ControllerISpec
import models.{ApplicantDetails, TransactorDetails}
import models.api.EligibilitySubmissionData
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._


class TransactorCaptureEmailPasscodeControllerISpec extends ControllerISpec {

  private val testEmail = "test@test.com"
  private val testPasscode = "123456"

  val s4lContents = TransactorDetails(email = Some(testEmail))

  "GET /transactor-details/enter-the-verification-code" should {
    "show the view correctly" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[TransactorDetails].contains(s4lContents)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient("/enter-the-verification-code").get)

      res.status mustBe OK
    }
  }

  "POST /transactor-details/enter-the-verification-code" when {
    "the email verification feature switch is enabled" should {
      "verify the entered passcode against the stub and redirect to Transactor Email Verified page" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[TransactorDetails].contains(s4lContents)
          .s4lContainer[TransactorDetails].isUpdatedWith(s4lContents.copy(emailVerified = Some(true)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", CREATED, Json.obj("passcode" -> testPasscode).toString)

        val res: WSResponse = await(buildClient("/enter-the-verification-code").post(Map("email-passcode" -> testPasscode)))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.transactor.routes.TransactorEmailAddressVerifiedController.show.url)
      }

      "return BAD_REQUEST for an incorrect passcode" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[TransactorDetails].contains(s4lContents)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", NOT_FOUND, Json.obj("code" -> "PASSCODE_MISMATCH").toString)

        val res: WSResponse = await(buildClient("/enter-the-verification-code").post(Map("email-passcode" -> testPasscode)))

        res.status mustBe BAD_REQUEST
      }

      "redirect to passcode not found error page if the passcode can't be found" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[TransactorDetails].contains(s4lContents)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", NOT_FOUND, Json.obj("code" -> "PASSCODE_NOT_FOUND").toString)

        val res: WSResponse = await(buildClient("/enter-the-verification-code").post(Map("email-passcode" -> testPasscode)))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.errors.routes.EmailPasscodeNotFoundController.show(
          controllers.registration.transactor.routes.TransactorCaptureEmailAddressController.show.url
        ).url)
      }

      "redirect to error page for exceeding the maximum number of passcode attempts" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[TransactorDetails].contains(s4lContents)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", FORBIDDEN, Json.obj("code" -> "MAX_PASSCODE_ATTEMPTS_EXCEEDED").toString)

        val res: WSResponse = await(buildClient("/enter-the-verification-code").post(Map("email-passcode" -> testPasscode)))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.errors.routes.EmailPasscodesMaxAttemptsExceededController.show.url)
      }
    }
  }

}
