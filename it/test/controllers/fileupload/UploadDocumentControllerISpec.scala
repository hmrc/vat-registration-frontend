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
import models.api._
import models.{ApplicantDetails, TransactorDetails}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class UploadDocumentControllerISpec extends ControllerISpec {

  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val url: String = controllers.fileupload.routes.UploadDocumentController.show.url
  val proofOfIdentityUrl: String = controllers.fileupload.routes.UploadDocumentController.showProofOfIdentity.url

  val testReference = "testReference"
  implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

  override def afterEach(): Unit = {
    super.afterEach()
    disable(VrsNewAttachmentJourney)
  }

  s"GET $url" must {
    "return an OK when there's an incomplete attachment" in new Setup {
      disable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      verifyDocumentUploadPage(url)
    }

    "return an OK when there's an incomplete attachment and has an errorCode" in new Setup {
      disable(VrsNewAttachmentJourney)
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

    "return the identity evidence page for the first item with the applicant name when VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = verifyIdentityEvidencePage(List(ExtraIdentityEvidence, ExtraIdentityEvidence)) {
        _.registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
      }
      val doc = Jsoup.parse(res.body)

      doc.select("h1").text mustBe "Upload testFirstName testLastName’s identity evidence"
      doc.select("p[aria-live=polite] strong").text mustBe "0 of 2 files uploaded"
      doc.select("main label.govuk-label").text mustBe "First item of evidence"
      doc.select(".govuk-drop-zone").size mustBe 1
      doc.select("#file-upload-button").text mustBe "Continue"
    }

    "return the identity evidence page for the second item, ignoring other outstanding attachments, when VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = verifyIdentityEvidencePage(List(ExtraIdentityEvidence, VAT2, VAT5L)) {
        _.registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
      }
      val doc = Jsoup.parse(res.body)

      doc.select("p[aria-live=polite] strong").text mustBe "1 of 2 files uploaded"
      doc.select("main label.govuk-label").text mustBe "Second item of evidence"
    }

    "return the identity evidence page with the transactor name when VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = verifyIdentityEvidencePage(List(ExtraTransactorIdentityEvidence, ExtraTransactorIdentityEvidence, PrimaryIdentityEvidence)) {
        _.registrationApi.getSection[TransactorDetails](Some(validTransactorDetails.copy(personalDetails = validTransactorDetails.personalDetails.map(_.copy(firstName = "Tara", lastName = "Transactor")))))
      }
      val doc = Jsoup.parse(res.body)

      doc.select("h1").text mustBe "Upload Tara Transactor’s identity evidence"
      doc.select("p[aria-live=polite] strong").text mustBe "0 of 2 files uploaded"
    }

    "return the identity evidence page with the generic heading when the applicant has no details and VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = verifyIdentityEvidencePage(List(ExtraIdentityEvidence, ExtraIdentityEvidence)) {
        _.registrationApi.getSection[ApplicantDetails](None)
      }
      val doc = Jsoup.parse(res.body)

      doc.select("h1").text mustBe "Upload a document"
      doc.select("main label.govuk-label").text mustBe "First item of evidence"
    }

    "show the upscan error on the identity evidence page when VrsNewAttachmentJourney is enabled and there is an errorCode" in new Setup {
      enable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = verifyIdentityEvidencePage(List(ExtraIdentityEvidence, ExtraIdentityEvidence), s"$url?errorCode=EntityTooSmall") {
        _.registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
      }
      val doc = Jsoup.parse(res.body)

      doc.select(".govuk-error-summary a").text mustBe "The selected file is empty"
      doc.select(".govuk-error-summary a").attr("href") mustBe "#file-upload-1"
      doc.select(".govuk-error-message").text mustBe "Error: The selected file is empty"
    }

    "return the old journey page for identity evidence when VrsNewAttachmentJourney is disabled" in new Setup {
      disable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = verifyIdentityEvidencePage(List(ExtraIdentityEvidence, ExtraIdentityEvidence))(identity)
      val doc = Jsoup.parse(res.body)

      doc.select("h1").text mustBe "Upload a document"
      doc.select("main .govuk-inset-text").text must include("This could be a:")
      doc.select(".govuk-drop-zone").size mustBe 0
      doc.select("p[aria-live=polite]").size mustBe 0
    }

    "return the proof of identity page with the applicant name when VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = verifyIdentityEvidencePage(List(PrimaryIdentityEvidence, ExtraIdentityEvidence, ExtraIdentityEvidence), proofOfIdentityUrl) {
        _.registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
      }
      val doc = Jsoup.parse(res.body)

      doc.title mustBe "Upload proof of identity - Register for VAT - GOV.UK"
      doc.select("h1").text mustBe "Upload testFirstName testLastName’s proof of identity"
      doc.select("main form > p").text mustBe "This must be a copy of either testFirstName testLastName’s passport, driving licence photocard or national identity card."
      doc.select(".govuk-drop-zone").size mustBe 1
      doc.select("p[aria-live=polite]").size mustBe 0
      doc.select("#file-upload-button").text mustBe "Upload"
    }

    "return the proof of identity page with the transactor name when VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = verifyIdentityEvidencePage(List(PrimaryTransactorIdentityEvidence, PrimaryIdentityEvidence), proofOfIdentityUrl) {
        _.registrationApi.getSection[TransactorDetails](Some(validTransactorDetails.copy(personalDetails = validTransactorDetails.personalDetails.map(_.copy(firstName = "Tara", lastName = "Transactor")))))
      }
      val doc = Jsoup.parse(res.body)

      doc.select("h1").text mustBe "Upload Tara Transactor’s proof of identity"
      doc.select("main form > p").text mustBe "This must be a copy of either Tara Transactor’s passport, driving licence photocard or national identity card."
    }

    "return the proof of identity page with the nameless heading when the applicant has no details and VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = verifyIdentityEvidencePage(List(PrimaryIdentityEvidence), proofOfIdentityUrl)(_.registrationApi.getSection[ApplicantDetails](None))
      val doc = Jsoup.parse(res.body)

      doc.select("h1").text mustBe "Upload proof of identity"
      doc.select("main form > p").size mustBe 0
      doc.select(".govuk-drop-zone").size mustBe 1
    }

    "show the upscan error on the proof of identity page when VrsNewAttachmentJourney is enabled and there is an errorCode" in new Setup {
      enable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = verifyIdentityEvidencePage(List(PrimaryIdentityEvidence), s"$proofOfIdentityUrl?errorCode=EntityTooSmall") {
        _.registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
      }
      val doc = Jsoup.parse(res.body)

      doc.select(".govuk-error-summary a").text mustBe "The selected file is empty"
      doc.select(".govuk-error-summary a").attr("href") mustBe "#file-upload-1"
      doc.select(".govuk-error-message").text mustBe "Error: The selected file is empty"
    }

    "return the old journey page for primary identity evidence when VrsNewAttachmentJourney is disabled" in new Setup {
      disable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = verifyDocumentUploadPage(url, PrimaryIdentityEvidence)
      val doc = Jsoup.parse(res.body)

      doc.select("h1").text mustBe "Upload a document"
      doc.select("main .govuk-inset-text").text must include("This must be a passport, photocard drivers licence or a national identity card.")
      doc.select(".govuk-drop-zone").size mustBe 0
      doc.select("#file-upload-button").text mustBe "Continue"
    }

    "redirect to the proof of identity page without initiating upscan when proof of identity is next and VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = getWithIncomplete(url, List(PrimaryIdentityEvidence, ExtraIdentityEvidence, ExtraIdentityEvidence))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some("/register-for-vat/file-upload/proof-identity")
    }

    "carry the upscan errorCode, and nothing else, across the redirect to the proof of identity page" in new Setup {
      enable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = getWithIncomplete(s"$url?errorCode=EntityTooLarge&errorMessage=ignored", List(PrimaryTransactorIdentityEvidence))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some("/register-for-vat/file-upload/proof-identity?errorCode=EntityTooLarge")
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

  s"GET $proofOfIdentityUrl" must {
    "redirect to the upload document page when a different attachment is next" in new Setup {
      enable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = getWithIncomplete(proofOfIdentityUrl, List(ExtraIdentityEvidence, PrimaryTransactorIdentityEvidence))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(url)
    }

    "redirect to the upload document page when VrsNewAttachmentJourney is disabled" in new Setup {
      disable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = getWithIncomplete(proofOfIdentityUrl, List(PrimaryIdentityEvidence))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(url)
    }

    "redirect to the upload document page when all attachments are complete" in new Setup {
      enable(VrsNewAttachmentJourney)
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = getWithIncomplete(proofOfIdentityUrl, Nil)

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(url)
    }
  }

  // Stubs neither upscan nor the registration API, so a response other than a redirect means one of them was called
  private def getWithIncomplete(requestUrl: String, incomplete: List[AttachmentType]): WSResponse = {
    given()
      .user.isAuthorised()
      .audit.writesAudit()
      .audit.writesAuditMerged()
      .attachmentsApi.getIncompleteAttachments(incomplete)

    await(buildClient(requestUrl).get())
  }

  private def verifyIdentityEvidencePage(incomplete: List[AttachmentType], requestUrl: String = url)
                                        (addStubs: PreconditionBuilder => PreconditionBuilder): WSResponse = {
    val stubs = given()
      .user.isAuthorised()
      .audit.writesAudit()
      .audit.writesAuditMerged()
      .attachmentsApi.getIncompleteAttachments(incomplete)
      .upscanApi.upscanInitiate(testReference)
      .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

    addStubs(incomplete.headOption.fold(stubs)(stubs.upscanApi.storeUpscanReference(testReference, _)))

    val response: Future[WSResponse] = buildClient(requestUrl).get()

    whenReady(response) { res =>
      res.status mustBe OK
      res
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
