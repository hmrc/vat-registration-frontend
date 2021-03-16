/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class UpscanDetails(reference: String,
                         downloadUrl: Option[String] = None,
                         fileStatus: String,
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