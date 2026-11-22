/*
 * Copyright 2026 HM Revenue & Customs
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

  private def stubDependencies(
                                attachments: List[AttachmentType],
                                isTransactor: Boolean = false
                              ): Unit =
    given()
      .user.isAuthorised()
      .registrationApi.getSection[EligibilitySubmissionData](
        Some(testEligibilitySubmissionData.copy(isTransactor = isTransactor))
      )
      .attachmentsApi.getAttachments(attachments)

  private def assertRedirect(expectedUrl: String): Unit =
    whenReady(buildClient(resolveUrl).get()) { response =>
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
    }

  s"GET $resolveUrl" must {

    "redirect to TaskList when there are no attachments" in {
      stubDependencies(Nil)

      assertRedirect(controllers.routes.TaskListController.show.url)
    }

    "redirect to TransactorIdentityEvidenceRequired when the user is a transactor with IdentityEvidence" in {
      stubDependencies(
        List(IdentityEvidence),
        isTransactor = true
      )

      assertRedirect(routes.TransactorIdentityEvidenceRequiredController.show.url)
    }

    "redirect to TransactorIdentityEvidenceRequired when the user is a transactor with TransactorIdentityEvidence" in {
      stubDependencies(
        List(TransactorIdentityEvidence),
        isTransactor = true
      )

      assertRedirect(routes.TransactorIdentityEvidenceRequiredController.show.url)
    }

    "redirect to TransactorIdentityEvidenceRequired when the user is a transactor with both identity documents" in {
      stubDependencies(
        List(IdentityEvidence, TransactorIdentityEvidence),
        isTransactor = true
      )

      assertRedirect(routes.TransactorIdentityEvidenceRequiredController.show.url)
    }

    "redirect to MultipleDocumentsRequired when the user is not a transactor with IdentityEvidence" in {
      stubDependencies(List(IdentityEvidence))

      assertRedirect(routes.MultipleDocumentsRequiredController.show.url)
    }

    "redirect to Vat2DocumentsRequired when VAT2 is the only required attachment" in {
      stubDependencies(List(VAT2))

      assertRedirect(routes.Vat2DocumentsRequiredController.show.url)
    }

    "redirect to Vat5LDocumentsRequired when VAT5L is the only required attachment" in {
      stubDependencies(List(VAT5L))

      assertRedirect(routes.Vat5LDocumentsRequiredController.show.url)
    }

    "redirect to MultipleDocumentsRequired when VAT2 and VAT5L are required" in {
      stubDependencies(List(VAT2, VAT5L))

      assertRedirect(routes.MultipleDocumentsRequiredController.show.url)
    }

    "redirect to MultipleDocumentsRequired when VAT51 is required" in {
      stubDependencies(List(VAT51))

      assertRedirect(routes.MultipleDocumentsRequiredController.show.url)
    }

    "redirect to MultipleDocumentsRequired when TaxRepresentativeAuthorisation is required" in {
      stubDependencies(List(TaxRepresentativeAuthorisation))

      assertRedirect(routes.MultipleDocumentsRequiredController.show.url)
    }
  }

  s"POST $submitUrl" must {

    "redirect to AttachmentMethod" in {
      given()
        .user.isAuthorised()

      whenReady(buildClient(submitUrl).post("")) { response =>
        response.status mustBe SEE_OTHER
        response.header(HeaderNames.LOCATION) mustBe Some(
          routes.AttachmentMethodController.show.url
        )
      }
    }
  }
}
