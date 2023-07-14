/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors

import config.FrontendAppConfig
import models.api.AttachmentType
import play.api.http.Status._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, InternalServerException, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import utils.LoggingUtil
import play.api.mvc.Request

@Singleton
class AttachmentsConnector @Inject()(httpClient: HttpClientV2, config: FrontendAppConfig)(implicit ec: ExecutionContext) extends LoggingUtil {

  def getAttachmentList(regId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[List[AttachmentType]] = {

    implicit val readRaw: HttpReads[HttpResponse] = HttpReads.Implicits.readRaw
    httpClient.get(url"${config.attachmentsApiUrl(regId)}")
      .execute
      .map { result =>
        result.status match {
          case OK => result.json.as[List[AttachmentType]]
          case status => 
            errorLog(s"[AttachmentsConnector][getAttachmentList] unexpected status from backend: $status")
            throw new InternalServerException(s"[AttachmentsConnector][getAttachmentList] unexpected status from backend: $status")
        }
      }
  }

  def getIncompleteAttachments(regId: String)(implicit hc: HeaderCarrier, reuqest: Request[_]): Future[List[AttachmentType]] = {
    implicit val readRaw: HttpReads[HttpResponse] = HttpReads.Implicits.readRaw

    httpClient.get(url"${config.incompleteAttachmentsApiUrl(regId)}")
      .execute
      .map { result =>
        result.status match {
          case OK => result.json.as[List[AttachmentType]]
          case status => 
            errorLog(s"[AttachmentsConnector][getAttachmentList] unexpected status from backend: $status")
            throw new InternalServerException(s"[AttachmentsConnector][getIncompleteAttachments] unexpected status from backend: $status")
        }
      }
  }
}