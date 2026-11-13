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

class DocumentsPostControllerISpec extends ControllerISpec {

  val showUrl: String =
    controllers.attachments.routes.DocumentsPostController.show.url

  val vat2Url: String =
    controllers.attachments.routes.Vat2DocumentsPostController.show.url

  val vat5LUrl: String =
    controllers.attachments.routes.Vat5LDocumentsPostController.show.url

  val testVat2Attachments: List[AttachmentType] =
    List(VAT2)

  val testVat5LAttachments: List[AttachmentType] =
    List(VAT5L)

  val testBothAttachments: List[AttachmentType] =
    List(VAT2, VAT5L)

  val testOtherAttachments: List[AttachmentType] =
    List(IdentityEvidence, TaxRepresentativeAuthorisation, OtherAttachments)

  val testEmptyAttachments: List[AttachmentType] =
    List.empty

  s"GET $showUrl" must {

    "redirect to VAT2 documents page when VAT2 is present" in {
      given()
        .user.isAuthorised()
        .attachmentsApi.getAttachments(testVat2Attachments)

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(vat2Url)
      }
    }

    "redirect to VAT5L documents page when VAT5L is present" in {
      given()
        .user.isAuthorised()
        .attachmentsApi.getAttachments(testVat5LAttachments)

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(vat5LUrl)
      }
    }

    "redirect to VAT2 when both VAT2 and VAT5L are present" in {
      given()
        .user.isAuthorised()
        .attachmentsApi.getAttachments(testBothAttachments)

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(vat2Url)
      }
    }

    "display the standard documents post page when neither VAT2 nor VAT5L is present" in {
      given()
        .user.isAuthorised()
        .attachmentsApi.getAttachments(testOtherAttachments)

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe OK
        result.body must include("Posting your documents and forms")
      }
    }

    "display the standard documents post page when the attachment list is empty" in {
      given()
        .user.isAuthorised()
        .attachmentsApi.getAttachments(testEmptyAttachments)

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe OK
        result.body must include("Posting your documents and forms")
      }
    }
  }
}
