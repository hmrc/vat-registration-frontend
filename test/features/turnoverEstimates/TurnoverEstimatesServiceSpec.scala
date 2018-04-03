/*
 * Copyright 2018 HM Revenue & Customs
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

package features.turnoverEstimates

import connectors.VatRegistrationConnector
import features.frs.services.FlatRateService
import helpers.VatSpec
import models.api.SicCode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class TurnoverEstimatesServiceSpec extends VatSpec {

  trait Setup {
    val service: TurnoverEstimatesService = new TurnoverEstimatesService {
      override val vatRegConnector: VatRegistrationConnector = mockRegConnector
      override val frsService: FlatRateService = mockFlatRateService
    }
  }

  val turnoverEstimates = TurnoverEstimates(1000L)

  "fetchTurnoverEstimates" should {

    "return a TurnoverEstimates case class from the connector" in new Setup {
      when(mockRegConnector.getTurnoverEstimates(any(), any()))
        .thenReturn(Future.successful(Some(turnoverEstimates)))

      val result: Option[TurnoverEstimates] = await(service.fetchTurnoverEstimates)
      result mustBe Some(turnoverEstimates)
    }
  }

  "saveTurnoverEstimates" should {

    "return a TurnoverEstimates from connector estimate is > 150k so FRS is cleared and updated with false" in new Setup {
      val toe = TurnoverEstimates(150001L)
      when(mockRegConnector.patchTurnoverEstimates(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      when(mockFlatRateService.clearFrs(any(),any()))
        .thenReturn(Future.successful(true))

      when(mockFlatRateService.saveJoiningFRS(any())(any(),any())).thenReturn(Future.successful(validFlatRate))

      val result: TurnoverEstimates = await(service.saveTurnoverEstimates(toe))
      result mustBe toe
      verify(mockFlatRateService, times(1)).saveJoiningFRS(any())(any(),any())
    }
    "return a TurnoverEstimates from connector estimate is <= 150k so FRS is NOT cleared and no call is made to save it" in new Setup {
      val toe = TurnoverEstimates(150000L)
      when(mockRegConnector.patchTurnoverEstimates(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      val result: TurnoverEstimates = await(service.saveTurnoverEstimates(toe))
      result mustBe toe
      verify(mockFlatRateService, times(0)).saveJoiningFRS(any())(any(),any())
    }
    "return future failed if TurnoverEstimates > 150k but delete clear frs returns failure" in new Setup {
      val toe = TurnoverEstimates(150001L)
      when(mockRegConnector.patchTurnoverEstimates(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      when(mockFlatRateService.clearFrs(any(),any()))
        .thenReturn(Future.failed(new Exception("foo")))

      an[Exception] mustBe thrownBy(await(service.saveTurnoverEstimates(toe)))
    }
  }
}
