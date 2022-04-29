/*
 * Copyright 2022 HM Revenue & Customs
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

package models.external.upscan

import models.api.AttachmentType
import play.api.libs.json._

import java.time.LocalDateTime

case class UpscanDetails(attachmentType: AttachmentType,
                         reference: String,
                         downloadUrl: Option[String] = None,
                         fileStatus: FileStatus,
                         uploadDetails: Option[UploadDetails] = None,
                         failureDetails: Option[FailureDetails] = None)

object UpscanDetails {
  implicit val format: OFormat[UpscanDetails] = Json.format[UpscanDetails]
}

case class UploadDetails(fileName: String,
                         fileMimeType: String,
                         uploadTimestamp: LocalDateTime,
                         checksum: String,
                         size: Int)

object UploadDetails {
  implicit val format: OFormat[UploadDetails] = Json.format[UploadDetails]
}

case class FailureDetails(failureReason: String,
                          message: String)

object FailureDetails {
  implicit val format: OFormat[FailureDetails] = Json.format[FailureDetails]
}

sealed trait FileStatus {
  val value: String
}

case object InProgress extends FileStatus {
  val value = "IN_PROGRESS"
}

case object Failed extends FileStatus {
  val value = "FAILED"
}

case object Ready extends FileStatus {
  val value = "READY"
}

object FileStatus {

  def unapply(status: FileStatus): String = status.value

  val reads: Reads[FileStatus] = for {
    value <- JsPath.read[String].map {
      case InProgress.value => InProgress
      case Failed.value => Failed
      case Ready.value => Ready
    }
  } yield value

  val writes: Writes[FileStatus] = Writes {
    status: FileStatus => JsString(status.value)
  }

  implicit val format: Format[FileStatus] = Format(
    reads,
    writes
  )

}