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
import helpers.VatSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class TurnoverEstimatesServiceSpec extends VatSpec {

  trait Setup {
    val service: TurnoverEstimatesService = new TurnoverEstimatesService {
      override val vatRegConnector: VatRegistrationConnector = mockRegConnector
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

    "return a TurnoverEstimates case class after a successful call to the connector" in new Setup {
      when(mockRegConnector.patchTurnoverEstimates(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      val result: TurnoverEstimates = await(service.saveTurnoverEstimates(turnoverEstimates))
      result mustBe turnoverEstimates
    }
  }
}
