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

package services

import connectors.UpscanConnector
import models.api.AttachmentType
import models.external.upscan.{UpscanDetails, UpscanResponse}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Request

@Singleton
class UpscanService @Inject()(upscanConnector: UpscanConnector)(implicit executionContext: ExecutionContext) {

  def initiateUpscan(regId: String, attachmentType: AttachmentType)(implicit hc: HeaderCarrier, request: Request[_]): Future[UpscanResponse] = {
    logger.info(s"[UploadDocumentController][show] attempting to initiate upscan. regId $regId")
    upscanConnector.upscanInitiate().flatMap { response =>
      logger.info(s"[UpscanService][UpscanService] upscan upload initiated. regId $regId upscanRef ${response.reference}")
      upscanConnector.storeUpscanReference(regId, response.reference, attachmentType).map(_ => response)
    }
  }

  def fetchUpscanFileDetails(regId: String, reference: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[UpscanDetails] = {
    upscanConnector.fetchUpscanFileDetails(regId, reference)
  }

  def deleteUpscanDetails(regId: String, reference: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[Boolean] = {
    upscanConnector.deleteUpscanDetails(regId, reference)
  }

  def deleteUpscanDetailsByType(regId: String, attachmentType: AttachmentType)(implicit hc: HeaderCarrier, request: Request[_]): Future[Seq[Boolean]] = {
    fetchAllUpscanDetails(regId)
      .flatMap { attachments =>
        Future.sequence(
          attachments
            .filter(details => details.attachmentType.equals(attachmentType))
            .map(_.reference)
            .map { attachmentReference =>
              deleteUpscanDetails(regId, attachmentReference)
            }
        )
      }

  }

  def deleteAllUpscanDetails(regId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[Boolean] = {
    upscanConnector.deleteAllUpscanDetails(regId)
  }

  def fetchAllUpscanDetails(regId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[Seq[UpscanDetails]] = {
    upscanConnector.fetchAllUpscanDetails(regId)
  }
}
