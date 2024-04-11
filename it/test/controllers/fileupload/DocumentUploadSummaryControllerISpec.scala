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
import models.api._
import models.external.upscan.{Ready, UploadDetails, UpscanDetails}
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDateTime

class DocumentUploadSummaryControllerISpec extends ControllerISpec {

  val pageUrl: String = routes.DocumentUploadSummaryController.show.url
  val continueUrl: String = routes.DocumentUploadSummaryController.continue.url

  val testUpscanDetails = List(
    UpscanDetails(
      attachmentType = PrimaryIdentityEvidence,
      reference = "tes-reference",
      fileStatus = Ready,
      uploadDetails = Some(UploadDetails("test-file", "image/gif", LocalDateTime.now(), "checksum", 100))
    )
  )
  val testUpscanDetailsWithMissingUploadDetails = List(
    UpscanDetails(attachmentType = PrimaryIdentityEvidence, reference = "tes-reference", fileStatus = Ready)
  )

  def attachmentDetails(attachmentType: AttachmentType): UpscanDetails = UpscanDetails(
    attachmentType = attachmentType,
    reference = "tes-reference",
    fileStatus = Ready,
    uploadDetails = Some(UploadDetails("test-file", "image/gif", LocalDateTime.now(), "checksum", 100))
  )

  s"GET $pageUrl" when {
    "the user has 1 or more documents uploaded" must {
      "return OK with the view" in new Setup {
        given
          .user.isAuthorised()
          .upscanApi.fetchAllUpscanDetails(testUpscanDetails)
          .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])
          .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(pageUrl).get)

        res.status mustBe OK
      }

      "return server error if upload details missing" in new Setup {
        given
          .user.isAuthorised()
          .upscanApi.fetchAllUpscanDetails(testUpscanDetailsWithMissingUploadDetails)
          .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])
          .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(pageUrl).get)

        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  s"POST $continueUrl" must {
    "redirect to Upload Document page if not all required attachments are uploaded" in new Setup {
      given
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(testUpscanDetails)
        .attachmentsApi.getIncompleteAttachments(List(VAT5L))
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(continueUrl).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.UploadDocumentController.show.url)
    }

    "redirect to Supply 1614A page if 1614 info is required but incomplete" in new Setup {
      given
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(testUpscanDetails :+ attachmentDetails(VAT5L))
        .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(continueUrl).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.Supply1614AController.show.url)
    }

    "redirect to Supply Supporting Documents page if they are required but none are present" in new Setup {
      given
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(testUpscanDetails :+ attachmentDetails(VAT5L) :+ attachmentDetails(Attachment1614a))
        .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload), Some(true), None, Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(continueUrl).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.SupplySupportingDocumentsController.show.url)
    }

    "redirect to Task List page if all option to tax conditions are met" in new Setup {
      given
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(testUpscanDetails :+ attachmentDetails(VAT5L) :+ attachmentDetails(LandPropertyOtherDocs))
        .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload), Some(false), Some(false), Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(continueUrl).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }
  }

  s"POST $pageUrl" must {
    "redirect to Upload Supporting Document page if Yes is selected" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given
        .user.isAuthorised()

      val res: WSResponse = await(buildClient(pageUrl).post(Map("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.UploadSupportingDocumentController.show.url)
    }

    "redirect to Task list page if No is selected" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given
        .user.isAuthorised()

      val res: WSResponse = await(buildClient(pageUrl).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }

    "return a 400 if nothing is selected" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(testUpscanDetails)
        .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])

      val res: WSResponse = await(buildClient(pageUrl).post(Map[String, String]()))

      res.status mustBe BAD_REQUEST
    }
  }
}
