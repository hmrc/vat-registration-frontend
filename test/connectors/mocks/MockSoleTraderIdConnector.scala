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

package connectors.mocks

import connectors.SoleTraderIdentificationConnector
import models.TransactorDetails
import models.external.incorporatedentityid.SoleTrader
import models.external.soletraderid.SoleTraderIdJourneyConfig
import org.mockito.ArgumentMatchers.{any, eq => matches}
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockSoleTraderIdConnector extends MockitoSugar {
  self: Suite =>

  val mockSoleTraderIdConnector = mock[SoleTraderIdentificationConnector]

  def mockStartJourney(config: SoleTraderIdJourneyConfig)(response: Future[String]): OngoingStubbing[Future[String]] =
    when(mockSoleTraderIdConnector.startJourney(
      config = matches(config)
    )(any[HeaderCarrier])) thenReturn response

  def mockRetrieveSoleTraderDetails(journeyId: String)(response: Future[(TransactorDetails, Option[SoleTrader])]): OngoingStubbing[Future[(TransactorDetails, Option[SoleTrader])]] =
    when(mockSoleTraderIdConnector.retrieveSoleTraderDetails(
      journeyId = matches(journeyId)
    )(any[HeaderCarrier])) thenReturn response

}
