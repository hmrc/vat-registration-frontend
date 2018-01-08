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

import connectors._
import features.iv.services.IdentityVerificationService
import features.officer.services.LodgingOfficerService
import org.mockito.Mockito.reset
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import services._
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.audit.model.Audit
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

trait VatMocks
  extends SaveForLaterMock
    with KeystoreMock
    with WSHTTPMock {

  this: MockitoSugar =>

  implicit lazy val mockMessagesApi = mock[MessagesApi]
  implicit lazy val mockAuthConnector = mock[AuthConnector]
  implicit lazy val mockSessionCache = mock[SessionCache]
  implicit lazy val mockAudit = mock[Audit]
  implicit lazy val mockS4LService = mock[S4LService]
  implicit lazy val mockCurrentProfile = mock[CurrentProfileService]
  implicit lazy val mockAddressService = mock[AddressLookupService]
  implicit lazy val mockRegConnector = mock[VatRegistrationConnector]
  implicit lazy val mockCompanyRegConnector = mock[CompanyRegistrationConnector]
  implicit lazy val mockPPConnector = mock[PPConnector]
  implicit lazy val mockPPService = mock[PrePopulationService]
  implicit lazy val mockIIService = mock[IncorporationInformationService]
  implicit lazy val mockIIConnector = mock[IncorporationInformationConnector]
  implicit lazy val mockConfigConnector = mock[ConfigConnector]
  implicit lazy val mockVatRegistrationService = mock[VatRegistrationService]
  implicit lazy val mockAddressLookupConnector = mock[AddressLookupConnector]
  implicit lazy val mockDateService = mock[DateService]
  implicit lazy val mockIncorpInfoService = mock[IncorporationInfoSrv]
  implicit lazy val mockIdentityVerificationConnector = mock[IdentityVerificationConnector]
  implicit lazy val mockBankAccountReputationService = mock[BankAccountReputationServiceImpl]
  implicit lazy val mockBankAccountReputationConnector = mock[BankAccountReputationConnector]
  implicit lazy val mockIVService = mock[IdentityVerificationService]
  implicit lazy val mockConfig = mock[ServicesConfig]
  implicit lazy val mockLodgingOfficerService = mock[LodgingOfficerService]

  def resetMocks() {
    reset(
      mockVatRegistrationService,
      mockS4LConnector,
      mockS4LConnector,
      mockS4LService,
      mockKeystoreConnector,
      mockAuthConnector,
      mockSessionCache,
      mockAudit,
      mockVatRegistrationService,
      mockRegConnector,
      mockCompanyRegConnector,
      mockPPConnector,
      mockPPService,
      mockDateService,
      mockIIConnector,
      mockConfigConnector,
      mockIIService,
      mockAddressLookupConnector,
      mockWSHttp,
      mockCurrentProfile,
      mockIdentityVerificationConnector,
      mockIVService,
      mockLodgingOfficerService
    )
  }
}
