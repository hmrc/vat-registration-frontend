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

import connectors.RegistrationApiConnector.nrsSubmissionPayloadKey
import itutil.ControllerISpec
import models.ApiKey
import models.api.{Attachment1614a, Attachment1614h, ExtraIdentityEvidence, ExtraTransactorIdentityEvidence, IdentityEvidence, LandPropertyOtherDocs, LetterOfAuthority, OtherAttachments, PrimaryIdentityEvidence, PrimaryTransactorIdentityEvidence, TaxAgentAuthorisation, TaxRepresentativeAuthorisation, TransactorIdentityEvidence, VAT2, VAT51, VAT5L}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class SummaryControllerISpec extends ControllerISpec {

  "GET Summary page" should {
    "display the summary page correctly" in new Setup {
      implicit val key: ApiKey[String] = nrsSubmissionPayloadKey
      val nrsSubmissionPayload = "nrsSubmissionPayload"
      given()
        .user.isAuthorised()
        .registrationApi.replaceSectionWithoutCheckingData(nrsSubmissionPayload)
        .registrationApi.getRegistration(fullVatSchemeAttachment)
        .attachmentsApi.getAttachments(attachments = List(
          Attachment1614a,
          Attachment1614h
        ))
        .attachmentsApi.getIncompleteAttachments(attachments = List.empty)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient("/check-confirm-answers").get()
      whenReady(response) { res =>
        res.status mustBe 200
        val document = Jsoup.parse(res.body)
        document.title() mustBe "Check your answers before sending your application - Register for VAT - GOV.UK"
        document.select("h1").first().text() mustBe "Check your answers before sending your application"
      }
    }

    "display the summary page correctly for a NETP (Individual with no fixed establishment in UK)" in new Setup {
      implicit val key: ApiKey[String] = nrsSubmissionPayloadKey
      val nrsSubmissionPayload = "nrsSubmissionPayload"
      given()
        .user.isAuthorised()
        .registrationApi.replaceSectionWithoutCheckingData(nrsSubmissionPayload)
        .registrationApi.getRegistration(fullVatSchemeAttachmentNETP)
        .attachmentsApi.getAttachments(attachments = List(
          Attachment1614a,
          Attachment1614h
        ))
        .attachmentsApi.getIncompleteAttachments(attachments = List.empty)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

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
          .registrationApi.getRegistration(fullVatSchemeAttachment)
          .attachmentsApi.getAttachments(attachments = List(
            Attachment1614a,
            Attachment1614h
          ))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)
          .vatRegistration.submit(s"/vatreg/${fullVatSchemeAttachment.registrationId}/submit-registration", OK)

        insertCurrentProfileIntoDb(currentProfileIncorp, sessionString)

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
          .registrationApi.getRegistration(fullVatSchemeAttachment)
          .attachmentsApi.getAttachments(attachments = List(
            Attachment1614a,
            Attachment1614h
          ))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)
          .vatRegistration.submit(s"/vatreg/${fullVatSchemeAttachment.registrationId}/submit-registration", CONFLICT)

        insertCurrentProfileIntoDb(currentProfileIncorp, sessionString)

        val response: Future[WSResponse] = buildClient("/check-confirm-answers").post(Map("" -> Seq("")))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.alreadySubmitted.url)
        }
      }
    }

    "redirect to the submission in progress page" when {
      "the submission is already in progress" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getRegistration(fullVatSchemeAttachment)
          .attachmentsApi.getAttachments(attachments = List(
            Attachment1614a,
            Attachment1614h
          ))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)
          .vatRegistration.submit(s"/vatreg/${fullVatSchemeAttachment.registrationId}/submit-registration", TOO_MANY_REQUESTS)

        insertCurrentProfileIntoDb(currentProfileIncorp, sessionString)

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
          .registrationApi.getRegistration(fullVatSchemeAttachment)
          .attachmentsApi.getAttachments(attachments = List(
            Attachment1614a,
            Attachment1614h
          ))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)
          .vatRegistration.submit(s"/vatreg/${fullVatSchemeAttachment.registrationId}/submit-registration", BAD_REQUEST)

        insertCurrentProfileIntoDb(currentProfileIncorp, sessionString)

        val response: Future[WSResponse] = buildClient("/check-confirm-answers").post(Map("" -> Seq("")))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.submissionFailed.url)
        }
      }
    }

    "redirect to the submission failed retryable page" when {
      "the submission fails with a 500 series status" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getRegistration(fullVatSchemeAttachment)
          .attachmentsApi.getAttachments(attachments = List(
            Attachment1614a,
            Attachment1614h
          ))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)
          .vatRegistration.submit(s"/vatreg/${fullVatSchemeAttachment.registrationId}/submit-registration", INTERNAL_SERVER_ERROR)

        insertCurrentProfileIntoDb(currentProfileIncorp, sessionString)

        val response: Future[WSResponse] = buildClient("/check-confirm-answers").post(Map("" -> Seq("")))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.submissionRetryable.url)
        }
      }
    }

    "redirect to the contact OSH page" when {
      "the submission fails with a 500 series status" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getRegistration(fullVatSchemeAttachment)
          .attachmentsApi.getAttachments(attachments = List(
            Attachment1614a,
            Attachment1614h
          ))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)
          .vatRegistration.submit(s"/vatreg/${fullVatSchemeAttachment.registrationId}/submit-registration", UNPROCESSABLE_ENTITY)

        insertCurrentProfileIntoDb(currentProfileIncorp, sessionString)

        val response: Future[WSResponse] = buildClient("/check-confirm-answers").post(Map("" -> Seq("")))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.contact.url)
        }
      }
    }
  }
}