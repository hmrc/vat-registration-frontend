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

package testHelpers

import connectors._
import connectors.mocks.{AuthMock, HttpClientMock, MockS4lConnector, SessionServiceMock}
import org.mockito.Mockito.reset
import org.scalatestplus.mockito.MockitoSugar
import play.api.cache.SyncCacheApi
import play.api.i18n.MessagesApi
import repositories.SessionRepository
import services._
import services.mocks.{BusinessServiceMock, IncorpIdServiceMock, PersonalDetailsValidationServiceMock}
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.Audit
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.CascadeUpsert

trait VatMocks
  extends MockS4lConnector
    with AuthMock
    with SessionServiceMock
    with HttpClientMock
    with BusinessServiceMock
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
  implicit lazy val mockCurrentProfileService = mock[JourneyService]
  implicit lazy val mockAddressLookupService = mock[AddressLookupService]
  implicit lazy val mockVatRegistrationService = mock[VatRegistrationService]
  implicit lazy val mockBankAccountReputationService = mock[BankAccountReputationService]
  implicit lazy val movkVatApplicationService = mock[VatApplicationService]
  implicit lazy val mockApplicantDetailsServiceOld = mock[ApplicantDetailsService]
  implicit lazy val mockFlatRateService = mock[FlatRateService]
  implicit lazy val mockTrafficManagementService = mock[TrafficManagementService]
  implicit lazy val mockAttachmentsService = mock[AttachmentsService]
  lazy val mockTradingDetailsService = mock[TradingDetailsService]
  lazy val mockSummaryService: SummaryService = mock[SummaryService]

  val mockTimeService = mock[TimeService]
  lazy val mockICLService = mock[ICLService]
  val mockAuditConnector = mock[AuditConnector]
  implicit lazy val mockSaveAndRetrieveService = mock[SaveAndRetrieveService]
  lazy val mockBankHolidayConnector: BankHolidaysConnector = mock[BankHolidaysConnector]
  lazy val mockCache: SyncCacheApi = mock[SyncCacheApi]

  def resetMocks() {
    reset(
      mockHttpClient,
      mockS4LConnector,
      mockS4LService,
      mockSessionService,
      mockSessionCache,
      mockSessionRepository,
      mockCascadeUpsert,
      mockAudit,
      mockVatRegistrationService,
      mockVatRegistrationConnector,
      mockConfigConnector,
      mockAddressLookupConnector,
      mockCurrentProfileService,
      movkVatApplicationService,
      mockAttachmentsService,
      mockApplicantDetailsServiceOld,
      mockFlatRateService,
      mockMessagesAPI,
      mockSummaryService,
      mockAuthClientConnector,
      mockTimeService,
      mockTrafficManagementService,
      mockBankHolidayConnector,
      mockCache
    )
  }
}
