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

package services.mocks

import models.TransactorDetails
import models.api.PartyType
import models.external.SoleTraderIdEntity
import org.mockito.ArgumentMatchers.{any, eq => matches}
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import services.SoleTraderIdentificationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockSoleTraderIdService extends MockitoSugar {
  self: Suite =>

  val mockSoleTraderIdService = mock[SoleTraderIdentificationService]

  def mockStartJourney(continueUrl: String,
                       serviceName: String,
                       deskproId: String,
                       signOutUrl: String,
                       enableSautrCheck: Boolean,
                       partyType: PartyType)
                      (response: Future[String]): OngoingStubbing[Future[String]] =
    when(
      mockSoleTraderIdService.startJourney(
        continueUrl = matches(continueUrl),
        serviceName = matches(serviceName),
        deskproId = matches(deskproId),
        signOutUrl = matches(signOutUrl),
        enableSautrCheck = matches(enableSautrCheck),
        partyType = matches(partyType)
      )(any[HeaderCarrier])
    ) thenReturn response

  def mockRetrieveSoleTraderDetails(journeyId: String)(response: Future[(TransactorDetails, SoleTraderIdEntity)]): OngoingStubbing[Future[(TransactorDetails, SoleTraderIdEntity)]] =
    when(
      mockSoleTraderIdService.retrieveSoleTraderDetails(journeyId = matches(journeyId))
      (any[HeaderCarrier])
    ) thenReturn response

}
