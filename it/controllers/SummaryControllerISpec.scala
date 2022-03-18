/*
 * Copyright 2017 HM Revenue & Customs
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

import itutil.ControllerISpec
import models.{Director, Email, SicAndCompliance}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import support.RegistrationsApiStubs

import scala.collection.JavaConverters._
import scala.concurrent.Future
import play.api.libs.json.Json

class SummaryControllerISpec extends ControllerISpec with RegistrationsApiStubs {

  "GET Summary page" should {
    "display the summary page correctly" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[SicAndCompliance].cleared
        .vatRegistration.storesNrsPayload(testRegId)
        .vatScheme.has("eligibility-data", fullEligibilityDataJson)

      specificRegistrationApi(testRegId).GET.respondsWith(OK, Some(Json.toJson(fullVatScheme)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/check-confirm-answers").get()
      whenReady(response) { res =>
        res.status mustBe 200
        val document = Jsoup.parse(res.body)
        document.title() mustBe "Check your answers before sending your application - Register for VAT - GOV.UK"
        document.select("h1").first().text() mustBe "Check your answers before sending your application"
      }
    }

    "display the summary page correctly for a NETP" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[SicAndCompliance].cleared
        .vatRegistration.storesNrsPayload(testRegId)
        .vatScheme.has("eligibility-data", fullEligibilityDataJson)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      specificRegistrationApi(testRegId).GET.respondsWith(OK, Some(Json.toJson(fullNetpVatScheme)))

      val response: Future[WSResponse] = buildClient("/check-confirm-answers").get()
      whenReady(response) { res =>
        res.status mustBe 200
        val document = Jsoup.parse(res.body)
        document.title() mustBe "Check your answers before sending your application - Register for VAT - GOV.UK"
        document.select("h1").first().text() mustBe "Check your answers before sending your application"
      }
    }
  }

  "POST Summary Page" should {
    "redirect to the confirmation page" when {
      "the submission succeeds" in new Setup {
        given()
          .user.isAuthorised()
          .vatScheme.contains(vatReg)
          .vatRegistration.submit(s"/vatreg/${vatReg.id}/submit-registration", OK)

        insertCurrentProfileIntoDb(currentProfileIncorp, sessionId)

        val response: Future[WSResponse] = buildClient("/check-confirm-answers").post(Map("" -> Seq("")))
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ApplicationSubmissionController.show.url)
        }
      }
    }

    "redirect to the already submitted kickout page" when {
      "the submission is already submitted" in new Setup {
        given()
          .user.isAuthorised()
          .vatScheme.contains(vatReg)
          .vatRegistration.submit(s"/vatreg/${vatReg.id}/submit-registration", CONFLICT)

        insertCurrentProfileIntoDb(currentProfileIncorp, sessionId)

        val response: Future[WSResponse] = buildClient("/check-confirm-answers").post(Map("" -> Seq("")))
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ErrorController.alreadySubmitted.url)
        }
      }
    }

    "redirect to the submission in progress page" when {
      "the submission is already in progress" in new Setup {
        given()
          .user.isAuthorised()
          .vatScheme.contains(vatReg)
          .vatRegistration.submit(s"/vatreg/${vatReg.id}/submit-registration", TOO_MANY_REQUESTS)

        insertCurrentProfileIntoDb(currentProfileIncorp, sessionId)

        val response: Future[WSResponse] = buildClient("/check-confirm-answers").post(Map("" -> Seq("")))
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SubmissionInProgressController.show.url)
        }
      }
    }

    "redirect to the submission failed page" when {
      "the submission failed with a bad request" in new Setup {
        given()
          .user.isAuthorised()
          .vatScheme.contains(vatReg)
          .vatRegistration.submit(s"/vatreg/${vatReg.id}/submit-registration", BAD_REQUEST)

        insertCurrentProfileIntoDb(currentProfileIncorp, sessionId)

        val response: Future[WSResponse] = buildClient("/check-confirm-answers").post(Map("" -> Seq("")))
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ErrorController.submissionFailed.url)
        }
      }
    }

    "redirect to the submission failed retryable page" when {
      "the submission fails with a 500 series status" in new Setup {
        given()
          .user.isAuthorised()
          .vatScheme.contains(vatReg)
          .vatRegistration.submit(s"/vatreg/${vatReg.id}/submit-registration", INTERNAL_SERVER_ERROR)

        insertCurrentProfileIntoDb(currentProfileIncorp, sessionId)

        val response: Future[WSResponse] = buildClient("/check-confirm-answers").post(Map("" -> Seq("")))
        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ErrorController.submissionRetryable.url)
        }
      }
    }
  }
}