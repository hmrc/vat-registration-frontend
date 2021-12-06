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
import models.ApplicantDetails
import models.api.EligibilitySubmissionData
import models.external.{EmailAddress, EmailVerified}
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class CaptureEmailAddressControllerISpec extends ControllerISpec {

  val url: String = controllers.registration.applicant.routes.CaptureEmailAddressController.show.url
  private val testEmail = "test@test.com"

  val s4lData = ApplicantDetails(
    entity = Some(testIncorpDetails),
    personalDetails = Some(testPersonalDetails),
    emailAddress = Some(EmailAddress(testEmail)),
    emailVerified = Some(EmailVerified(true))
  )

  s"GET $url" should {
    "show the view correctly" in new Setup {
      given()
        .user.isAuthorised
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(url).get)

      res.status mustBe OK

    }

    "returns an OK with prepopulated data" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[ApplicantDetails].contains(s4lData)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("email-address").attr("value") mustBe testEmail
      }
    }
  }

  s"POST $url" when {
    "ApplicantDetails is not complete" should {
      "Update S4L and redirect to Capture Email Passcode page" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .s4lContainer[ApplicantDetails].isUpdatedWith(
          ApplicantDetails().copy(emailAddress = Some(EmailAddress(testEmail)))
        )
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/request-passcode", CREATED, Json.obj("email" -> testEmail, "serviceName" -> "VAT Registration").toString)

        val res: WSResponse = await(buildClient("/email-address").post(Map("email-address" -> Seq(testEmail))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.applicant.routes.CaptureEmailPasscodeController.show.url)
      }
      "Update S4L redirect to Capture Email Passcode page when the user has already verified" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails().copy(emailAddress = Some(EmailAddress(testEmail))))
          .s4lContainer[ApplicantDetails].isUpdatedWith(
          ApplicantDetails().copy(emailAddress = Some(EmailAddress(testEmail)), emailVerified = Some(EmailVerified(true)))
        )
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/request-passcode", CONFLICT, Json.obj().toString)

        val res: WSResponse = await(buildClient("/email-address").post(Map("email-address" -> Seq(testEmail))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.applicant.routes.EmailAddressVerifiedController.show.url)
      }
    }
    "ApplicantDetails is complete" should {
      "Post the block to the backend and redirect to the capture email postcode page" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails.copy(emailAddress = None))
          .vatScheme.patched("applicant-details", Json.toJson(validFullApplicantDetails)(ApplicantDetails.writes))
          .s4lContainer[ApplicantDetails].clearedByKey
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/request-passcode", CREATED, Json.obj("email" -> testEmail, "serviceName" -> "VAT Registration").toString)

        val res: WSResponse = await(buildClient("/email-address").post(Map("email-address" -> Seq(testEmail))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.applicant.routes.CaptureEmailPasscodeController.show.url)

      }
    }
  }

}
