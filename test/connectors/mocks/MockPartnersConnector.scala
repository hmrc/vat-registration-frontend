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

package connectors.mocks

import connectors.PartnersConnector
import models.PartnerEntity
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockPartnersConnector extends MockitoSugar {
  self: Suite =>

  val mockPartnersConnector: PartnersConnector = mock[PartnersConnector]

  def mockGetAllPartners(regId: String)(response: List[PartnerEntity]): OngoingStubbing[Future[List[PartnerEntity]]] =
    when(mockPartnersConnector.getAllPartners(
      ArgumentMatchers.eq(regId)
    )(ArgumentMatchers.any[HeaderCarrier]
    )) thenReturn Future.successful(response)

  def mockGetPartner(regId: String, index: Int)(response: Option[PartnerEntity]): OngoingStubbing[Future[Option[PartnerEntity]]] =
    when(mockPartnersConnector.getPartner(
      ArgumentMatchers.eq(regId),
      ArgumentMatchers.eq(index)
    )(ArgumentMatchers.any[HeaderCarrier]
    )) thenReturn Future.successful(response)

  def mockUpsertPartner(regId: String, index: Int, partner: PartnerEntity)(response: PartnerEntity): OngoingStubbing[Future[PartnerEntity]] =
    when(mockPartnersConnector.upsertPartner(
      ArgumentMatchers.eq(regId),
      ArgumentMatchers.eq(index),
      ArgumentMatchers.eq(partner)
    )(ArgumentMatchers.any[HeaderCarrier]
    )) thenReturn Future.successful(response)

  def mockDeletePartner(regId: String, index: Int)(response: Boolean): OngoingStubbing[Future[Boolean]] =
    when(mockPartnersConnector.deletePartner(
      ArgumentMatchers.eq(regId),
      ArgumentMatchers.eq(index)
    )(ArgumentMatchers.any[HeaderCarrier]
    )) thenReturn Future.successful(response)

}
