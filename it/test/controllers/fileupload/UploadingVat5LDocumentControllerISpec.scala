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

import com.github.tomakehurst.wiremock.client.WireMock.{deleteRequestedFor, urlPathEqualTo, verify}
import itutil.ControllerISpec
import models.api._
import models.external.upscan._
import play.api.http.HeaderNames
import play.api.test.Helpers._

import java.time.LocalDateTime

class UploadingVat5LDocumentControllerISpec extends ControllerISpec {

  val testReference = "test-reference"
  val showUrl: String = routes.UploadingVat5LDocumentController.show.url
  val pollUrl: String = routes.UploadingVat5LDocumentController.poll(testReference).url
  val submitUrl: String = routes.UploadingVat5LDocumentController.submit(testReference).url

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
    "redirect to the Supporting Documents page when a VAT5L upload is Ready" in {
      given()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails(Ready).copy(attachmentType = VAT5L), reference = testReference)

      val res = buildClient(submitUrl).post("")

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SupportingDocumentsController.show.url)
      }
    }
    "redirect to the supporting documents upload page when a LandPropertyOtherDocs upload is Ready" in {
      given()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails(Ready).copy(attachmentType = LandPropertyOtherDocs), reference = testReference)

      val res = buildClient(submitUrl).post("")

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SupportingDocumentUploadController.show.url)
      }
    }
    "redirect to the VAT1614H page when a Attachment1614a upload is Ready" in {
      given()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails(Ready).copy(attachmentType = Attachment1614a), reference = testReference)

      val res = buildClient(submitUrl).post("")

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.VAT1614HController.show.url)
      }
    }
    "redirect to the uploaded VAT5L documents page when a Attachment1614h upload is Ready" in {
      given()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails(Ready).copy(attachmentType = Attachment1614h), reference = testReference)

      val res = buildClient(submitUrl).post("")

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.UploadedVat5LDocumentsController.show.url)
      }
    }
    "redirect to the same page when upload file status is InProgress" in {
      given()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(testUpscanDetails(InProgress), reference = testReference)

      val res = buildClient(submitUrl).post("")

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.UploadingVat5LDocumentController.show.url)
      }
    }
    "remove a failed supporting document and return to the supporting documents upload page with the InvalidFileType error when the failure reason is Rejected" in {
      given()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(
          testUpscanDetails(Failed).copy(attachmentType = LandPropertyOtherDocs, failureDetails = Some(FailureDetails(FailureDetails.rejectedKey, ""))),
          reference = testReference
        )
        .upscanApi.deleteUpscanDetails(testRegId, testReference)

      val res = buildClient(submitUrl).post("")

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(s"${routes.SupportingDocumentUploadController.show.url}?errorCode=InvalidFileType")
        verify(deleteRequestedFor(urlPathEqualTo(s"/vatreg/$testRegId/upscan-file-details/$testReference")))
      }
    }
    "remove a failed supporting document and return to the supporting documents upload page with the Quarantine error when the failure reason is Quarantine" in {
      given()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(
          testUpscanDetails(Failed).copy(attachmentType = LandPropertyOtherDocs, failureDetails = Some(FailureDetails(FailureDetails.quarantineKey, ""))),
          reference = testReference
        )
        .upscanApi.deleteUpscanDetails(testRegId, testReference)

      val res = buildClient(submitUrl).post("")

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(s"${routes.SupportingDocumentUploadController.show.url}?errorCode=Quarantine")
        verify(deleteRequestedFor(urlPathEqualTo(s"/vatreg/$testRegId/upscan-file-details/$testReference")))
      }
    }
    "remove a failed supporting document and return to the supporting documents upload page with the Unknown error when the failure reason is Unknown" in {
      given()
        .user.isAuthorised()
        .upscanApi.fetchUpscanFileDetails(
          testUpscanDetails(Failed).copy(attachmentType = LandPropertyOtherDocs, failureDetails = Some(FailureDetails(FailureDetails.unknownKey, ""))),
          reference = testReference
        )
        .upscanApi.deleteUpscanDetails(testRegId, testReference)

      val res = buildClient(submitUrl).post("")

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(s"${routes.SupportingDocumentUploadController.show.url}?errorCode=Unknown")
        verify(deleteRequestedFor(urlPathEqualTo(s"/vatreg/$testRegId/upscan-file-details/$testReference")))
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
