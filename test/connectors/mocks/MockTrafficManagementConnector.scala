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

import connectors.TrafficManagementConnector
import models.api.trafficmanagement.ClearTrafficManagementResponse
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockTrafficManagementConnector extends MockitoSugar {
  self: Suite =>

  val mockTrafficManagementConnector = mock[TrafficManagementConnector]

  def mockClearTrafficManagement(response: Future[ClearTrafficManagementResponse]): OngoingStubbing[Future[ClearTrafficManagementResponse]] =
    when(mockTrafficManagementConnector.clearTrafficManagement(any[HeaderCarrier])) thenReturn response

}
