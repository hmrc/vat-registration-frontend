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
import controllers.fileupload.SupportingDocumentUploadController.maxSupportingDocuments
import itutil.ControllerISpec
import models.api._
import models.external.upscan.{InProgress, Ready, UploadDetails, UpscanDetails}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDateTime
import scala.concurrent.Future

class SupportingDocumentUploadControllerISpec extends ControllerISpec {

  val url: String = routes.SupportingDocumentUploadController.show.url
  val continueUrl: String = routes.SupportingDocumentUploadController.continue.url

  val testReference = "testReference"
  val supportingDocumentsAnswered: Attachments = Attachments(Some(Upload), None, None, Some(true))

  def supportingDocument(fileName: String, fileStatus: models.external.upscan.FileStatus = Ready): UpscanDetails = UpscanDetails(
    attachmentType = LandPropertyOtherDocs,
    reference = s"$fileName-reference",
    fileStatus = fileStatus,
    uploadDetails = if (fileStatus == Ready) Some(UploadDetails(fileName, "image/gif", LocalDateTime.now(), "checksum", 100)) else None
  )

  s"GET $url" must {
    "return OK with the upload form and no list when nothing has been uploaded yet" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.upscanInitiate(testReference)
        .upscanApi.storeUpscanReference(testReference, LandPropertyOtherDocs)
        .upscanApi.fetchAllUpscanDetails(Nil)
        .registrationApi.getSection[Attachments](Some(supportingDocumentsAnswered))

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        val doc = Jsoup.parse(res.body)
        doc.select(".govuk-summary-list__row").size mustBe 0
        doc.select("input[type=file]").size mustBe 1
        doc.select("#file-upload-button").text mustBe "Upload"
        verify(postRequestedFor(urlPathEqualTo("/upscan/v2/initiate"))
          .withRequestBody(matchingJsonPath("$.errorRedirect", containing(url))))
        verify(postRequestedFor(urlPathEqualTo("/upscan/v2/initiate"))
          .withRequestBody(matchingJsonPath("$.successRedirect", containing(routes.UploadingVat5LDocumentController.show.url))))
      }
    }

    "return OK listing the uploaded supporting documents, ignoring uploads that never received a file" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.upscanInitiate(testReference)
        .upscanApi.storeUpscanReference(testReference, LandPropertyOtherDocs)
        .upscanApi.fetchAllUpscanDetails(List(
          supportingDocument("planning.pdf"),
          supportingDocument("letter.docx"),
          supportingDocument("never-uploaded", InProgress)
        ))
        .registrationApi.getSection[Attachments](Some(supportingDocumentsAnswered))

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        val doc = Jsoup.parse(res.body)
        doc.select(".govuk-summary-list__key").eachText() mustBe java.util.List.of("planning.pdf", "letter.docx")
        doc.select(".govuk-summary-list__actions a").first.attr("href") mustBe routes.RemoveSupportingDocumentController.show("planning.pdf-reference").url
        doc.select("#file-upload-button").text mustBe "Upload another supporting document"
      }
    }

    s"return OK without the upload form when $maxSupportingDocuments supporting documents have been uploaded" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails((1 to maxSupportingDocuments).map(index => supportingDocument(s"document-$index")).toList)
        .registrationApi.getSection[Attachments](Some(supportingDocumentsAnswered))

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        val doc = Jsoup.parse(res.body)
        doc.select(".govuk-summary-list__row").size mustBe maxSupportingDocuments
        doc.select("input[type=file]").size mustBe 0
        verify(0, postRequestedFor(urlPathEqualTo("/upscan/v2/initiate")))
      }
    }

    "return OK with the error when there's an errorCode" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.upscanInitiate(testReference)
        .upscanApi.storeUpscanReference(testReference, LandPropertyOtherDocs)
        .upscanApi.fetchAllUpscanDetails(Nil)
        .registrationApi.getSection[Attachments](Some(supportingDocumentsAnswered))

      val response: Future[WSResponse] = buildClient(s"$url?errorCode=Quarantine").get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).select(".govuk-error-summary__list").text mustBe "The selected file contains a virus"
      }
    }

    "redirect to the VAT1614A page when the user hasn't answered yes to the supporting documents question" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Upload))))

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.VAT1614AController.show.url)
      }
    }

    "redirect to the attachment method page when no attachment method has been chosen" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Attachments](Some(Attachments(supplySupportingDocuments = Some(true))))

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
  }

  s"POST $continueUrl" must {
    "redirect to the VAT1614A page when at least one supporting document has been uploaded" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(List(supportingDocument("planning.pdf")))

      val res: WSResponse = await(buildClient(continueUrl).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.VAT1614AController.show.url)
    }

    "return to the page with the 'Select a file' error when nothing has been uploaded" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      given()
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(List(supportingDocument("never-uploaded", InProgress)))

      val res: WSResponse = await(buildClient(continueUrl).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(s"$url?errorCode=InvalidArgument")
    }
  }
}
