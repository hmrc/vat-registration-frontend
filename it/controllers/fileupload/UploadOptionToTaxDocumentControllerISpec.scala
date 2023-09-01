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

import itutil.ControllerISpec
import models.api._
import models.external.upscan.{Ready, UpscanDetails}
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class UploadOptionToTaxDocumentControllerISpec extends ControllerISpec {

  val url: String = routes.UploadOptionToTaxDocumentController.show.url

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
      }
    }

    "return an OK when the user tries to upload a 1614H form" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.upscanInitiate(testReference)
        .upscanApi.storeUpscanReference(testReference, Attachment1614h)
        .upscanApi.fetchAllUpscanDetails(Nil)
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload), Some(false), Some(true), None)))

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
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

    "redirect to attachment summary page when the user tries to upload after answering no on a previous form" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload), Some(false), Some(false), None)))

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentUploadSummaryController.show.url)
      }
    }

    "redirect to attachment summary page when the user hasn't answered the previous question" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload), None, None, None)))

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentUploadSummaryController.show.url)
      }
    }
  }
}
