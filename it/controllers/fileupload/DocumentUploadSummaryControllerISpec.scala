
package controllers.fileupload

import featureswitch.core.config.{FeatureSwitching, OptionToTax}
import itutil.ControllerISpec
import models.api._
import models.external.upscan.{Ready, UploadDetails, UpscanDetails}
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDateTime

class DocumentUploadSummaryControllerISpec extends ControllerISpec with FeatureSwitching {

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
          .registrationApi.getSection[Attachments](Some(Attachments(Some(Attached))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(pageUrl).get)

        res.status mustBe OK
      }

      "return server error if upload details missing" in new Setup {
        given
          .user.isAuthorised()
          .upscanApi.fetchAllUpscanDetails(testUpscanDetailsWithMissingUploadDetails)
          .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])
          .registrationApi.getSection[Attachments](Some(Attachments(Some(Attached))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

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
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Attached))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(continueUrl).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.UploadDocumentController.show.url)
    }

    "redirect to Supply 1614A page if 1614 info is required but incomplete" in new Setup {
      enable(OptionToTax)
      given
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(testUpscanDetails :+ attachmentDetails(VAT5L))
        .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Attached))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(continueUrl).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.Supply1614AController.show.url)
      disable(OptionToTax)
    }

    "redirect to Supply Supporting Documents page if they are required but none are present" in new Setup {
      enable(OptionToTax)
      given
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(testUpscanDetails :+ attachmentDetails(VAT5L) :+ attachmentDetails(Attachment1614a))
        .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Attached), Some(true), None, Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(continueUrl).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.SupplySupportingDocumentsController.show.url)
      disable(OptionToTax)
    }

    "redirect to Task List page if all option to tax conditions are met" in new Setup {
      enable(OptionToTax)
      given
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(testUpscanDetails :+ attachmentDetails(VAT5L) :+ attachmentDetails(LandPropertyOtherDocs))
        .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])
        .registrationApi.getSection[Attachments](Some(Attachments(Some(Attached), Some(false), Some(false), Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(continueUrl).post(""))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      disable(OptionToTax)
    }
  }

  s"POST $pageUrl" must {
    "redirect to Upload Supporting Document page if Yes is selected" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionId)
      given
        .user.isAuthorised()

      val res: WSResponse = await(buildClient(pageUrl).post(Map("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.UploadSupportingDocumentController.show.url)
    }

    "redirect to Task list page if No is selected" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionId)
      given
        .user.isAuthorised()

      val res: WSResponse = await(buildClient(pageUrl).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }

    "return a 400 if nothing is selected" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionId)
      given
        .user.isAuthorised()
        .upscanApi.fetchAllUpscanDetails(testUpscanDetails)
        .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])

      val res: WSResponse = await(buildClient(pageUrl).post(Map[String, String]()))

      res.status mustBe BAD_REQUEST
    }
  }
}
