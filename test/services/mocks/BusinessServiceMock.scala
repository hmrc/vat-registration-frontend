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

import models.{Business, CurrentProfile}
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.mockito.{ArgumentMatchers => Matchers}
import org.scalatestplus.mockito.MockitoSugar
import services.BusinessService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait BusinessServiceMock {
  this: MockitoSugar =>

  lazy val mockBusinessService = mock[BusinessService]

  def mockGetBusiness(res: Future[Business]): OngoingStubbing[Future[Business]] = {
    when(mockBusinessService.getBusiness(Matchers.any[CurrentProfile], Matchers.any[HeaderCarrier])).thenReturn(res)
  }

  def mockUpdateBusiness(res: Future[Business]): OngoingStubbing[Future[Business]] = {
    when(mockBusinessService.updateBusiness(Matchers.any)(Matchers.any[CurrentProfile], Matchers.any[HeaderCarrier])).thenReturn(res)
  }

}
