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

package services.mocks

import models.PersonalDetails
import models.api.PartyType
import models.external.SoleTraderIdEntity
import models.external.soletraderid.SoleTraderIdJourneyConfig
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

  val mockSoleTraderIdService: SoleTraderIdentificationService = mock[SoleTraderIdentificationService]

  def mockStartSoleTraderJourney(config: SoleTraderIdJourneyConfig,
                                 partyType: PartyType)
                                (response: Future[String]): OngoingStubbing[Future[String]] =
    when(
      mockSoleTraderIdService.startSoleTraderJourney(
        config = matches(config),
        partyType = matches(partyType)
      )(any[HeaderCarrier])
    ) thenReturn response

  def mockStartIndividualJourney(config: SoleTraderIdJourneyConfig,
                                 partyType: PartyType)
                                (response: Future[String]): OngoingStubbing[Future[String]] =
    when(
      mockSoleTraderIdService.startIndividualJourney(
        config = matches(config)
      )(any[HeaderCarrier])
    ) thenReturn response

  def mockRetrieveSoleTraderDetails(journeyId: String)(response: Future[(PersonalDetails, SoleTraderIdEntity)]): OngoingStubbing[Future[(PersonalDetails, SoleTraderIdEntity)]] =
    when(
      mockSoleTraderIdService.retrieveSoleTraderDetails(journeyId = matches(journeyId))
      (any[HeaderCarrier])
    ) thenReturn response

  def mockRetrieveIndividualDetails(journeyId: String)(response: Future[PersonalDetails]): OngoingStubbing[Future[PersonalDetails]] =
    when(
      mockSoleTraderIdService.retrieveIndividualDetails(journeyId = matches(journeyId))
      (any[HeaderCarrier])
    ) thenReturn response

}
