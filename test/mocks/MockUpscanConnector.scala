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

package mocks

import connectors.UpscanConnector
import models.external.upscan.{UpscanDetails, UpscanResponse}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

trait MockUpscanConnector extends MockitoSugar {
  self: Suite =>

  val mockUpscanConnector: UpscanConnector = mock[UpscanConnector]

  def mockUpscanInitiate(res: Future[UpscanResponse]): OngoingStubbing[Future[UpscanResponse]] =
    when(mockUpscanConnector.upscanInitiate()(ArgumentMatchers.any[HeaderCarrier])).thenReturn(res)

  def mockStoreUpscanReference(regId: String, reference: String)(res: Future[HttpResponse]): OngoingStubbing[Future[HttpResponse]] =
    when(mockUpscanConnector.storeUpscanReference(
      ArgumentMatchers.eq(regId),
      ArgumentMatchers.eq(reference)
    )(
      ArgumentMatchers.any[HeaderCarrier]
    )).thenReturn(res)

  def mockFetchUpscanFileDetails(reference: String)(res: Future[UpscanDetails]): OngoingStubbing[Future[UpscanDetails]] =
    when(mockUpscanConnector.fetchUpscanFileDetails(
      ArgumentMatchers.eq(reference)
    )(
      ArgumentMatchers.any[HeaderCarrier]
    )).thenReturn(res)
}
