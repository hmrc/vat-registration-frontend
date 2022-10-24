
package controllers.fileupload

import itutil.ControllerISpec
import models.api._
import models.external.upscan._
import play.api.http.HeaderNames
import play.api.test.Helpers._

import java.time.LocalDateTime

class UploadingDocumentControllerISpec extends ControllerISpec {

  val testReference = "test-reference"
  val showUrl: String = routes.UploadingDocumentController.show.url
  val pollUrl: String = routes.UploadingDocumentController.poll(testReference).url
  val submitUrl: String = routes.UploadingDocumentController.submit(testReference).url

  def testUpscanDetails(fileStatus: FileStatus): UpscanDetails = UpscanDetails(
    attachmentType = PrimaryIdentityEvidence,
    reference = testReference,
    fileStatus = fileStatus,
    uploadDetails = Some(UploadDetails("test-file", "image/gif", LocalDateTime.now(), "checksum", 100))
  )

  s"GET $showUrl" must {
    "return an Ok response when Upscan document reference present the session" in {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails(Ready), reference = testReference)

      val res = buildClient(path = showUrl, reference = Some(testReference)).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
    "return an Exception when Upscan document reference is not present the session" in {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(path = showUrl, reference = None).get()

      whenReady(res) { result =>
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  s"GET $pollUrl" must {
    "return an Ok response with status in the body" in {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails(Ready), reference = testReference)

      val res = buildClient(path = pollUrl, reference = Some(testReference)).get()

      whenReady(res) { result =>
        result.status mustBe OK
        result.body mustBe "{\"status\":\"READY\"}"
      }
    }

    "return an Ok response with status and failure reason, if any, in the body" in {
      val detailsWithFailureReason = testUpscanDetails(Ready).copy(failureDetails = Some(FailureDetails(FailureDetails.rejectedKey, "")))
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .upscanApi.fetchUpscanFileDetails(detailsWithFailureReason, reference = testReference)

      val res = buildClient(path = pollUrl, reference = Some(testReference)).get()

      whenReady(res) { result =>
        result.status mustBe OK
        result.body mustBe "{\"status\":\"READY\",\"reason\":\"REJECTED\"}"
      }
    }
  }

  s"POST $submitUrl" when {
    "redirect to the Summary page when upload file status is Ready" in {
      given()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails(Ready), reference = testReference)

      val res = buildClient(submitUrl).post("")

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentUploadSummaryController.show.url)
      }
    }
    "redirect to the same page when upload file status is InProgress" in {
      given()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails(InProgress), reference = testReference)

      val res = buildClient(submitUrl).post("")

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.UploadingDocumentController.show.url)
      }
    }
    "redirect to problem with file page when upload file status is Failed" in {
      given()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails(Failed), reference = testReference)

      val res = buildClient(submitUrl).post("")

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentUploadErrorController.show.url)
      }
    }
    "redirect to invalid file type page when upload file status is Failed and failure reason is Rejected" in {
      given()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(
        testUpscanDetails(Failed).copy(failureDetails = Some(FailureDetails(FailureDetails.rejectedKey, ""))),
        reference = testReference
      )

      val res = buildClient(submitUrl).post("")

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentUploadTypeErrorController.show.url)
      }
    }
  }
}
