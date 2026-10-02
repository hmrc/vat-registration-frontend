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

import config.FrontendAppConfig
import featuretoggle.FeatureSwitch.VrsNewAttachmentJourney
import itutil.ControllerISpec
import models.api.{AttachmentType, EligibilitySubmissionData, PrimaryIdentityEvidence, VAT2}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class UploadDocumentControllerISpec extends ControllerISpec {

  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val url: String = controllers.fileupload.routes.UploadDocumentController.show.url

  val testReference = "testReference"

  override def afterEach(): Unit = {
    super.afterEach()
    disable(VrsNewAttachmentJourney)
  }

  s"GET $url" must {
    "return an OK when there's an incomplete attachment" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      verifyDocumentUploadPage(url)
    }

    "return an OK when there's an incomplete attachment and has an errorCode" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      verifyDocumentUploadPage(s"$url?errorCode=EntityTooLarge")
    }

    "return the old journey page without the VAT2 form link when VrsNewAttachmentJourney is disabled" in new Setup {
      disable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = verifyDocumentUploadPage(url, VAT2)
      val doc = Jsoup.parse(res.body)

      doc.select("h1").text mustBe "Upload a VAT2"
      doc.select(".govuk-drop-zone").size mustBe 0
      doc.select("#file-upload-button").text mustBe "Continue"
    }

    "return the new journey page with the VAT2 form link when VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = verifyDocumentUploadPage(url, VAT2)
      val doc = Jsoup.parse(res.body)

      doc.select("h1").text mustBe "Upload your VAT2 form"
      doc.getElementsByAttributeValue("href", appConfig.vat2Link).size mustBe 1
      doc.select(".govuk-drop-zone").size mustBe 1
      doc.select("#file-upload-button").text mustBe "Upload"
    }

    "redirect to task list page when all attachments are complete" in new Setup {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .attachmentsApi.getIncompleteAttachments(List[AttachmentType]())

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
  }

  private def verifyDocumentUploadPage(url: String, attachmentType: AttachmentType = PrimaryIdentityEvidence): WSResponse = {
    given()
      .user.isAuthorised()
      .audit.writesAudit()
      .audit.writesAuditMerged()
      .attachmentsApi.getIncompleteAttachments(List(attachmentType))
      .upscanApi.upscanInitiate(testReference)
      .upscanApi.storeUpscanReference(testReference, attachmentType)
      .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))


    val response: Future[WSResponse] = buildClient(url).get()

    whenReady(response) { res =>
      res.status mustBe OK
      res
    }
  }
}
