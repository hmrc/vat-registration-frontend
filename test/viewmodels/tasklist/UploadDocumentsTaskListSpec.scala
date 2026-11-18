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

package viewmodels.tasklist

import fixtures.VatRegistrationFixture
import models.api.{AttachmentType, VAT2, VAT5L}
import models.external.upscan.{FileStatus, Failed, Ready, UpscanDetails}
import testHelpers.VatRegSpec

class UploadDocumentsTaskListSpec extends VatRegSpec with VatRegistrationFixture {

  val taskList = new UploadDocumentsTaskList

  def upload(attachmentType: AttachmentType, fileStatus: FileStatus): UpscanDetails =
    UpscanDetails(attachmentType = attachmentType, reference = "reference", fileStatus = fileStatus)

  def row(attachmentType: AttachmentType, uploads: UpscanDetails*): TaskListSectionRow =
    taskList.attachmentRow(attachmentType, uploads).build(emptyVatScheme)

  "attachmentRow" when {
    "the VAT5L has been uploaded" must {
      "offer an edit link to the VAT5L recap page" in {
        val vat5LRow = row(VAT5L, upload(VAT5L, Ready))

        vat5LRow.status mustBe TLCompleted
        vat5LRow.canEdit mustBe true
        vat5LRow.url mustBe controllers.fileupload.routes.UploadedVat5LDocumentsController.show.url
      }
    }

    "the VAT5L upload has failed" must {
      "link to the upload error page" in {
        val vat5LRow = row(VAT5L, upload(VAT5L, Failed))

        vat5LRow.status mustBe TLFailed
        vat5LRow.url mustBe controllers.fileupload.routes.UploadErrorController.show(VAT5L.toString).url
      }
    }

    "another document has been uploaded" must {
      "offer an edit link to the attachment details page" in {
        val vat2Row = row(VAT2, upload(VAT2, Ready))

        vat2Row.status mustBe TLCompleted
        vat2Row.url mustBe controllers.fileupload.routes.ViewAttachmentController.show.url
      }
    }
  }
}
