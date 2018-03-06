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
import features.bankAccountDetails.connectors.BankAccountReputationConnectorImpl
import features.bankAccountDetails.services.BankAccountReputationServiceImpl
import features.businessContact.BusinessContactService
import features.officer.services.{IVServiceImpl, LodgingOfficerService}
import features.returns.services.ReturnsService
import features.turnoverEstimates.TurnoverEstimatesService
import org.mockito.Mockito.reset
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import services._
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.audit.model.Audit
import uk.gov.hmrc.play.config.inject.ServicesConfig
import utils.{VATRegFeatureSwitch, VATRegFeatureSwitches}

trait VatMocks
  extends SaveForLaterMock
    with AuthMock
    with KeystoreMock
    with WSHTTPMock
    with SicAndComplianceServiceMock {
  this: MockitoSugar =>


  implicit lazy val mockFeatureSwitch = mock[VATRegFeatureSwitch]
  implicit lazy val mockFeatureSwitches  = mock[VATRegFeatureSwitches]
  implicit lazy val mockAudit = mock[Audit]
  implicit lazy val mockSessionCache = mock[SessionCache]
  implicit lazy val mockS4LService = mock[S4LService]
  implicit lazy val mockConfig = mock[ServicesConfig]
  implicit lazy val mockMessagesAPI: MessagesApi = mock[MessagesApi]
  //Connectors
  implicit lazy val mockCompanyRegConnector = mock[CompanyRegistrationConnector]
  implicit lazy val mockIIConnector = mock[IncorporationInformationConnector]
  implicit lazy val mockRegConnector = mock[VatRegistrationConnector]
  implicit lazy val mockConfigConnector = mock[ConfigConnector]
  implicit lazy val mockAddressLookupConnector = mock[AddressLookupConnector]
  implicit lazy val mockIdentityVerificationConnector = mock[IVConnectorImpl]
  implicit lazy val mockBankAccountReputationConnector = mock[BankAccountReputationConnectorImpl]
  implicit lazy val mockPPConnector = mock[PPConnector]
  //Services
  implicit lazy val mockCurrentProfile = mock[CurrentProfileService]
  implicit lazy val mockAddressService = mock[AddressLookupService]
  implicit lazy val mockPPService = mock[PrePopulationService]
  implicit lazy val mockIIService = mock[IncorporationInformationServiceImpl]
  implicit lazy val mockVatRegistrationService = mock[VatRegistrationService]
  implicit lazy val mockDateService = mock[DateService]
  implicit lazy val mockIncorpInfoService = mock[IncorporationInformationService]
  implicit lazy val mockBankAccountReputationService = mock[BankAccountReputationServiceImpl]
  implicit lazy val mockIVService = mock[IVServiceImpl]
  implicit lazy val mockReturnsService = mock[ReturnsService]
  implicit lazy val mockLodgingOfficerService = mock[LodgingOfficerService]
  implicit lazy val mockTurnoverEstimatesService = mock[TurnoverEstimatesService]
  implicit lazy val mockFlatRateService = mock[FlatRateService]
  implicit lazy val mockPrePopService: PrePopService = mock[PrePopService]
  lazy val mockBusinessContactService = mock[BusinessContactService]


  def resetMocks() {
    reset(
      mockVatRegistrationService,
      mockS4LConnector,
      mockS4LConnector,
      mockS4LService,
      mockKeystoreConnector,
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
      mockReturnsService,
      mockLodgingOfficerService,
      mockTurnoverEstimatesService,
      mockFlatRateService,
      mockSicAndComplianceService,
      mockMessagesAPI,
      mockPrePopService,
      mockBusinessContactService,
      mockAuthClientConnector
    )
  }
}
