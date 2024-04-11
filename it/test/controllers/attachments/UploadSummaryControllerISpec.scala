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

package controllers.attachments

import itutil.ControllerISpec
import models.api.{AttachmentType, Attachments, IdentityEvidence, TaxRepresentativeAuthorisation, TransactorIdentityEvidence, Upload, VAT2, VAT51, VAT5L}
import models.external.upscan.{Failed, InProgress, Ready, UpscanDetails}
import org.jsoup.Jsoup
import play.api.test.Helpers._

import scala.collection.JavaConverters._

class UploadSummaryControllerISpec extends ControllerISpec {

  val fullAttachmentList = Attachments(Some(Upload))

  val url = "/upload-summary"
  val fullAttachmentsList: List[AttachmentType] = List(TransactorIdentityEvidence, IdentityEvidence, VAT2, VAT51, TaxRepresentativeAuthorisation, VAT5L)
  val upscanDetailsList: List[UpscanDetails] = List(
    UpscanDetails(attachmentType = VAT51, reference = "ref1", fileStatus = InProgress),
    UpscanDetails(attachmentType = VAT2, reference = "ref2", fileStatus = Failed),
    UpscanDetails(attachmentType = VAT5L, reference = "ref3", fileStatus = Ready))

  "GET /register-for-vat/upload-summary" when {

    "no attachments are required" must {
      "return OK and render empty attachments list" in new Setup {
        given()
          .user.isAuthorised()
          .attachmentsApi.getAttachments(List())
          .upscanApi.fetchAllUpscanDetails(List())
          .registrationApi.getRegistration(fullVatScheme)


        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get())
        res.status mustBe OK

        val elements = Jsoup.parse(res.body).select("a[href*=register-for-vat/file-upload/]").asScala
        elements.isEmpty mustBe true
      }
    }

    "attachments are required and there is no upscan in progress" must {
      "return OK and render a list of attachments with correct links" in new Setup {
        given()
          .user.isAuthorised()
          .attachmentsApi.getAttachments(fullAttachmentsList)
          .upscanApi.fetchAllUpscanDetails(List[UpscanDetails]())
          .registrationApi.getRegistration(fullVatScheme)


        val res = await(buildClient(url).get())
        res.status mustBe OK

        val elements = Jsoup.parse(res.body).select("a[href*=register-for-vat/file-upload/upload-document]").asScala
        elements.map(_.childNode(0).toString.replaceAll(" ", "")).toList mustBe fullAttachmentsList.map(_.toString)
      }
    }

    "attachments are required and there is a mixture of upscan statuses" must {
      "return OK and render attachments list with edit links" in new Setup {
        given()
          .user.isAuthorised()
          .attachmentsApi.getAttachments(fullAttachmentsList)
          .upscanApi.fetchAllUpscanDetails(upscanDetailsList)
          .registrationApi.getRegistration(fullVatScheme)

        val res = await(buildClient(url).get())
        res.status mustBe OK

        val editLinkFailed = Jsoup.parse(res.body).select("a[href*=register-for-vat/attachment-error]").asScala
        val editLinkSuccess = Jsoup.parse(res.body).select("a[href*=register-for-vat/attachment-details]").asScala
        val allLinks = Jsoup.parse(res.body).select("a[href*=register-for-vat/file-upload/upload-document]").asScala

        editLinkFailed.map(_.childNode(0).toString).toList mustBe List("Edit")
        editLinkSuccess.map(_.childNode(0).toString).toList mustBe List("Edit")

        allLinks.map(_.childNode(0).toString.replaceAll(" ", "")).toList mustBe Seq(TransactorIdentityEvidence, IdentityEvidence, VAT51, TaxRepresentativeAuthorisation).map(_.toString)
      }
    }

  }

}
