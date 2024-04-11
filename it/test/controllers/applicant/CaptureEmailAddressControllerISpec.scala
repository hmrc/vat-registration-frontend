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

import featuretoggle.FeatureSwitch.StubEmailVerification
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, UkCompany}
import models.{ApplicantDetails, Contact}
import org.jsoup.Jsoup
import play.api.libs.json.{Format, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class CaptureEmailAddressControllerISpec extends ControllerISpec {

  val url: String = controllers.applicant.routes.CaptureEmailAddressController.show.url
  private val testEmail = "test@test.com"

  val testApplicant: ApplicantDetails = validFullApplicantDetails.copy(
    entity = Some(testIncorpDetails),
    personalDetails = Some(testPersonalDetails),
    contact = Contact(
      Some(testEmail),
      emailVerified = Some(true)
    )
  )

  s"GET $url" must {
    "show the view correctly" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get)

      res.status mustBe OK

    }

    "returns an OK with pre-populated data" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("email-address").attr("value") mustBe testEmail
      }
    }
  }

  s"POST $url" must {
    "Update model and redirect to Capture Email Passcode page" in new Setup {
      disable(StubEmailVerification)
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .registrationApi.replaceSection[ApplicantDetails](
        ApplicantDetails(contact = Contact(Some(testEmail)))
      )
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      stubPost("/email-verification/request-passcode", CREATED, Json.obj("email" -> testEmail, "serviceName" -> "VAT Registration").toString)

      val res: WSResponse = await(buildClient("/email-address").post(Map("email-address" -> Seq(testEmail))))

      res.status mustBe SEE_OTHER
      res.header("LOCATION") mustBe Some(controllers.applicant.routes.CaptureEmailPasscodeController.show.url)
    }

    "Update model and redirect to Email Address Verified page when the user has already verified" in new Setup {
      disable(StubEmailVerification)
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .registrationApi.replaceSection[ApplicantDetails](
        ApplicantDetails(contact = Contact(Some(testEmail)))
      )
        .registrationApi.getSection[ApplicantDetails](Some(ApplicantDetails(contact = Contact(Some(testEmail)))))
        .registrationApi.replaceSection[ApplicantDetails](
        ApplicantDetails(contact = Contact(Some(testEmail), emailVerified = Some(true)))
      )
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      stubPost("/email-verification/request-passcode", CONFLICT, Json.obj().toString)

      val res: WSResponse = await(buildClient("/email-address").post(Map("email-address" -> Seq(testEmail))))

      res.status mustBe SEE_OTHER
      res.header("LOCATION") mustBe Some(controllers.applicant.routes.EmailAddressVerifiedController.show.url)
    }

    "Update model and redirect to 'Email confirmation code max attempt exceeded' page when the user has already verified" in new Setup {
      disable(StubEmailVerification)
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .registrationApi.replaceSection[ApplicantDetails](
        ApplicantDetails(contact = Contact(Some(testEmail)))
      )
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      stubPost("/email-verification/request-passcode", FORBIDDEN, Json.obj().toString)

      val res: WSResponse = await(buildClient("/email-address").post(Map("email-address" -> Seq(testEmail))))

      res.status mustBe SEE_OTHER
      res.header("LOCATION") mustBe Some(controllers.errors.routes.EmailConfirmationCodeMaxAttemptsExceededController.show.url)
    }

    "Update model and redirect to Capture Telephone Number page when the user is a transactor" in new Setup {
      disable(StubEmailVerification)
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .registrationApi.replaceSection[ApplicantDetails](
        ApplicantDetails(contact = Contact(Some(testEmail)))
      )
        .registrationApi.getSection[ApplicantDetails](Some(ApplicantDetails(contact = Contact(Some(testEmail)))))
        .registrationApi.replaceSection[ApplicantDetails](
        ApplicantDetails(contact = Contact(Some(testEmail), emailVerified = Some(false)))
      )
        .registrationApi.getSection[EligibilitySubmissionData](Some(
        testEligibilitySubmissionData.copy(
          isTransactor = true
        )
      ))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      stubPost("/email-verification/request-passcode", CONFLICT, Json.obj().toString)

      val res: WSResponse = await(buildClient("/email-address").post(Map("email-address" -> Seq(testEmail))))

      res.status mustBe SEE_OTHER
      res.header("LOCATION") mustBe Some(controllers.applicant.routes.CaptureTelephoneNumberController.show.url)
    }
    List("", "e" * 133, "test-email").foreach { email =>
      s"return BAD_REQUEST for invalid email: '$email'" in new Setup {
        disable(StubEmailVerification)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionString)
        val res: WSResponse = await(buildClient("/email-address").post(Map("email-address" -> email)))
        res.status mustBe BAD_REQUEST
      }
    }
  }

}
