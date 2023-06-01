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

package services.mocks

import models.api.PartyType
import models.external.IncorporatedEntity
import models.external.incorporatedentityid.IncorpIdJourneyConfig
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import services.IncorpIdService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import play.api.mvc.Request

trait IncorpIdServiceMock {
  this: MockitoSugar =>

  val mockIncorpIdService: IncorpIdService = mock[IncorpIdService]

  def mockCreateJourney(incorpIdJourneyConfig: IncorpIdJourneyConfig, partyType: PartyType)(response: Future[String]): Unit =
    when(mockIncorpIdService.createJourney(
      ArgumentMatchers.eq(incorpIdJourneyConfig),
      ArgumentMatchers.eq(partyType)
    )(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[Request[_]]))
      .thenReturn(response)

  def mockGetDetails(journeyId: String)(response: Future[IncorporatedEntity]): Unit =
    when(mockIncorpIdService.getDetails(ArgumentMatchers.eq(journeyId))(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[Request[_]]))
      .thenReturn(response)

}
