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
import models.{AuthorisedEmployee, DeclarationCapacityAnswer, TransactorDetails}
import models.api.EligibilitySubmissionData
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class TransactorCaptureEmailAddressControllerISpec extends ControllerISpec {

  val url: String = controllers.registration.transactor.routes.TransactorCaptureEmailAddressController.show.url
  private val testEmail = "test@test.com"

  val s4lData = TransactorDetails(
    Some(testPersonalDetails),
    Some(true),
    Some(testCompanyName),
    Some("1234"),
    Some("test@test.com"),
    Some(true),
    Some(address),
    Some(DeclarationCapacityAnswer(AuthorisedEmployee))
  )

  s"GET $url" should {
    "show the view correctly" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(url).get)

      res.status mustBe OK

    }

    "returns an OK with prepopulated data" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .s4lContainer[TransactorDetails].contains(s4lData)
        .audit.writesAuditMerged()
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
    "TransactorDetails is not complete" should {
      "Update S4L and redirect to Transactor Capture Email Passcode page" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[TransactorDetails].contains(TransactorDetails())
          .s4lContainer[TransactorDetails].isUpdatedWith(
          TransactorDetails().copy(email = Some(testEmail))
        )
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/request-passcode", CREATED, Json.obj("email" -> testEmail, "serviceName" -> "VAT Registration").toString)

        val res: WSResponse = await(buildClient("/your-email-address").post(Map("email-address" -> Seq(testEmail))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.transactor.routes.TransactorCaptureEmailPasscodeController.show.url)
      }
      "Update S4L redirect to Transactor Capture Email Passcode page when the user has already verified" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[TransactorDetails].contains(TransactorDetails())
          .s4lContainer[TransactorDetails].isUpdatedWith(TransactorDetails().copy(email = Some(testEmail)))
          .s4lContainer[TransactorDetails].isUpdatedWith(
          TransactorDetails().copy(email = Some(testEmail), emailVerified = Some(true))
        )
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/request-passcode", CONFLICT, Json.obj().toString)

        val res: WSResponse = await(buildClient("/your-email-address").post(Map("email-address" -> Seq(testEmail))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.transactor.routes.TransactorEmailAddressVerifiedController.show.url)
      }
    }
    "TransactorDetails is complete" should {
      "Post the block to the backend and redirect to the Transactor Capture Email Passcode page" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[TransactorDetails].contains(validTransactorDetails.copy(email = None))
          .s4lContainer[TransactorDetails].clearedByKey
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.replaceSection[TransactorDetails](validTransactorDetails)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/request-passcode", CREATED, Json.obj("email" -> testEmail, "serviceName" -> "VAT Registration").toString)

        val res: WSResponse = await(buildClient("/your-email-address").post(Map("email-address" -> Seq(testEmail))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.transactor.routes.TransactorCaptureEmailPasscodeController.show.url)

      }
    }
  }

}
