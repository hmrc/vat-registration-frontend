/*
 * Copyright 2017 HM Revenue & Customs
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

import connectors._
import org.mockito.Mockito
import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import services._
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.audit.model.Audit
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

trait VatMocks
  extends SaveForLaterMock
    with KeystoreMock
    with WSHTTPMock {

  this: MockitoSugar =>
  implicit lazy val mockAuthConnector = mock[AuthConnector]
  implicit lazy val mockSessionCache = mock[SessionCache]
  implicit lazy val mockAudit = mock[Audit]
  implicit lazy val mockS4LService = mock[S4LService]
  implicit lazy val mockRegConnector = mock[VatRegistrationConnector]
  implicit lazy val mockCompanyRegConnector = mock[CompanyRegistrationConnector]
  implicit lazy val mockPPConnector = Mockito.mock(classOf[PPConnector])
  implicit lazy val mockPPService = Mockito.mock(classOf[PrePopulationService])
  implicit lazy val mockIIService = Mockito.mock(classOf[IncorporationInformationService])
  implicit lazy val mockIIConnector = Mockito.mock(classOf[IncorporationInformationConnector])
  implicit lazy val mockVatRegistrationService = Mockito.mock(classOf[VatRegistrationService])
  implicit lazy val mockAddressLookupConnector = Mockito.mock(classOf[AddressLookupConnect])
  implicit lazy val mockDateService = mock[DateService]

}
