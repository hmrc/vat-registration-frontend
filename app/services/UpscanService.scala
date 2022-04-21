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

package services

import connectors.UpscanConnector
import models.api.AttachmentType
import models.external.upscan.{UpscanDetails, UpscanResponse}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpscanService @Inject()(upscanConnector: UpscanConnector)(implicit executionContext: ExecutionContext) {

  def initiateUpscan(regId: String, attachmentType: AttachmentType)(implicit hc: HeaderCarrier): Future[UpscanResponse] = {
    upscanConnector.upscanInitiate().flatMap { response =>
      upscanConnector.storeUpscanReference(regId, response.reference, attachmentType).map(_ => response)
    }
  }

  def fetchUpscanFileDetails(regId: String, reference: String)(implicit hc: HeaderCarrier): Future[UpscanDetails] = {
    upscanConnector.fetchUpscanFileDetails(regId, reference)
  }

  def fetchAllUpscanDetails(regId: String)(implicit hc: HeaderCarrier): Future[Seq[UpscanDetails]] = {
    upscanConnector.fetchAllUpscanDetails(regId)
  }

  def deleteUpscanDetails(regId: String, reference: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    upscanConnector.deleteUpscanDetails(regId, reference)
  }
}
