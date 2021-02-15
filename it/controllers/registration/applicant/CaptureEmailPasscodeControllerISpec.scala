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

import featureswitch.core.config.StubEmailVerification
import itutil.ControllerISpec
import models.external.{EmailAddress, EmailVerified}
import models.view.ApplicantDetails
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._


class CaptureEmailPasscodeControllerISpec extends ControllerISpec {

  private val testEmail = "test@test.com"
  private val testPasscode = "123456"

  val s4lContents = ApplicantDetails(emailAddress = Some(EmailAddress(testEmail)))

  "GET /email-address-verification" should {
    "show the view correctly" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[ApplicantDetails].contains(s4lContents)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient("/email-address-verification").get)

      res.status mustBe OK
    }
  }

  "POST /email-address-verification" when {
    "the feature switch is enabled" should {
      "verify the entered passcode against the stub and redirect to Email Verified page" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[ApplicantDetails].contains(s4lContents)
          .s4lContainer[ApplicantDetails].isUpdatedWith(s4lContents.copy(emailVerified = Some(EmailVerified(true))))
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", CREATED, Json.obj("passcode" -> testPasscode).toString)

        val res: WSResponse = await(buildClient("/email-address-verification").post(Map("email-passcode" -> testPasscode)))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.applicant.routes.EmailAddressVerifiedController.show().url)
      }

      "return BAD_REQUEST for an incorrect passcode" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[ApplicantDetails].contains(s4lContents)
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", NOT_FOUND, Json.obj("code" -> "PASSCODE_MISMATCH").toString)

        val res: WSResponse = await(buildClient("/email-address-verification").post(Map("email-passcode" -> testPasscode)))

        res.status mustBe BAD_REQUEST
      }

      "return BAD_REQUEST when passcode is not found" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[ApplicantDetails].contains(s4lContents)
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", NOT_FOUND, Json.obj("code" -> "PASSCODE_NOT_FOUND").toString)

        val res: WSResponse = await(buildClient("/email-address-verification").post(Map("email-passcode" -> testPasscode)))

        res.status mustBe BAD_REQUEST
      }
      "redirect to error page for exceeding the maximum number of passcode attempts" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[ApplicantDetails].contains(s4lContents)
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", FORBIDDEN, Json.obj("code" -> "MAX_EMAILS_EXCEEDED").toString)

        val res: WSResponse = await(buildClient("/email-address-verification").post(Map("email-passcode" -> testPasscode)))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.applicant.errors.routes.EmailPasscodesMaxAttemptsExceededController.show().url)

      }
    }
  }

}
