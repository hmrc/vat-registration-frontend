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

package mocks

import org.mockito.{ArgumentMatchers => Matchers}
import features.sicAndCompliance.services.SicAndComplianceService
import models.{CurrentProfile, S4LVatSicAndCompliance}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait SicAndComplianceServiceMock {
  this: MockitoSugar =>

  lazy val mockSicAndComplianceSrv = mock[SicAndComplianceService]

  def mockGetSicAndCompliance(res: Future[S4LVatSicAndCompliance]) : OngoingStubbing[Future[S4LVatSicAndCompliance]] = {
    when(mockSicAndComplianceSrv.getSicAndCompliance(Matchers.any[HeaderCarrier],Matchers.any[CurrentProfile])).thenReturn(res)
  }

  def mockUpdateSicAndCompliance(res:Future[S4LVatSicAndCompliance]) : OngoingStubbing[Future[S4LVatSicAndCompliance]] = {
    when(mockSicAndComplianceSrv.updateSicAndCompliance(Matchers.any)(Matchers.any[HeaderCarrier],Matchers.any[CurrentProfile])).thenReturn(res)
  }

}
