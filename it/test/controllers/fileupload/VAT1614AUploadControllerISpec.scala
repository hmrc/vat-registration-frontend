/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.fileupload

import com.github.tomakehurst.wiremock.client.WireMock.{containing, matchingJsonPath, postRequestedFor, urlPathEqualTo, verify}
import itutil.ControllerISpec
import models.api._
import models.external.upscan.{Ready, UpscanDetails}
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class VAT1614AUploadControllerISpec extends ControllerISpec {

  val url: String = routes.VAT1614AUploadController.show.url

  val testReference = "testReference"

  s"GET $url" must {
    "return an OK when the user tries to upload a 1614A form" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.upscanInitiate(testReference)
        .upscanApi.storeUpscanReference(testReference, Attachment1614a)
        .upscanApi.fetchAllUpscanDetails(Nil)
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload), Some(true), None, None)))

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        verify(postRequestedFor(urlPathEqualTo("/upscan/v2/initiate"))
          .withRequestBody(matchingJsonPath("$.errorRedirect", containing(url))))
        verify(postRequestedFor(urlPathEqualTo("/upscan/v2/initiate"))
          .withRequestBody(matchingJsonPath("$.successRedirect", containing(routes.UploadingVat5LDocumentController.show.url))))
      }
    }

    "return an OK when the user tries to upload a 1614A form but there is already one uploaded" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val testUpscanDetails: UpscanDetails = UpscanDetails(Attachment1614a, testReference, None, Ready, None, None)
      given()
        .user.isAuthorised()
        .upscanApi.upscanInitiate(testReference)
        .upscanApi.storeUpscanReference(testReference, Attachment1614a)
        .upscanApi.fetchAllUpscanDetails(List(testUpscanDetails))
        .upscanApi.deleteUpscanDetails(testRegId, testReference)
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload), Some(true), None, None)))

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "return an OK when there's an errorCode" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.upscanInitiate(testReference)
        .upscanApi.storeUpscanReference(testReference, Attachment1614a)
        .upscanApi.fetchAllUpscanDetails(Nil)
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload), Some(true), None, None)))

      val response: Future[WSResponse] = buildClient(s"$url?errorCode=EntityTooLarge").get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "redirect to the attachment method page when no attachment method has been chosen" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(supplyVat1614a = Some(true))))

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.AttachmentMethodController.show.url)
      }
    }

    "redirect to the attachment method page when there is no attachments section" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](None)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.AttachmentMethodController.show.url)
      }
    }

    "redirect to uploaded VAT5L documents page when the user answered no to the 1614A question" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload), Some(false), None, None)))

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.UploadedVat5LDocumentsController.show.url)
      }
    }

    "redirect to uploaded VAT5L documents page when the user hasn't answered the 1614A question" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload), None, None, None)))

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.UploadedVat5LDocumentsController.show.url)
      }
    }
  }
}
