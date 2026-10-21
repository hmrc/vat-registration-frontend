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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{deleteRequestedFor, urlPathEqualTo, verify}
import featuretoggle.FeatureSwitch.VrsNewAttachmentJourney
import itutil.ControllerISpec
import models.api.{AttachmentType, PrimaryIdentityEvidence, TaxRepresentativeAuthorisation, VAT2, VAT51, VAT5L}
import models.external.upscan.{Ready, UploadDetails, UpscanDetails}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDateTime

class RemoveDocumentControllerISpec extends ControllerISpec {

  val testReference = "test-reference"
  val url: String = routes.RemoveDocumentController.show(testReference).url
  val uploadSummaryUrl: String = routes.UploadSummaryController.show.url
  val oldJourneySummaryUrl: String = routes.DocumentUploadSummaryController.show.url
  val deleteUpscanDetailsPath = s"/vatreg/1/upscan-file-details/$testReference"

  def uploaded(attachmentType: AttachmentType): UpscanDetails = UpscanDetails(
    attachmentType = attachmentType,
    reference = testReference,
    fileStatus = Ready,
    uploadDetails = Some(UploadDetails("test-file.pdf", "application/pdf", LocalDateTime.now(), "checksum", 100))
  )

  override def afterEach(): Unit = {
    super.afterEach()
    disable(VrsNewAttachmentJourney)
  }

  private def givenDocument(upscanDetails: UpscanDetails): Unit =
    given()
      .audit.writesAudit()
      .audit.writesAuditMerged()
      .user.isAuthorised()
      .upscanApi.fetchUpscanFileDetails(upscanDetails, reference = testReference)
      .upscanApi.deleteUpscanDetails(reference = testReference)

  s"GET $url" must {
    Seq(VAT2 -> "VAT2", VAT51 -> "VAT50/51", TaxRepresentativeAuthorisation -> "VAT1TR", VAT5L -> "VAT5L").foreach { case (attachmentType, documentType) =>
      s"show the new Remove document page for $attachmentType when VrsNewAttachmentJourney is enabled" in new Setup {
        enable(VrsNewAttachmentJourney)
        givenDocument(uploaded(attachmentType))
        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.select("h1").text mustBe s"Remove $documentType document"
        doc.select("main table tbody td").first.text mustBe "test-file.pdf"
        doc.select("#remove-button").size mustBe 1
      }
    }

    "show the old yes/no page for a form document when VrsNewAttachmentJourney is disabled" in new Setup {
      disable(VrsNewAttachmentJourney)
      givenDocument(uploaded(VAT2))
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())
      val doc = Jsoup.parse(res.body)

      res.status mustBe OK
      doc.select("h1").text mustBe "Are you sure you want to remove test-file.pdf?"
      doc.select("input[type=radio][name=value]").size mustBe 2
      doc.select("#remove-button").size mustBe 0
    }

    "show the old yes/no page for a document that is not one of the four forms when VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)
      givenDocument(uploaded(PrimaryIdentityEvidence))
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())
      val doc = Jsoup.parse(res.body)

      res.status mustBe OK
      doc.select("h1").text mustBe "Are you sure you want to remove test-file.pdf?"
      doc.select("#remove-button").size mustBe 0
    }

    "redirect to Upload your documents when the document cannot be found and VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(uploadSummaryUrl)
    }
  }

  s"POST $url" must {
    Seq(VAT2, VAT51, TaxRepresentativeAuthorisation, VAT5L).foreach { attachmentType =>
      s"delete the $attachmentType upload and return to Upload your documents when the user chooses Remove" in new Setup {
        enable(VrsNewAttachmentJourney)
        givenDocument(uploaded(attachmentType))
        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).post(Map("choice" -> Seq("remove"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(uploadSummaryUrl)
        verify(WireMock.exactly(1), deleteRequestedFor(urlPathEqualTo(deleteUpscanDetailsPath)))
      }
    }

    "keep the upload and return to Upload your documents when the user chooses Go back" in new Setup {
      enable(VrsNewAttachmentJourney)
      givenDocument(uploaded(VAT2))
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("choice" -> Seq("cancel"))))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(uploadSummaryUrl)
      verify(WireMock.exactly(0), deleteRequestedFor(urlPathEqualTo(deleteUpscanDetailsPath)))
    }

    "keep the upload when the choice is missing or unrecognised" in new Setup {
      enable(VrsNewAttachmentJourney)
      givenDocument(uploaded(VAT2))
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val missing: WSResponse = await(buildClient(url).post(Map.empty[String, Seq[String]]))
      val unrecognised: WSResponse = await(buildClient(url).post(Map("choice" -> Seq("delete-everything"))))

      missing.header(HeaderNames.LOCATION) mustBe Some(uploadSummaryUrl)
      unrecognised.header(HeaderNames.LOCATION) mustBe Some(uploadSummaryUrl)
      verify(WireMock.exactly(0), deleteRequestedFor(urlPathEqualTo(deleteUpscanDetailsPath)))
    }

    "hand the old yes/no answer to the old journey when VrsNewAttachmentJourney is disabled" in new Setup {
      disable(VrsNewAttachmentJourney)
      givenDocument(uploaded(VAT2))
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> Seq("true"))))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(oldJourneySummaryUrl)
      verify(WireMock.exactly(1), deleteRequestedFor(urlPathEqualTo(deleteUpscanDetailsPath)))
    }

    "hand a document that is not one of the four forms to the old journey when VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)
      givenDocument(uploaded(PrimaryIdentityEvidence))
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("choice" -> Seq("remove"))))

      res.status mustBe BAD_REQUEST
      verify(WireMock.exactly(0), deleteRequestedFor(urlPathEqualTo(deleteUpscanDetailsPath)))
    }
  }
}
