
package controllers.fileupload

import itutil.ControllerISpec
import models.api.PrimaryIdentityEvidence
import models.external.upscan.{Ready, UploadDetails, UpscanDetails}
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDateTime
import scala.concurrent.Future

class RemoveUploadedDocumentControllerISpec extends ControllerISpec {

  val testReference = "test-reference"

  def removeDocumentUrl(reference: String): String = routes.RemoveUploadedDocumentController.submit(reference).url

  val testUpscanDetails: UpscanDetails = UpscanDetails(
    attachmentType = PrimaryIdentityEvidence,
    reference = testReference,
    fileStatus = Ready,
    uploadDetails = Some(UploadDetails("test-file", "image/gif", LocalDateTime.now(), "checksum", 100))
  )

  s"GET ${removeDocumentUrl(testReference)}" must {
    "return OK" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails, reference = testReference)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(removeDocumentUrl(testReference)).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "return an error when document name is missing" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails.copy(uploadDetails = None), reference = testReference)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(removeDocumentUrl(testReference)).get()

      whenReady(response) { res =>
        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  s"POST ${removeDocumentUrl(testReference)}" must {
    "return a redirect to summary page when user chooses 'Yes' and has been successfully removed" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .upscanApi.deleteUpscanDetails()
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails, reference = testReference)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(removeDocumentUrl(testReference)).post(Map("value" -> Seq("true")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentUploadSummaryController.show.url)
      }
    }
  }

  "return a redirect to summary page when user chooses 'No'" in new Setup {
    given()
      .audit.writesAudit()
      .audit.writesAuditMerged()
      .user.isAuthorised()
      .upscanApi.fetchUpscanFileDetails(testUpscanDetails, reference = testReference)

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response: Future[WSResponse] = buildClient(removeDocumentUrl(testReference)).post(Map("value" -> Seq("false")))

    whenReady(response) { res =>
      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentUploadSummaryController.show.url)
    }
  }

  "return BAD_REQUEST if none of the option is selected" in new Setup {
    given()
      .audit.writesAudit()
      .audit.writesAuditMerged()
      .user.isAuthorised()
      .upscanApi.fetchUpscanFileDetails(testUpscanDetails, reference = testReference)

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response: Future[WSResponse] = buildClient(removeDocumentUrl(testReference)).post(Map("value" -> Seq("")))

    whenReady(response) { res =>
      res.status mustBe BAD_REQUEST
    }
  }
}
