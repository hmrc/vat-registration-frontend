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
import models.external.upscan.{InProgress, Ready, UploadDetails, UpscanDetails}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDateTime

class UploadedVat5LDocumentsControllerISpec extends ControllerISpec {

  val pageUrl: String = routes.UploadedVat5LDocumentsController.show.url
  val continueUrl: String = routes.UploadedVat5LDocumentsController.continue.url

  def attachmentDetails(attachmentType: AttachmentType, fileName: String = "test-file"): UpscanDetails = UpscanDetails(
    attachmentType = attachmentType,
    reference = s"$fileName-reference",
    fileStatus = Ready,
    uploadDetails = Some(UploadDetails(fileName, "image/gif", LocalDateTime.now(), "checksum", 100))
  )

  s"GET $pageUrl" must {
    "return OK with only the VAT5L related documents, including both 1614 forms, and no yes/no question" in new Setup {
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(List(
          attachmentDetails(PrimaryIdentityEvidence, "passport"),
          attachmentDetails(VAT5L, "vat5l"),
          attachmentDetails(LandPropertyOtherDocs, "supporting"),
          attachmentDetails(Attachment1614a, "vat1614a"),
          attachmentDetails(Attachment1614h, "vat1614h")
        ))
        .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(pageUrl).get())
      val doc = Jsoup.parse(res.body)

      res.status mustBe OK
      doc.select("tbody tr td:nth-child(2)").eachText() mustBe java.util.List.of("vat5l", "supporting", "vat1614a", "vat1614h")
      doc.select("input[type=radio]").size mustBe 0
      doc.select("form").attr("action") mustBe continueUrl
    }

    "return OK without listing uploads that never received a file" in new Setup {
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(List(
          attachmentDetails(VAT5L, "vat5l"),
          UpscanDetails(attachmentType = LandPropertyOtherDocs, reference = "uploading-reference", fileStatus = InProgress)
        ))
        .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(pageUrl).get())
      val doc = Jsoup.parse(res.body)

      res.status mustBe OK
      doc.select("tbody tr").size mustBe 1
      doc.select("tbody tr td:nth-child(2)").text mustBe "vat5l"
    }

    "redirect to Upload Document page if no VAT5L related documents are uploaded" in new Setup {
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(List(attachmentDetails(PrimaryIdentityEvidence)))
        .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(pageUrl).get())

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.UploadDocumentController.show.url)
    }

    "return server error if upload details missing" in new Setup {
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(List(UpscanDetails(attachmentType = VAT5L, reference = "test-reference", fileStatus = Ready)))
        .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(pageUrl).get())

      res.status mustBe INTERNAL_SERVER_ERROR
    }
  }

  s"POST $continueUrl" must {
    "redirect to Upload Document page for identity evidence if it is still outstanding" in new Setup {
      given()
        .user.isAuthorised()
        .attachmentsApi.getIncompleteAttachments(List(PrimaryIdentityEvidence))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(continueUrl).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.UploadDocumentController.show.url)
    }

    "redirect to Task List page when nothing is outstanding" in new Setup {
      given()
        .user.isAuthorised()
        .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(continueUrl).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }
  }
}
