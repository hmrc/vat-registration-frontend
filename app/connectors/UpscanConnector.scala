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

package connectors

import config.FrontendAppConfig
import featuretoggle.FeatureSwitch.StubUpscan
import featuretoggle.FeatureToggleSupport
import models.api.AttachmentType
import models.external.upscan.{UpscanDetails, UpscanResponse}
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Request
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException, StringContextOps}
import utils.SessionIdRequestHelper

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpscanConnector @Inject()(httpClient: HttpClientV2, appConfig: FrontendAppConfig)
                               (implicit executionContext: ExecutionContext) extends FeatureToggleSupport {

  def upscanInitiate()(implicit hc: HeaderCarrier): Future[UpscanResponse] = {
    lazy val url = appConfig.setupUpscanJourneyUrl
    lazy val body = Json.obj(
      "callbackUrl" -> appConfig.storeUpscanCallbackUrl,
      "successRedirect" -> s"${appConfig.hostAbsoluteUrl}${controllers.fileupload.routes.UploadingDocumentController.show.url}",
      "errorRedirect" -> s"${appConfig.hostAbsoluteUrl}${controllers.fileupload.routes.UploadDocumentController.show.url}",
      "minimumFileSize" -> 1,
      "maximumFileSize" -> 10485760
    )

    val upscanRequest = SessionIdRequestHelper.conditionallyAddSessionIdHeader(
      baseRequest = httpClient.post(url"$url").withBody(body),
      condition = isEnabled(StubUpscan)(appConfig)
    )

    upscanRequest.execute.map {
      case response@HttpResponse(OK, _, _) => response.json.as[UpscanResponse]
      case response => throw new InternalServerException(s"[UpscanConnector] Upscan initiate received an unexpected response Status: ${response.status}")
    }
  }

  def storeUpscanReference(regId: String, reference: String, attachmentType: AttachmentType)(implicit hc: HeaderCarrier, request: Request[_]): Future[HttpResponse] = {
    logger.info(s"[UpscanConnector][storeUpscanReference] attempting to store upscan reference: $reference for regId $regId")
    lazy val url = appConfig.storeUpscanReferenceUrl(regId)
    httpClient.post(url"$url")
      .withBody(
        Json.obj(
          "reference" -> reference,
          "attachmentType" -> attachmentType
        )
      ).execute
      .recover {
        case e => throw logResponse(e, "storeUpscanReference")
      }
  }

  def fetchUpscanFileDetails(regId: String, reference: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[UpscanDetails] = {
    logger.info(s"[UpscanConnector][fetchUpscanFileDetails] attempting to retrieve upscan file details. regId $regId reference $reference")
    lazy val url = appConfig.fetchUpscanFileDetails(regId, reference)

    httpClient.get(url"$url")
      .execute[UpscanDetails]
      .recover {
        case e => throw logResponse(e, "fetchUpscanFileDetails")
      }
  }

  def deleteUpscanDetails(regId: String, reference: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[Boolean] = {
    lazy val url = appConfig.fetchUpscanFileDetails(regId, reference)

    httpClient.delete(url"$url")
      .execute
      .map {
        _.status match {
          case NO_CONTENT => true
          case status => throw new InternalServerException(s"[UpscanConnector] Delete upscan details received an unexpected response Status: $status")
        }
      }
  }

  def deleteAllUpscanDetails(regId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[Boolean] = {
    lazy val url = appConfig.deleteAllUpscanDetails(regId)

    httpClient.delete(url"$url")
      .execute
      .map {
        _.status match {
          case NO_CONTENT => true
          case status => throw new InternalServerException(s"[UpscanConnector] Delete all upscan details received an unexpected response Status: $status")
        }
      }
  }

  def fetchAllUpscanDetails(regId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[Seq[UpscanDetails]] = {
    lazy val url = appConfig.fetchAllUpscanDetails(regId)
    httpClient.get(url"$url")
      .execute[Seq[UpscanDetails]]
      .recover {
        case e => throw logResponse(e, "fetchAllUpscanDetails")
      }
  }
}