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

package config

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import config.startup.{VerifyCrypto, VerifyCryptoConfig}
import connectors._
import connectors.test.{BusinessRegDynamicStubConnector, BusinessRegDynamicStubConnectorImpl, TestRegistrationConnector, TestVatRegistrationConnector}
import controllers._
import controllers.callbacks.{SignInOutController, SignInOutControllerImpl}
import controllers.feedback.{FeedbackController, FeedbackControllerImpl}
import controllers.internal.{DeleteSessionItemsController, DeleteSessionItemsControllerImpl}
import controllers.test._
import controllers.{ErrorController, ErrorControllerImpl}
import features.bankAccountDetails.connectors.{BankAccountReputationConnector, BankAccountReputationConnectorImpl}
import features.bankAccountDetails.controllers.{BankAccountDetailsController, BankAccountDetailsControllerImpl}
import features.bankAccountDetails.services.{BankAccountDetailsService, BankAccountDetailsServiceImpl, BankAccountReputationService, BankAccountReputationServiceImpl}
import features.businessContact.controllers.{BusinessContactDetailsController, BusinessContactDetailsControllerImpl}
import features.businessContact.{BusinessContactService, BusinessContactServiceImpl}
import features.frs.controllers.{FlatRateController, FlatRateControllerImpl}
import features.frs.services.{FlatRateService, FlatRateServiceImpl}
import features.officer.controllers._
import features.officer.controllers.test.{TestIVController, TestIVControllerImpl}
import features.officer.services.{IVService, IVServiceImpl, LodgingOfficerService, LodgingOfficerServiceImpl}
import features.returns.controllers.{ReturnsController, ReturnsControllerImpl}
import features.returns.services.{ReturnsService, ReturnsServiceImpl}
import features.sicAndCompliance.controllers._
import features.sicAndCompliance.controllers.test.{SicStubController, SicStubControllerImpl}
import features.sicAndCompliance.services.{SicAndComplianceService, SicAndComplianceServiceImpl}
import features.tradingDetails.{TradingDetailsService, TradingDetailsServiceImpl}
import features.turnoverEstimates._
import repositories.SessionRepository
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache, ShortLivedHttpCaching}
import uk.gov.hmrc.play.config.inject.{DefaultServicesConfig, ServicesConfig}
import utils.{FeatureManager, FeatureSwitchManager, VATRegFeatureSwitch, VATRegFeatureSwitches}

class Module extends AbstractModule {

  override def configure(): Unit = {
    startupBindings()
    hmrcDependencyBindings()
    bindConnectors()
    bindServices()
    featureSwitches()
    bindTestControllers()
    bindInternalRoutes()
    bindControllers()
  }

  private def startupBindings(): Unit = {
    bind(classOf[VerifyCryptoConfig]).to(classOf[VerifyCrypto]).asEagerSingleton()
  }

  private def hmrcDependencyBindings(): Unit = {
    bind(classOf[AuthConnector]).to(classOf[AuthClientConnector]).asEagerSingleton()
    bind(classOf[ServicesConfig]).to(classOf[DefaultServicesConfig]).asEagerSingleton()
    bind(classOf[SessionCache]).to(classOf[VatSessionCache]).asEagerSingleton()
    bind(classOf[ShortLivedHttpCaching]).to(classOf[VatShortLivedHttpCaching]).asEagerSingleton()
    bind(classOf[ShortLivedCache]).to(classOf[VatShortLivedCache]).asEagerSingleton()
    bind(classOf[WSHttp]).to(classOf[Http]).asEagerSingleton()
  }

  private def bindInternalRoutes(): Unit = {
    bind(classOf[DeleteSessionItemsController]).to(classOf[DeleteSessionItemsControllerImpl])
  }

  private def bindControllers(): Unit = {
    bind(classOf[FlatRateController]).to(classOf[FlatRateControllerImpl]).asEagerSingleton()
    bind(classOf[BankAccountDetailsController]).to(classOf[BankAccountDetailsControllerImpl])
    bind(classOf[TurnoverEstimatesController]).to(classOf[TurnoverEstimatesControllerImpl]).asEagerSingleton()
    bind(classOf[ReturnsController]).to(classOf[ReturnsControllerImpl]).asEagerSingleton()
    bind(classOf[OfficerController]).to(classOf[OfficerControllerImpl]).asEagerSingleton()
    bind(classOf[TradingDetailsController]).to(classOf[TradingDetailsControllerImpl]).asEagerSingleton()
    bind(classOf[SicAndComplianceController]).to(classOf[SicAndComplianceControllerImpl]).asEagerSingleton()
    bind(classOf[LabourComplianceController]).to(classOf[LabourComplianceControllerImpl]).asEagerSingleton()
    bind(classOf[SignInOutController]).to(classOf[SignInOutControllerImpl]).asEagerSingleton()
    bind(classOf[FeedbackController]).to(classOf[FeedbackControllerImpl]).asEagerSingleton()
    bind(classOf[ApplicationSubmissionController]).to(classOf[ApplicationSubmissionControllerImpl]).asEagerSingleton()
    bind(classOf[SummaryController]).to(classOf[SummaryControllerImpl]).asEagerSingleton()
    bind(classOf[WelcomeController]).to(classOf[WelcomeControllerImpl]).asEagerSingleton()
    bind(classOf[IdentityVerificationController]).to(classOf[IdentityVerificationControllerImpl]).asEagerSingleton()
    bind(classOf[ErrorController]).to(classOf[ErrorControllerImpl]).asEagerSingleton()
  }

  private def bindTestControllers(): Unit = {
    bind(classOf[SicStubController]).to(classOf[SicStubControllerImpl]).asEagerSingleton()
    bind(classOf[BusinessContactDetailsController]).to(classOf[BusinessContactDetailsControllerImpl]).asEagerSingleton()
    bind(classOf[IncorporationInformationStubsController]).to(classOf[IncorporationInformationStubsControllerImpl]).asEagerSingleton()
    bind(classOf[TestCacheController]).to(classOf[TestCacheControllerImpl]).asEagerSingleton()
    bind(classOf[TestCTController]).to(classOf[TestCTControllerImpl]).asEagerSingleton()
    bind(classOf[TestSetupController]).to(classOf[TestSetupControllerImpl]).asEagerSingleton()
    bind(classOf[FeatureSwitchController]).to(classOf[FeatureSwitchControllerImpl]).asEagerSingleton()
    bind(classOf[TestWorkingDaysValidationController]).to(classOf[TestWorkingDaysValidationControllerImpl]).asEagerSingleton()
    bind(classOf[TestIVController]).to(classOf[TestIVControllerImpl]).asEagerSingleton()
  }

  private def bindServices(): Unit = {
    bind(classOf[CancellationService]).to(classOf[CancellationServiceImpl]).asEagerSingleton()
    bind(classOf[AddressLookupService]).to(classOf[AddressLookupServiceImpl]).asEagerSingleton()
    bind(classOf[IncorporationInformationService]).to(classOf[IncorporationInformationServiceImpl]).asEagerSingleton()
    bind(classOf[DateService]).to(classOf[DateServiceImpl]).asEagerSingleton()
    bind(classOf[S4LService]).to(classOf[S4LServiceImpl]).asEagerSingleton()
    bind(classOf[RegistrationService]).to(classOf[VatRegistrationService]).asEagerSingleton()
    bind(classOf[PrePopService]).to(classOf[PrePopulationService]).asEagerSingleton()
    bind(classOf[IVService]).to(classOf[IVServiceImpl]).asEagerSingleton()
    bind(classOf[CurrentProfileService]).to(classOf[CurrentProfileServiceImpl]).asEagerSingleton()
    bind(classOf[ReturnsService]).to(classOf[ReturnsServiceImpl]).asEagerSingleton()
    bind(classOf[BankAccountReputationService]).to(classOf[BankAccountReputationServiceImpl]).asEagerSingleton()
    bind(classOf[BankAccountDetailsService]).to(classOf[BankAccountDetailsServiceImpl]).asEagerSingleton()
    bind(classOf[TurnoverEstimatesService]).to(classOf[TurnoverEstimatesServiceImpl]).asEagerSingleton()
    bind(classOf[LodgingOfficerService]).to(classOf[LodgingOfficerServiceImpl]).asEagerSingleton()
    bind(classOf[TradingDetailsService]).to(classOf[TradingDetailsServiceImpl]).asEagerSingleton()
    bind(classOf[FlatRateService]).to(classOf[FlatRateServiceImpl]).asEagerSingleton()
    bind(classOf[SicAndComplianceService]).to(classOf[SicAndComplianceServiceImpl]).asEagerSingleton()
    bind(classOf[BusinessContactService]).to(classOf[BusinessContactServiceImpl]).asEagerSingleton()
    bind(classOf[TimeService]).to(classOf[TimeServiceImpl]).asEagerSingleton()
  }

  private def bindConnectors(): Unit = {
    bind(classOf[AddressLookupConnector]).to(classOf[AddressLookupConnectorImpl]).asEagerSingleton()
    bind(classOf[PPConnector]).to(classOf[PrePopConnector]).asEagerSingleton()
    bind(classOf[TestRegistrationConnector]).to(classOf[TestVatRegistrationConnector]).asEagerSingleton()
    bind(classOf[BankHolidaysConnector]).annotatedWith(Names.named("fallback")).to(classOf[FallbackBankHolidaysConnector]).asEagerSingleton()
    bind(classOf[BankHolidaysConnector]).to(classOf[WSBankHolidaysConnector]).asEagerSingleton()
    bind(classOf[IVConnector]).to(classOf[IVConnectorImpl]).asEagerSingleton()
    bind(classOf[BankAccountReputationConnector]).to(classOf[BankAccountReputationConnectorImpl]).asEagerSingleton()
    bind(classOf[BusinessRegistrationConnect]).to(classOf[BusinessRegistrationConnector]).asEagerSingleton()
    bind(classOf[CompanyRegistrationConnector]).to(classOf[CompanyRegistrationConnectorImpl]).asEagerSingleton()
    bind(classOf[S4LConnector]).to(classOf[S4LConnectorImpl]).asEagerSingleton()
    bind(classOf[KeystoreConnector]).to(classOf[KeystoreConnectorImpl]).asEagerSingleton()
    bind(classOf[RegistrationConnector]).to(classOf[VatRegistrationConnector]).asEagerSingleton()
    bind(classOf[IncorporationInformationConnector]).to(classOf[IncorporationInformationConnectorImpl]).asEagerSingleton()
    bind(classOf[BusinessRegDynamicStubConnector]).to(classOf[BusinessRegDynamicStubConnectorImpl]).asEagerSingleton()
    bind(classOf[ConfigConnector]).to(classOf[ConfigConnectorImpl]).asEagerSingleton()
  }

  private def featureSwitches(): Unit = {
    bind(classOf[FeatureManager]).to(classOf[FeatureSwitchManager]).asEagerSingleton()
    bind(classOf[VATRegFeatureSwitches]).to(classOf[VATRegFeatureSwitch]).asEagerSingleton()
  }
}
