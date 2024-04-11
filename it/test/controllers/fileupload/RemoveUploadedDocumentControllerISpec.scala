/*
 * Copyright 2024 HM Revenue & Customs
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
import models.api.PrimaryIdentityEvidence
import models.external.upscan.{Ready, UploadDetails, UpscanDetails}
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDateTime
import scala.concurrent.Future

class RemoveUploadedDocumentControllerISpec extends ControllerISpec {

  val testReference = "test-reference"

  def removeDocumentUrl(reference: String): String = routes.RemoveUploadedDocumentController.submit(reference).url

  val testUpscanDetails: UpscanDetails = UpscanDetails(
    attachmentType = PrimaryIdentityEvidence,
    reference = testReference,
    fileStatus = Ready,
    uploadDetails = Some(UploadDetails("test-file", "image/gif", LocalDateTime.now(), "checksum", 100))
  )

  s"GET ${removeDocumentUrl(testReference)}" must {
    "return OK" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails, reference = testReference)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(removeDocumentUrl(testReference)).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "redirect to the document upload summary page when document is missing" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(removeDocumentUrl(testReference)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentUploadSummaryController.show.url)
      }
    }
  }

  s"POST ${removeDocumentUrl(testReference)}" must {
    "return a redirect to summary page when user chooses 'Yes' and has been successfully removed" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .upscanApi.deleteUpscanDetails()
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails, reference = testReference)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(removeDocumentUrl(testReference)).post(Map("value" -> Seq("true")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentUploadSummaryController.show.url)
      }
    }
  }

  "return a redirect to summary page when user chooses 'No'" in new Setup {
    given()
      .audit.writesAudit()
      .audit.writesAuditMerged()
      .user.isAuthorised()
      .upscanApi.fetchUpscanFileDetails(testUpscanDetails, reference = testReference)

    insertCurrentProfileIntoDb(currentProfile, sessionString)

    val response: Future[WSResponse] = buildClient(removeDocumentUrl(testReference)).post(Map("value" -> Seq("false")))

    whenReady(response) { res =>
      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentUploadSummaryController.show.url)
    }
  }

  "return BAD_REQUEST if none of the option is selected" in new Setup {
    given()
      .audit.writesAudit()
      .audit.writesAuditMerged()
      .user.isAuthorised()
      .upscanApi.fetchUpscanFileDetails(testUpscanDetails, reference = testReference)

    insertCurrentProfileIntoDb(currentProfile, sessionString)

    val response: Future[WSResponse] = buildClient(removeDocumentUrl(testReference)).post(Map("value" -> Seq("")))

    whenReady(response) { res =>
      res.status mustBe BAD_REQUEST
    }
  }
}
