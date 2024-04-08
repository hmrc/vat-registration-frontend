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

class UploadSupportingDocumentControllerISpec extends ControllerISpec {

  val url: String = routes.UploadSupportingDocumentController.show.url

  val testReference = "testReference"

  val testAttachmentDetails: Attachments = Attachments(Some(Upload), None, None, Some(true))
  val supportingAttachmentDetails: UpscanDetails = UpscanDetails(LandPropertyOtherDocs, testReference, None, Ready, None, None)

  s"GET $url" must {
    "return an OK when there's fewer than 20 supporting attachments" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.upscanInitiate(testReference)
        .upscanApi.storeUpscanReference(testReference, LandPropertyOtherDocs)
        .upscanApi.fetchAllUpscanDetails(Nil)
        .registrationApi.getSection[Attachments](Some(testAttachmentDetails))

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
        .upscanApi.storeUpscanReference(testReference, LandPropertyOtherDocs)
        .upscanApi.fetchAllUpscanDetails(Nil)
        .registrationApi.getSection[Attachments](Some(testAttachmentDetails))

      val response: Future[WSResponse] = buildClient(s"$url?errorCode=EntityTooLarge").get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "redirect to attachment summary page when there's 20 supporting attachments" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(List.fill(20)(supportingAttachmentDetails))
        .registrationApi.getSection[Attachments](Some(testAttachmentDetails))

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentUploadSummaryController.show.url)
      }
    }

    "return an OK when the user hasn't answered the supporting documents question" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(Nil)
        .registrationApi.getSection[Attachments](Some(testAttachmentDetails.copy(supplySupportingDocuments = None)))

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentUploadSummaryController.show.url)
      }
    }
  }
}
