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
import models.api._
import play.api.http.HeaderNames
import play.api.test.Helpers._

class DocumentsRequiredControllerISpec extends ControllerISpec {

  val resolveUrl: String = routes.DocumentsRequiredController.resolve.url
  val submitUrl: String = routes.DocumentsRequiredController.submit.url

  s"GET $resolveUrl" must {

    "return a redirect to documents required page when identity evidence is required" in {
      given()
        .user.isAuthorised()
        .attachmentsApi.getAttachments(List[AttachmentType](IdentityEvidence))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.IdentityEvidenceRequiredController.show.url)
      }
    }

    "return a redirect to VAT2 required page when VAT2 is required" in {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .attachmentsApi.getAttachments(List[AttachmentType](VAT2))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.Vat2RequiredController.show.url)
      }
    }

    "return a redirect to VAT51 required page when VAT51 is required" in {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .attachmentsApi.getAttachments(List[AttachmentType](VAT51))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.Vat51RequiredController.show.url)
      }
    }

    "return a redirect to VAT5L required page when VAT5L is required" in {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .attachmentsApi.getAttachments(List[AttachmentType](VAT5L))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.Vat5LRequiredController.show.url)
      }
    }

    "return a redirect to VAT1TR required page when VAT1TR is required" in {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .attachmentsApi.getAttachments(List[AttachmentType](TaxRepresentativeAuthorisation))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.Vat1TRRequiredController.show.url)
      }
    }

    "return a redirect to multiple documents required page when multiple attachments are required" in {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .attachmentsApi.getAttachments(List[AttachmentType](VAT5L, VAT2))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.MultipleDocumentsRequiredController.show.url)
      }
    }

    "return a redirect to Transactor Identity Evidence Required" when {
      "the user is a Transactor and transactor details are unverified" in {
        given()
          .user.isAuthorised()
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .attachmentsApi.getAttachments(List[AttachmentType](TransactorIdentityEvidence))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

        val res = buildClient(resolveUrl).get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.TransactorIdentityEvidenceRequiredController.show.url)
        }
      }

      "the user is a Transactor and applicant details are unverified" in {
        given()
          .user.isAuthorised()
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .attachmentsApi.getAttachments(List[AttachmentType](IdentityEvidence))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

        val res = buildClient(resolveUrl).get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.TransactorIdentityEvidenceRequiredController.show.url)
        }
      }

      "the user is a Transactor and transactor with applicant details are unverified" in {
        given()
          .user.isAuthorised()
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .attachmentsApi.getAttachments(List[AttachmentType](TransactorIdentityEvidence, IdentityEvidence))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

        val res = buildClient(resolveUrl).get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.TransactorIdentityEvidenceRequiredController.show.url)
        }
      }
    }

    "return a redirect to task list page when no attachments are given" in {
      given()
        .user.isAuthorised()
        .attachmentsApi.getAttachments(List[AttachmentType]())
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(resolveUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
  }

  s"POST $submitUrl" when {
    "redirect to the AttachmentMethod page" in {
      given()
        .user.isAuthorised()

      val res = buildClient(submitUrl).post("")

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.AttachmentMethodController.show.url)
      }
    }
  }
}
