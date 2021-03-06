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

package testHelpers

import connectors.mocks.{AuthMock, HttpClientMock, KeystoreMock, MockS4lConnector}
import connectors._
import org.mockito.Mockito.reset
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import repositories.SessionRepository
import services.mocks.{IncorpIdServiceMock, PersonalDetailsValidationServiceMock, SicAndComplianceServiceMock}
import services._
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.Audit
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.CascadeUpsert

trait VatMocks
  extends MockS4lConnector
    with AuthMock
    with KeystoreMock
    with HttpClientMock
    with SicAndComplianceServiceMock
    with IncorpIdServiceMock
    with PersonalDetailsValidationServiceMock {
  this: MockitoSugar =>

  implicit lazy val mockAudit = mock[Audit]
  implicit lazy val mockSessionRepository = mock[SessionRepository]
  implicit lazy val mockCascadeUpsert = mock[CascadeUpsert]
  implicit lazy val mockSessionCache = mock[SessionCache]
  implicit lazy val mockS4LService = mock[S4LService]
  implicit lazy val mockServicesConfig = mock[ServicesConfig]

  implicit lazy val mockMessagesAPI: MessagesApi = mock[MessagesApi]
  //Connectors
  implicit lazy val mockVatRegistrationConnector = mock[VatRegistrationConnector]
  implicit lazy val mockConfigConnector = mock[ConfigConnector]
  implicit lazy val mockAddressLookupConnector = mock[AddressLookupConnector]
  implicit lazy val mockBankAccountReputationConnector = mock[BankAccountReputationConnector]
  implicit lazy val mockICLConnector = mock[ICLConnector]
  //Services
  implicit lazy val mockCurrentProfileService = mock[CurrentProfileService]
  implicit lazy val mockCancellationService = mock[CancellationService]
  implicit lazy val mockAddressLookupService = mock[AddressLookupService]
  implicit lazy val mockVatRegistrationService = mock[VatRegistrationService]
  implicit lazy val mockDateService = mock[DateService]
  implicit lazy val mockBankAccountReputationService = mock[BankAccountReputationService]
  implicit lazy val mockReturnsService = mock[ReturnsService]
  implicit lazy val mockApplicantDetailsServiceOld = mock[ApplicantDetailsService]
  implicit lazy val mockFlatRateService = mock[FlatRateService]
  implicit lazy val mockPrePopulationService: PrePopulationService = mock[PrePopulationService]
  implicit lazy val mockTrafficManagementService = mock[TrafficManagementService]
  lazy val mockTradingDetailsService = mock[TradingDetailsService]
  lazy val mockSummaryService: SummaryService = mock[SummaryService]
  lazy val mockBusinessContactService = mock[BusinessContactService]
  val mockTimeService = mock[TimeService]
  lazy val mockICLService = mock[ICLService]
  val mockAuditConnector = mock[AuditConnector]
  implicit lazy val mockSaveAndRetrieveService = mock[SaveAndRetrieveService]

  def resetMocks() {
    reset(
      mockHttpClient,
      mockVatRegistrationService,
      mockS4LConnector,
      mockS4LConnector,
      mockS4LService,
      mockKeystoreConnector,
      mockSessionCache,
      mockSessionRepository,
      mockCascadeUpsert,
      mockAudit,
      mockVatRegistrationService,
      mockVatRegistrationConnector,
      mockPrePopulationService,
      mockDateService,
      mockConfigConnector,
      mockAddressLookupConnector,
      mockCurrentProfileService,
      mockReturnsService,
      mockApplicantDetailsServiceOld,
      mockFlatRateService,
      mockSicAndComplianceService,
      mockMessagesAPI,
      mockPrePopulationService,
      mockSummaryService,
      mockBusinessContactService,
      mockAuthClientConnector,
      mockTimeService,
      mockTrafficManagementService
    )
  }
}
