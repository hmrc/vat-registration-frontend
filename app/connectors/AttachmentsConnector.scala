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

package connectors

import config.FrontendAppConfig
import models.api.{AttachmentMethod, AttachmentType, Attachments}
import play.api.http.Status._
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse, InternalServerException}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AttachmentsConnector @Inject()(httpClient: HttpClient, config: FrontendAppConfig)(implicit ec: ExecutionContext) {

  def getAttachmentList(regId: String)(implicit hc: HeaderCarrier): Future[Attachments] = {
    implicit val readRaw: HttpReads[HttpResponse] = HttpReads.Implicits.readRaw

    httpClient.GET[HttpResponse](config.attachmentsApiUrl(regId)).map { result =>
      result.status match {
        case OK => result.json.validate[Attachments].get
        case status => throw new InternalServerException(s"[AttachmentsConnector][getAttachmentList] unexpected status from backend: $status")
      }
    }
  }

  def storeAttachmentDetails(regId: String, attachmentMethod: AttachmentMethod)(implicit hc: HeaderCarrier): Future[JsValue] =
    httpClient.PUT[JsValue, JsValue] (
      url = config.attachmentsApiUrl(regId),
      body = Json.obj("method" -> Json.toJson(attachmentMethod))
    )

}
