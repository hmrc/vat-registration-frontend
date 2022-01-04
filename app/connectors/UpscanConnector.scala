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

package connectors

import config.FrontendAppConfig
import models.external.upscan.{UpscanDetails, UpscanResponse}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, InternalServerException}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpscanConnector @Inject()(httpClient: HttpClient, appConfig: FrontendAppConfig)(implicit executionContext: ExecutionContext) {

  def upscanInitiate()(implicit hc: HeaderCarrier): Future[UpscanResponse] = {
    lazy val url = appConfig.setupUpscanJourneyUrl
    lazy val body = Json.obj(
      "callbackUrl" -> appConfig.storeUpscanCallbackUrl,
      "successRedirect" -> controllers.test.routes.FileUploadController.callbackCheck.url,
      "minimumFileSize" -> 0,
      "maximumFileSize" -> 10485760,
      "expectedContentType" -> "image/jpeg"
    )

    httpClient.POST[JsValue, HttpResponse](url, body).map {
      case response@HttpResponse(OK, _, _) => response.json.as[UpscanResponse]

      case response => throw new InternalServerException(s"[UpscanConnector] Upscan initiate received an unexpected response Status: ${response.status}")
    }
  }

  def storeUpscanReference(regId: String, reference: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    lazy val url = appConfig.storeUpscanReferenceUrl(regId)

    httpClient.POST[String, HttpResponse](url, reference) recover {
      case e => throw logResponse(e, "storeUpscanReference")
    }
  }

  def fetchUpscanFileDetails(regId: String, reference: String)(implicit hc: HeaderCarrier): Future[UpscanDetails] = {
    lazy val url = appConfig.fetchUpscanFileDetails(regId, reference)

    httpClient.GET[UpscanDetails](url) recover {
      case e => throw logResponse(e, "fetchUpscanFileDetails")
    }
  }
}