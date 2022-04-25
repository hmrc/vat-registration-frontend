
package controllers.fileupload

import itutil.ControllerISpec
import models.api.AttachmentType
import models.external.upscan.{Ready, UploadDetails, UpscanDetails}
import play.api.http.Status.OK
import play.api.test.Helpers._

import java.time.LocalDateTime

class DocumentUploadSummaryControllerISpec  extends ControllerISpec {

  val pageUrl: String = routes.DocumentUploadSummaryController.show.url

  val testUpscanDetails = List(
    UpscanDetails(
      reference = "tes-reference",
      fileStatus = Ready,
      uploadDetails = Some(UploadDetails("test-file", "image/gif", LocalDateTime.now(), "checksum", 100))
    )
  )
  val testUpscanDetailsWithMissingUploadDetails = List(
    UpscanDetails(reference = "tes-reference", fileStatus = Ready)
  )

  s"GET $pageUrl" when {
    "the user has 1 or more documents uploaded" must {
      "return OK with the view" in new Setup {
        given
          .user.isAuthorised()
          .upscanApi.fetchAllUpscanDetails(testUpscanDetails)
          .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(pageUrl).get)
        res.status mustBe OK
      }

      "return server error if upload details missing" in new Setup {
        given
          .user.isAuthorised()
          .upscanApi.fetchAllUpscanDetails(testUpscanDetailsWithMissingUploadDetails)
          .attachmentsApi.getIncompleteAttachments(List.empty[AttachmentType])
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(pageUrl).get)
        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
