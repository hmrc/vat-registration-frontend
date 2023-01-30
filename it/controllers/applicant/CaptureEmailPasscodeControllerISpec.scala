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

package controllers.applicant

import featureswitch.core.config.StubEmailVerification
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, UkCompany}
import models.{ApplicantDetails, Contact}
import play.api.libs.json.{Format, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._


class CaptureEmailPasscodeControllerISpec extends ControllerISpec {

  private val testEmail = "test@test.com"
  private val testPasscode = "123456"

  val testApplicant: ApplicantDetails = validFullApplicantDetails.copy(contact = Contact(email = Some(testEmail)))

  "GET /email-address-verification" must {
    "show the view correctly" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(routes.CaptureEmailPasscodeController.show.url).get)

      res.status mustBe OK
    }

    "return INTERNAL_SERVER_ERROR if email missing" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant.copy(contact = Contact())))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(routes.CaptureEmailPasscodeController.show.url).get)
      res.status mustBe INTERNAL_SERVER_ERROR
    }
  }

  "GET /email-address-verification-retry" must {
    "show the view after requesting a passcode successfully" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      stubPost("/email-verification/request-passcode", CREATED, Json.obj().toString)

      val res: WSResponse = await(buildClient(routes.CaptureEmailPasscodeController.requestNew.url).get())

      res.status mustBe OK
    }

    "redirect to email verified after requesting a passcode and getting an email verified response" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant))
        .registrationApi.replaceSection[ApplicantDetails](testApplicant.copy(contact = Contact(Some(testEmail), None, Some(true))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      stubPost("/email-verification/request-passcode", CONFLICT, Json.obj().toString)

      val res: WSResponse = await(buildClient(routes.CaptureEmailPasscodeController.requestNew.url).get())

      res.header("LOCATION") mustBe Some(routes.EmailAddressVerifiedController.show.url)
      res.status mustBe SEE_OTHER
    }

    "redirect to max emails exceeded when use has hit max limit and forbidden to do futher email verifications" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant))
        .registrationApi.replaceSection[ApplicantDetails](testApplicant.copy(contact = Contact(Some(testEmail), None, Some(true))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      stubPost("/email-verification/request-passcode", FORBIDDEN, Json.obj().toString)

      val res: WSResponse = await(buildClient(routes.CaptureEmailPasscodeController.requestNew.url).get())

      res.status mustBe SEE_OTHER
      res.header("LOCATION") mustBe Some(controllers.errors.routes.EmailConfirmationCodeMaxAttemptsExceededController.show.url)
    }

    "return INTERNAL_SERVER_ERROR if email missing" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant.copy(contact = Contact())))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(routes.CaptureEmailPasscodeController.requestNew.url).get())
      res.status mustBe INTERNAL_SERVER_ERROR
    }
  }

  "POST /email-address-verification" when {
    "the feature switch is enabled" must {
      "verify the entered passcode against the stub and redirect to Email Verified page" in new Setup {
        disable(StubEmailVerification)
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[ApplicantDetails](Some(testApplicant.copy(contact = Contact(Some(testEmail)))))
          .registrationApi.replaceSection[ApplicantDetails](testApplicant.copy(contact = Contact(Some(testEmail), None, Some(true))))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", CREATED, Json.obj("passcode" -> testPasscode).toString)

        val res: WSResponse = await(buildClient(routes.CaptureEmailPasscodeController.submit(false).url).post(Map("email-passcode" -> testPasscode)))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(routes.EmailAddressVerifiedController.show.url)
      }

      "return BAD_REQUEST for an incorrect passcode" in new Setup {
        disable(StubEmailVerification)
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[ApplicantDetails](Some(testApplicant))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", NOT_FOUND, Json.obj("code" -> "PASSCODE_MISMATCH").toString)

        val res: WSResponse = await(buildClient(routes.CaptureEmailPasscodeController.submit(false).url).post(Map("email-passcode" -> testPasscode)))

        res.status mustBe BAD_REQUEST
      }

      "redirect to passcode not found error page if the passcode can't be found" in new Setup {
        disable(StubEmailVerification)
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[ApplicantDetails](Some(testApplicant))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", NOT_FOUND, Json.obj("code" -> "PASSCODE_NOT_FOUND").toString)

        val res: WSResponse = await(buildClient(routes.CaptureEmailPasscodeController.submit(false).url).post(Map("email-passcode" -> testPasscode)))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.errors.routes.EmailPasscodeNotFoundController.show(
          routes.CaptureEmailAddressController.show.url
        ).url)
      }

      "redirect to error page for exceeding the maximum number of passcode attempts" in new Setup {
        disable(StubEmailVerification)
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[ApplicantDetails](Some(testApplicant))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", FORBIDDEN, Json.obj("code" -> "MAX_PASSCODE_ATTEMPTS_EXCEEDED").toString)

        val res: WSResponse = await(buildClient(routes.CaptureEmailPasscodeController.submit(false).url).post(Map("email-passcode" -> testPasscode)))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.errors.routes.EmailPasscodesMaxAttemptsExceededController.show.url)
      }

      List("", "e" * 7).foreach { passcode =>
        s"return BAD_REQUEST for invalid passcode: '$passcode'" in new Setup {
          disable(StubEmailVerification)
          implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
          given()
            .user.isAuthorised()
            .registrationApi.getSection[ApplicantDetails](Some(testApplicant))
            .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

          insertCurrentProfileIntoDb(currentProfile, sessionId)
          val res: WSResponse = await(buildClient(routes.CaptureEmailPasscodeController.submit(false).url).post(Map("email-passcode" -> passcode)))
          res.status mustBe BAD_REQUEST
        }
      }
    }
  }
}
