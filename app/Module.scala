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

import java.time.LocalDate
import javax.inject.Singleton

import com.google.inject.name.Names
import com.google.inject.{AbstractModule, TypeLiteral}
import common.Now
import config._
import config.startup.{VerifyCrypto, VerifyCryptoConfig}
import connectors._
import connectors.test.{TestRegistrationConnector, TestVatRegistrationConnector}
import controllers.frs._
import controllers.internal.{DeleteSessionItemsController, DeleteSessionItemsControllerImpl}
import controllers.test.{FeatureSwitchController, FeatureSwitchCtrl}
import features.bankAccountDetails.{BankAccountDetailsController, BankAccountDetailsControllerImpl}
import features.iv.services.{IVService, IdentityVerificationService}
import features.returns.{ReturnsController, ReturnsControllerImpl, ReturnsService, ReturnsServiceImpl}
import features.turnoverEstimates._
import services._
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache, ShortLivedHttpCaching}
import uk.gov.hmrc.play.config.inject.{DefaultServicesConfig, ServicesConfig}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.{FeatureManager, FeatureSwitchManager, VATRegFeatureSwitch, VATRegFeatureSwitches}

@Singleton
class LocalDateNow extends Now[LocalDate] {
  override def apply(): LocalDate = LocalDate.now()
}

class Module extends AbstractModule {

  override def configure(): Unit = {
    startupBindings()
    bind(new TypeLiteral[Now[LocalDate]] {}).to(classOf[LocalDateNow]).asEagerSingleton()
    hmrcDependencyBindings()
    bindControllers()
    bindInternalRoutes()
    bindServices()
    bindConnectors()
    featureSwitches()
  }

  private def startupBindings(): Unit = {
    bind(classOf[VerifyCryptoConfig]).to(classOf[VerifyCrypto]).asEagerSingleton()
  }

  private def hmrcDependencyBindings(): Unit = {
    bind(classOf[AuthConnector]).to(classOf[FrontendAuthConnector]).asEagerSingleton()
    bind(classOf[ServicesConfig]).to(classOf[DefaultServicesConfig]).asEagerSingleton()
    bind(classOf[SessionCache]).to(classOf[VatSessionCache]).asEagerSingleton()
    bind(classOf[ShortLivedHttpCaching]).to(classOf[VatShortLivedHttpCaching]).asEagerSingleton()
    bind(classOf[ShortLivedCache]).to(classOf[VatShortLivedCache]).asEagerSingleton()
    bind(classOf[WSHttp]).to(classOf[Http]).asEagerSingleton()
  }

  private def bindInternalRoutes(): Unit = {

  }

  private def bindControllers(): Unit = {
    bind(classOf[DeleteSessionItemsController]).to(classOf[DeleteSessionItemsControllerImpl]).asEagerSingleton()
    bind(classOf[JoinFrsController]).to(classOf[JoinFrsControllerImpl]).asEagerSingleton()
    bind(classOf[AnnualCostsInclusiveController]).to(classOf[AnnualCostsInclusiveControllerImpl]).asEagerSingleton()
    bind(classOf[AnnualCostsLimitedController]).to(classOf[AnnualCostsLimitedControllerImpl]).asEagerSingleton()
    bind(classOf[RegisterForFrsWithSectorController]).to(classOf[RegisterForFrsWithSectorControllerImpl]).asEagerSingleton()
    bind(classOf[RegisterForFrsController]).to(classOf[RegisterForFrsControllerImpl]).asEagerSingleton()
    bind(classOf[ConfirmBusinessSectorController]).to(classOf[ConfirmBusinessSectorControllerImpl]).asEagerSingleton()
    bind(classOf[FrsStartDateController]).to(classOf[FrsStartDateControllerImpl]).asEagerSingleton()
    bind(classOf[FeatureSwitchCtrl]).to(classOf[FeatureSwitchController]).asEagerSingleton()
    bind(classOf[BankAccountDetailsController]).to(classOf[BankAccountDetailsControllerImpl])
    bind(classOf[TurnoverEstimatesController]).to(classOf[TurnoverEstimatesControllerImpl]).asEagerSingleton()
    bind(classOf[ReturnsController]).to(classOf[ReturnsControllerImpl]).asEagerSingleton()
  }

  private def bindServices(): Unit = {
    bind(classOf[CancellationService]).to(classOf[CancellationServiceImpl]).asEagerSingleton()
    bind(classOf[AddressLookupService]).to(classOf[AddressLookupServiceImpl]).asEagerSingleton()
    bind(classOf[IncorporationInfoSrv]).to(classOf[IncorporationInformationService]).asEagerSingleton()
    bind(classOf[DateService]).to(classOf[WorkingDaysService]).asEagerSingleton()
    bind(classOf[S4LService]).to(classOf[PersistenceService]).asEagerSingleton()
    bind(classOf[RegistrationService]).to(classOf[VatRegistrationService]).asEagerSingleton()
    bind(classOf[PrePopService]).to(classOf[PrePopulationService]).asEagerSingleton()
    bind(classOf[IVService]).to(classOf[IdentityVerificationService]).asEagerSingleton()
    bind(classOf[CurrentProfileSrv]).to(classOf[CurrentProfileService]).asEagerSingleton()
    bind(classOf[ReturnsService]).to(classOf[ReturnsServiceImpl]).asEagerSingleton()
    bind(classOf[BankAccountReputationService]).to(classOf[BankAccountReputationServiceImpl]).asEagerSingleton()
    bind(classOf[BankAccountDetailsService]).to(classOf[BankAccountDetailsServiceImpl]).asEagerSingleton()
    bind(classOf[TurnoverEstimatesService]).to(classOf[TurnoverEstimatesServiceImpl]).asEagerSingleton()
  }

  private def bindConnectors(): Unit = {
    bind(classOf[AddressLookupConnector]).to(classOf[AddressLookupConnectorImpl]).asEagerSingleton()
    bind(classOf[PPConnector]).to(classOf[PrePopConnector]).asEagerSingleton()
    bind(classOf[TestRegistrationConnector]).to(classOf[TestVatRegistrationConnector]).asEagerSingleton()
    bind(classOf[BankHolidaysConnector]).annotatedWith(Names.named("fallback")).to(classOf[FallbackBankHolidaysConnector]).asEagerSingleton()
    bind(classOf[BankHolidaysConnector]).to(classOf[WSBankHolidaysConnector]).asEagerSingleton()
    bind(classOf[IVConnector]).to(classOf[IdentityVerificationConnector]).asEagerSingleton()
    bind(classOf[BankAccountReputationConnect]).to(classOf[BankAccountReputationConnector]).asEagerSingleton()
    bind(classOf[CompanyRegistrationConnect]).to(classOf[CompanyRegistrationConnector]).asEagerSingleton()
    bind(classOf[S4LConnect]).to(classOf[S4LConnector]).asEagerSingleton()
    bind(classOf[KeystoreConnect]).to(classOf[KeystoreConnector]).asEagerSingleton()
    bind(classOf[RegistrationConnector]).to(classOf[VatRegistrationConnector]).asEagerSingleton()
    bind(classOf[IncorporationInformationConnect]).to(classOf[IncorporationInformationConnector]).asEagerSingleton()
  }

  private def featureSwitches(): Unit = {
    bind(classOf[FeatureManager]).to(classOf[FeatureSwitchManager]).asEagerSingleton()
    bind(classOf[VATRegFeatureSwitches]).to(classOf[VATRegFeatureSwitch]).asEagerSingleton()
  }

  //TODO: Investigate way of making bindings easier to read
//  private def traitOf[A: ClassTag] = new TraitOf[A]
//
//  private class TraitOf[A: ClassTag] {
//    import scala.reflect._
//    def bindsToClassOf[B <: A](implicit tagA: ClassTag[A], tagB: ClassTag[B]): Unit = bind(tagA.runtimeClass).to(tagB.runtimeClass).asEagerSingleton()
//  }
}
