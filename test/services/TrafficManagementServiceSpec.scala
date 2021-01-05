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

package services

import java.time.LocalDate

import connectors.TrafficManagementConnector
import models.api.trafficmanagement.{Draft, RegistrationInformation, VatReg}
import org.mockito.Mockito.when
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.Future

class TrafficManagementServiceSpec extends VatRegSpec {

  val mockTrafficManagementConnector: TrafficManagementConnector = mock[TrafficManagementConnector]

  object TestTrafficManagementService extends TrafficManagementService(mockTrafficManagementConnector)

  "passedTrafficManagement" should {
    "return true if TM was passed and regId matches" in {
      val registrationInformation = RegistrationInformation("testIntId", testRegId, Draft, Some(LocalDate.now()), VatReg)
      when(mockTrafficManagementConnector.getRegistrationInformation)
        .thenReturn(Future.successful(Some(registrationInformation)))

      val res = TestTrafficManagementService.passedTrafficManagement(testRegId)

      await(res) mustBe true
    }

    "fail if TM was passed and regId does not match" in {
      val registrationInformation = RegistrationInformation("testIntId", "wrongRegId", Draft, Some(LocalDate.now()), VatReg)
      when(mockTrafficManagementConnector.getRegistrationInformation)
        .thenReturn(Future.successful(Some(registrationInformation)))

      val res = TestTrafficManagementService.passedTrafficManagement(testRegId)

      intercept[InternalServerException](await(res))
    }

    "return false if TM was not passed" in {
      when(mockTrafficManagementConnector.getRegistrationInformation)
        .thenReturn(Future.successful(None))

      val res = TestTrafficManagementService.passedTrafficManagement(testRegId)

      await(res) mustBe false
    }
  }
}
