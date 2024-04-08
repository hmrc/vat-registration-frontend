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

import featuretoggle.FeatureSwitch.StubEmailVerification
import itutil.ControllerISpec
import models.{AuthorisedEmployee, DeclarationCapacityAnswer, TransactorDetails}
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class TransactorCaptureEmailAddressControllerISpec extends ControllerISpec {

  val url: String = controllers.transactor.routes.TransactorCaptureEmailAddressController.show.url
  private val testEmail = "test@test.com"

  val testTransactor: TransactorDetails = TransactorDetails(
    Some(testPersonalDetails),
    Some(true),
    Some(testCompanyName),
    Some("1234"),
    Some("test@test.com"),
    Some(true),
    Some(address),
    Some(DeclarationCapacityAnswer(AuthorisedEmployee))
  )

  s"GET $url" must {
    "show the view correctly" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[TransactorDetails](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get)

      res.status mustBe OK

    }

    "returns an OK with prepopulated data" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[TransactorDetails](Some(testTransactor))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("email-address").attr("value") mustBe testEmail
      }
    }
  }

  s"POST $url" when {
    "TransactorDetails is not complete" must {
      "Update backend and redirect to Transactor Capture Email Passcode page" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised()
          .registrationApi.getSection[TransactorDetails](None)
          .registrationApi.replaceSection[TransactorDetails](TransactorDetails(email = Some(testEmail)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        stubPost("/email-verification/request-passcode", CREATED, Json.obj("email" -> testEmail, "serviceName" -> "VAT Registration").toString)

        val res: WSResponse = await(buildClient("/your-email-address").post(Map("email-address" -> Seq(testEmail))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.transactor.routes.TransactorCaptureEmailPasscodeController.show.url)
      }
      "Update backend and redirect to Transactor Capture Email Passcode page when the user has already verified" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised()
          .registrationApi.getSection[TransactorDetails](None)
          .registrationApi.replaceSection[TransactorDetails](TransactorDetails(email = Some(testEmail)))
          .registrationApi.getSection[TransactorDetails](Some(TransactorDetails(email = Some(testEmail))))
          .registrationApi.replaceSection[TransactorDetails](TransactorDetails(email = Some(testEmail), emailVerified = Some(true)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        stubPost("/email-verification/request-passcode", CONFLICT, Json.obj().toString)

        val res: WSResponse = await(buildClient("/your-email-address").post(Map("email-address" -> Seq(testEmail))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.transactor.routes.TransactorEmailAddressVerifiedController.show.url)
      }
      "Update backend and redirect to 'Email Confirmation Code Max Attempts Exceeded' page when the user has tried to verify too many times" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised()
          .registrationApi.getSection[TransactorDetails](None)
          .registrationApi.replaceSection[TransactorDetails](TransactorDetails(email = Some(testEmail)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        stubPost("/email-verification/request-passcode", FORBIDDEN, Json.obj().toString)

        val res: WSResponse = await(buildClient("/your-email-address").post(Map("email-address" -> Seq(testEmail))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.errors.routes.EmailConfirmationCodeMaxAttemptsExceededController.show.url)
      }
    }
    "TransactorDetails is complete" must {
      "Post the block to the backend and redirect to the Transactor Capture Email Passcode page" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised()
          .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails.copy(email = None)))
          .registrationApi.replaceSection[TransactorDetails](validTransactorDetails)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        stubPost("/email-verification/request-passcode", CREATED, Json.obj("email" -> testEmail, "serviceName" -> "VAT Registration").toString)

        val res: WSResponse = await(buildClient("/your-email-address").post(Map("email-address" -> Seq(testEmail))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.transactor.routes.TransactorCaptureEmailPasscodeController.show.url)

      }

      "return BAD_REQUEST if any of the validation fails for submitted email address" in new Setup {
        given().user.isAuthorised()
        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = buildClient(url).post("")
        whenReady(res) { res =>
          res.status mustBe BAD_REQUEST
        }
      }
    }
  }

}
