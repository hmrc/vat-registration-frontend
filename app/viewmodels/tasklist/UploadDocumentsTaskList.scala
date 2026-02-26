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

import models.api.AttachmentType
import models.external.upscan._

import javax.inject.Singleton

@Singleton
class UploadDocumentsTaskList {

  def attachmentRow(attachmentType: AttachmentType, uploadDetails: Seq[UpscanDetails]): TaskListRowBuilder = {
      TaskListRowBuilder(
        messageKey = _ => "fileUpload.summary." + attachmentType.typeName,
        url = _ => {
          case TLCompleted => controllers.fileupload.routes.ViewAttachmentController.show.url
          case TLFailed => controllers.fileupload.routes.UploadErrorController.show(attachmentType.toString).url
          case _ => controllers.fileupload.routes.UploadDocumentController.show.url
        },
        tagId = attachmentType.typeName + "Row",
        checks = _ => {
          val hasUpscan = uploadDetails.map(_.attachmentType).contains(attachmentType)
          Seq(hasUpscan, hasUpscan && uploadDetails.filter(_.attachmentType.equals(attachmentType)).head.fileStatus.equals(Ready))
        },
        prerequisites = _ => Seq(),
        error = _ => {
          val details = uploadDetails.filter(_.attachmentType.equals(attachmentType))
          details.nonEmpty && details.head.fileStatus.equals(Failed)
        },
        canEdit = state => state.equals(TLCompleted) || state.equals(TLFailed)
      )
  }

}
