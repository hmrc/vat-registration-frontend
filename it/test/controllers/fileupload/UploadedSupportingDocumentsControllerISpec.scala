/*
 * Copyright 2026 HM Revenue & Customs
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

import controllers.fileupload.SupportingDocumentUploadController.maxSupportingDocuments
import itutil.ControllerISpec
import models.api.{LandPropertyOtherDocs, VAT5L}
import models.external.upscan.{FileStatus, InProgress, Ready, UploadDetails, UpscanDetails}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDateTime

class UploadedSupportingDocumentsControllerISpec extends ControllerISpec {

  val url: String = routes.UploadedSupportingDocumentsController.show.url

  def supportingDocument(fileName: String, fileStatus: FileStatus = Ready): UpscanDetails = UpscanDetails(
    attachmentType = LandPropertyOtherDocs,
    reference = s"$fileName-reference",
    fileStatus = fileStatus,
    uploadDetails = if (fileStatus == Ready) Some(UploadDetails(fileName, "image/gif", LocalDateTime.now(), "checksum", 100)) else None
  )

  val maxDocuments: List[UpscanDetails] = (1 to maxSupportingDocuments).map(index => supportingDocument(s"document-$index")).toList

  s"GET $url" must {
    "list only the uploaded supporting documents and ask whether to add another" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(List(
          supportingDocument("planning.pdf"),
          supportingDocument("letter.docx"),
          supportingDocument("never-uploaded", InProgress),
          supportingDocument("vat5l.pdf").copy(attachmentType = VAT5L)
        ))

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
        val doc = Jsoup.parse(res.body)
        doc.select(".govuk-summary-list__key").eachText() mustBe java.util.List.of("planning.pdf", "letter.docx")
        doc.select(".govuk-summary-list__actions a").first.attr("href") mustBe routes.RemoveSupportingDocumentController.show("planning.pdf-reference").url
        doc.select("input[type=radio]").size mustBe 2
      }
    }

    s"not ask whether to add another when $maxSupportingDocuments documents have been uploaded" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(maxDocuments)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe OK
        val doc = Jsoup.parse(res.body)
        doc.select(".govuk-summary-list__row").size mustBe maxSupportingDocuments
        doc.select("input[type=radio]").size mustBe 0
      }
    }

    "redirect to the upload page when no supporting documents have been uploaded" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(Nil)

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.SupportingDocumentUploadController.show.url)
      }
    }
  }

  s"POST $url" must {
    "redirect to the upload page when 'yes' is selected" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(List(supportingDocument("planning.pdf")))

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.SupportingDocumentUploadController.show.url)
    }

    "redirect to the VAT1614A page when 'no' is selected" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(List(supportingDocument("planning.pdf")))

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.VAT1614AController.show.url)
    }

    "return BAD_REQUEST with the list when nothing is selected" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(List(supportingDocument("planning.pdf")))

      val res: WSResponse = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
      val doc = Jsoup.parse(res.body)
      doc.select(".govuk-summary-list__key").text mustBe "planning.pdf"
      doc.select(".govuk-error-summary__list").text mustBe "Select yes if you need to upload another file"
    }

    s"redirect to the VAT1614A page without needing an answer when $maxSupportingDocuments documents have been uploaded" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(maxDocuments)

      val res: WSResponse = await(buildClient(url).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.VAT1614AController.show.url)
    }

    "redirect to the upload page when no supporting documents have been uploaded" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(Nil)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.SupportingDocumentUploadController.show.url)
    }
  }
}
