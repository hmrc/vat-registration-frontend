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

package services

import connectors.{CompanyRegistrationConnector, KeystoreConnector, VatRegistrationConnector}
import helpers.VatSpec
import models.S4LVatSicAndCompliance
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any

import scala.concurrent.Future

class SicAndComplianceServiceSpec extends VatSpec {

  trait Setup {
    val service: RegistrationService = new RegistrationService {
      override val s4LService: S4LService = mockS4LService
      override val vatRegConnector: VatRegistrationConnector = mockRegConnector
      override val compRegConnector: CompanyRegistrationConnector = mockCompanyRegConnector
      override val incorporationService: IncorporationInformationService = mockIIService
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "fetchSicFromS4L" should {

    "return a S4LVatSicAndCompliance if one is found in Save 4 Later" in new Setup {

      when(mockS4LService.fetchAndGetNoAux[S4LVatSicAndCompliance](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(s4LVatSicAndCompliance)))

      val result: Option[S4LVatSicAndCompliance] = await(service.fetchSicFromS4L)

      result mustBe Some(s4LVatSicAndCompliance)
    }
  }
}
