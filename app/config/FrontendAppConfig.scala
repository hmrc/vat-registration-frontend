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

package config

import controllers.callbacks.routes
import featureswitch.core.config._
import models.api._
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.nio.charset.Charset
import java.util.Base64
import javax.inject.{Inject, Singleton}

trait AppConfig {
  val host: String
  val analyticsToken: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val betaFeedbackUrl: String
  val timeout: Int
  val countdown: Int
  val contactFrontendUrl: String
}

// scalastyle:off
@Singleton
class FrontendAppConfig @Inject()(val servicesConfig: ServicesConfig, runModeConfiguration: Configuration) extends AppConfig with FeatureSwitching {

  private def loadConfig(key: String) = servicesConfig.getString(key)

  lazy val hostUrl: String = loadConfig("microservice.services.vat-registration-frontend.www.url")
  lazy val hostAbsoluteUrl: String = loadConfig("microservice.services.vat-registration-frontend.www.absoluteUrl")
  lazy val host: String = servicesConfig.baseUrl("vat-registration-frontend.internal")
  lazy val backendHost: String = servicesConfig.baseUrl("vat-registration")
  lazy val eligibilityHost: String = servicesConfig.baseUrl("vat-registration-eligibility-frontend")
  lazy val eligibilityUrl: String = loadConfig("microservice.services.vat-registration-eligibility-frontend.uri")
  lazy val eligibilityQuestionUrl: String = loadConfig("microservice.services.vat-registration-eligibility-frontend.question")

  lazy val getRegistrationInformationUrl: String = s"$backendHost/vatreg/traffic-management/reg-info"

  def eligibilityStartUrl(regId: String): String = s"$eligibilityUrl/journey/$regId"

  def trafficManagementInfoApiUrl(regId: String): String = s"$backendHost/vatreg/traffic-management/$regId/reg-info"

  def partnersApiUrl(regId: String): String = s"$backendHost/vatreg/$regId/partners"

  def attachmentsApiUrl(regId: String): String = s"$backendHost/vatreg/$regId/attachments"

  def incompleteAttachmentsApiUrl(regId: String): String = s"$backendHost/vatreg/$regId/incomplete-attachments"

  def clearTrafficManagementUrl: String = s"$backendHost/vatreg/traffic-management/reg-info/clear"

  lazy val otrsRoute: String = "https://www.tax.service.gov.uk/business-registration/select-taxes"

  lazy val eligibilityRouteUrl: String = s"$eligibilityUrl/gone-over-threshold"

  def storeNrsPayloadUrl(regId: String): String = s"$backendHost/vatreg/$regId/nrs-payload"

  val contactFormServiceIdentifier = "vrs"

  lazy val feedbackFrontendUrl = loadConfig("microservice.services.feedback-frontend.url")
  lazy val feedbackUrl = s"$feedbackFrontendUrl/feedback/vat-registration"
  lazy val contactFrontendUrl: String = loadConfig("microservice.services.contact-frontend.url")
  lazy val analyticsToken = loadConfig(s"google-analytics.token")
  lazy val analyticsHost = loadConfig(s"google-analytics.host")
  lazy val reportAProblemPartialUrl = s"$contactFrontendUrl/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactFrontendUrl/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUrl = s"$contactFrontendUrl/contact/beta-feedback?service=$contactFormServiceIdentifier"

  lazy val accessibilityStatementPath = loadConfig("accessibility-statement.host")
  lazy val accessibilityStatementUrl = s"$accessibilityStatementPath/accessibility-statement/vat-registration"

  lazy val aasPaymentMethodInfoUrl = "https://www.gov.uk/government/publications/vat-annual-accounting-scheme-direct-debit-form-vat-623"

  val timeout: Int = servicesConfig.getInt("timeout.timeout")
  val countdown: Int = servicesConfig.getInt("timeout.countdown")

  lazy val companyAuthHost = servicesConfig.getString("microservice.services.auth.company-auth.url")

  lazy val loginCallback = servicesConfig.getString("microservice.services.auth.login-callback.url")
  lazy val loginPath = servicesConfig.getString("microservice.services.auth.login_path")

  lazy val loginUrl = s"$companyAuthHost$loginPath"
  lazy val continueUrl = s"$loginCallback${routes.SignInOutController.postSignIn}"

  final lazy val defaultOrigin: String = {
    lazy val appName = runModeConfiguration.getOptional[String]("appName").getOrElse("undefined")
    runModeConfiguration.getOptional[String]("sosOrigin").getOrElse(appName)
  }

  private def loadStringConfigBase64(key: String): String = {
    new String(Base64.getDecoder.decode(servicesConfig.getString(key)), Charset.forName("UTF-8"))
  }

  lazy val csrfBypassValue = loadStringConfigBase64("Csrf-Bypass-value")

  // Bank holidays
  lazy val bankHolidaysUrl = servicesConfig.getString("microservice.services.bank-holidays.url")

  // ALF
  lazy val addressLookupFrontendUrl: String = servicesConfig.baseUrl("address-lookup-frontend")

  def addressLookupJourneyUrl: String =
    if (isEnabled(StubAlf)) s"$host/register-for-vat/test-only/address-lookup/init"
    else s"$addressLookupFrontendUrl/api/v2/init"

  def addressLookupRetrievalUrl(id: String): String =
    if (isEnabled(StubAlf)) s"$host/register-for-vat/test-only/address-lookup/confirmed?id=$id"
    else s"$addressLookupFrontendUrl/api/v2/confirmed?id=$id"

  // Bank Account Reputation Section
  lazy val bankAccountReputationHost = servicesConfig.baseUrl("bank-account-reputation")

  def validateBankDetailsUrl: String =
    if (isEnabled(StubBars)) s"$host/register-for-vat/test-only/bars/validate-bank-details"
    else s"$bankAccountReputationHost/v2/validateBankDetails"

  // Incorporated Entity Identification Section

  lazy val incorpIdHost: String = servicesConfig.baseUrl("incorporated-entity-identification-frontend")

  def startUkCompanyIncorpJourneyUrl(): String =
    if (isEnabled(StubIncorpIdJourney)) {
      s"$host/register-for-vat/test-only/api/incorp-id-journey?partyType=${PartyType.stati(UkCompany)}"
    } else {
      s"$incorpIdHost/incorporated-entity-identification/api/limited-company-journey"
    }

  def startRegSocietyIncorpIdJourneyUrl(): String =
    if (isEnabled(StubIncorpIdJourney)) {
      s"$host/register-for-vat/test-only/api/incorp-id-journey?partyType=${PartyType.stati(RegSociety)}"
    } else {
      s"$incorpIdHost/incorporated-entity-identification/api/registered-society-journey"
    }

  def startCharitableOrgIncorpIdJourneyUrl(): String =
    if (isEnabled(StubIncorpIdJourney)) {
      s"$host/register-for-vat/test-only/api/incorp-id-journey?partyType=${PartyType.stati(CharitableOrg)}"
    } else {
      s"$incorpIdHost/incorporated-entity-identification/api/charitable-incorporated-organisation-journey"
    }

  def getIncorpIdDetailsUrl(journeyId: String): String =
    if (isEnabled(StubIncorpIdJourney)) {
      s"$host/register-for-vat/test-only/api/incorp-id-journey/$journeyId"
    } else {
      s"$incorpIdHost/incorporated-entity-identification/api/journey/$journeyId"
    }

  def incorpIdCallbackUrl: String = s"$hostUrl/register-for-vat/incorp-id-callback"

  def incorpIdPartnerCallbackUrl: String = s"$hostUrl/register-for-vat/incorp-id-partner-callback"

  // Personal Details Validation Section

  lazy val personalDetailsValidationHost: String = servicesConfig.baseUrl("personal-details-validation")
  lazy val personalDetailsValidationFrontendUrl: String = loadConfig("microservice.services.personal-details-validation-frontend.url")

  def getRetrievePersonalDetailsValidationResultUrl(validationId: String): String =
    if (isEnabled(StubPersonalDetailsValidation)) {
      s"$host/register-for-vat/test-only/personal-details-validation/$validationId"
    } else {
      s"$personalDetailsValidationHost/personal-details-validation/$validationId"
    }

  def getPersonalDetailsValidationJourneyUrl(): String =
    if (isEnabled(StubPersonalDetailsValidation)) {
      controllers.applicant.routes.PersonalDetailsValidationController.personalDetailsValidationCallback("testValidationId").url
    } else {
      s"$personalDetailsValidationFrontendUrl/personal-details-validation/start"
    }

  def getPersonalDetailsCallbackUrl(): String =
    s"$hostUrl/register-for-vat/personal-details-validation-callback"

  // Sole Trader Identification Section

  lazy val soleTraderIdentificationFrontendHost: String = servicesConfig.baseUrl("sole-trader-identification-frontend")

  def soleTraderJourneyUrl(partyType: PartyType): String =
    if (isEnabled(StubSoleTraderIdentification)) {
      s"$host/register-for-vat/test-only/sole-trader-identification?partyType=${PartyType.stati(partyType)}"
    } else {
      s"$soleTraderIdentificationFrontendHost/sole-trader-identification/api/sole-trader-journey"
    }

  def individualJourneyUrl(partyType: Option[PartyType]): String =
    if (isEnabled(StubSoleTraderIdentification)) {
      s"$host/register-for-vat/test-only/sole-trader-identification-individual${partyType.fold("")(pt => s"?partyType=${PartyType.stati(pt)}")}"
    } else {
      s"$soleTraderIdentificationFrontendHost/sole-trader-identification/api/individual-journey"
    }

  def retrieveSoleTraderIdentificationResultUrl(journeyId: String): String =
    if (isEnabled(StubSoleTraderIdentification)) {
      s"$host/register-for-vat/test-only/sole-trader-identification/$journeyId"
    } else {
      s"$soleTraderIdentificationFrontendHost/sole-trader-identification/api/journey/$journeyId"
    }

  def soleTraderCallbackUrl: String = s"$hostUrl/register-for-vat/sti-callback"

  def individualCallbackUrl: String = s"$hostUrl/register-for-vat/sti-individual-callback"

  def transactorCallbackUrl: String = s"$hostUrl/register-for-vat/sti-transactor-callback"

  def leadPartnerCallbackUrl: String = s"$hostUrl/register-for-vat/sti-partner-callback"

  // Partnership Identification Section

  lazy val partnershipIdHost: String = servicesConfig.baseUrl("partnership-identification-frontend")

  def startPartnershipJourneyUrl(partyType: PartyType): String = {
    if (isEnabled(StubPartnershipIdentification)) {
      s"$host/register-for-vat/test-only/partnership-identification?partyType=${PartyType.stati(partyType)}"
    } else {
      val url = partyType match {
        case Partnership => "general-partnership-journey"
        case LtdPartnership => "limited-partnership-journey"
        case ScotPartnership => "scottish-partnership-journey"
        case ScotLtdPartnership => "scottish-limited-partnership-journey"
        case LtdLiabilityPartnership => "limited-liability-partnership-journey"
      }
      s"$partnershipIdHost/partnership-identification/api/$url"
    }
  }

  def getPartnershipIdDetailsUrl(journeyId: String): String =
    if (isEnabled(StubPartnershipIdentification)) {
      s"$host/register-for-vat/test-only/partnership-identification/$journeyId"
    } else {
      s"$partnershipIdHost/partnership-identification/api/journey/$journeyId"
    }

  def partnershipIdCallbackUrl: String = s"$hostUrl/register-for-vat/partnership-id-callback"

  def partnershipIdPartnerCallbackUrl: String = s"$hostUrl/register-for-vat/partnership-id-partner-callback"

  // Minor Entity Identification Section

  lazy val minorEntityIdHost: String = servicesConfig.baseUrl("minor-entity-identification-frontend")

  def startUnincorpAssocJourneyUrl: String =
    if (isEnabled(StubMinorEntityIdentification)) {
      s"$host/register-for-vat/test-only/minor-entity-identification?partyType=${PartyType.stati(UnincorpAssoc)}"
    } else {
      s"$minorEntityIdHost/minor-entity-identification/api/unincorporated-association-journey"
    }

  def startTrustJourneyUrl: String =
    if (isEnabled(StubMinorEntityIdentification)) {
      s"$host/register-for-vat/test-only/minor-entity-identification?partyType=${PartyType.stati(Trust)}"
    } else {
      s"$minorEntityIdHost/minor-entity-identification/api/trusts-journey"
    }

  def startNonUKCompanyJourneyUrl: String =
    if (isEnabled(StubMinorEntityIdentification)) {
      s"$host/register-for-vat/test-only/minor-entity-identification?partyType=${PartyType.stati(NonUkNonEstablished)}"
    } else {
      s"$minorEntityIdHost/minor-entity-identification/api/overseas-company-journey"
    }

  def getMinorEntityIdDetailsUrl(journeyId: String): String =
    if (isEnabled(StubMinorEntityIdentification)) {
      s"$host/register-for-vat/test-only/minor-entity-identification/$journeyId"
    } else {
      s"$minorEntityIdHost/minor-entity-identification/api/journey/$journeyId"
    }

  def minorEntityIdCallbackUrl: String = s"$hostUrl/register-for-vat/minor-entity-id-callback"

  // Email Verification Section

  lazy val emailVerificationBaseUrl: String = servicesConfig.baseUrl("email-verification")

  def requestEmailVerificationPasscodeUrl(): String =
    if (isEnabled(StubEmailVerification)) s"$host/register-for-vat/test-only/api/request-passcode"
    else s"$emailVerificationBaseUrl/email-verification/request-passcode"

  def verifyEmailVerificationPasscodeUrl(): String =
    if (isEnabled(StubEmailVerification)) s"$host/register-for-vat/test-only/api/verify-passcode"
    else s"$emailVerificationBaseUrl/email-verification/verify-passcode"

  // Upscan Section

  lazy val upscanInitiateHost: String = servicesConfig.baseUrl("upscan-initiate")

  def setupUpscanJourneyUrl: String =
    if (isEnabled(StubUpscan)) s"$host/register-for-vat/test-only/upscan/initiate"
    else s"$upscanInitiateHost/upscan/v2/initiate"

  def storeUpscanReferenceUrl(regId: String): String = s"$backendHost/vatreg/$regId/upscan-reference"

  def storeUpscanCallbackUrl: String = s"$backendHost/vatreg/upscan-callback"

  def fetchUpscanFileDetails(regId: String, reference: String): String = s"$backendHost/vatreg/$regId/upscan-file-details/$reference"

  def fetchAllUpscanDetails(regId: String): String = s"$backendHost/vatreg/$regId/upscan-file-details"

  def deleteUpscanDetails(regId: String, reference: String): String = s"$backendHost/vatreg/$regId/upscan-file-details/$reference"

  def deleteAllUpscanDetails(regId: String): String = s"$backendHost/vatreg/$regId/upscan-file-details"

  lazy val privacyNoticeUrl = "https://www.gov.uk/government/publications/data-protection-act-dpa-information-hm-revenue-and-customs-hold-about-you/data-protection-act-dpa-information-hm-revenue-and-customs-hold-about-you"

  lazy val vatRatesUrl = "https://www.gov.uk/guidance/rates-of-vat-on-different-goods-and-services"
  lazy val vatAasUrl = "https://www.gov.uk/vat-annual-accounting-scheme"

  lazy val govukVat: String = servicesConfig.getString("urls.govukVat")
  lazy val govukMtd: String = servicesConfig.getString("urls.govukMtd")
  lazy val govukSoftware: String = servicesConfig.getString("urls.govukSoftware")

  lazy val businessDescriptionMaxLength = servicesConfig.getInt("constants.businessDescriptionMaxLength")

  lazy val findOutAboutEoriUrl = servicesConfig.getString("urls.findOutAboutEori")

  def individualKickoutUrl(continueUrl: String): String = s"https://www.tax.service.gov.uk/government-gateway-registration-frontend?accountType=organisation&continue=$continueUrl&origin=unknown"

  lazy val businessSignInLink = "https://www.gov.uk/guidance/sign-in-to-your-hmrc-business-tax-account"
  lazy val fhddsRegisteredBusinessesListUrl = "https://www.gov.uk/government/publications/fulfilment-house-due-diligence-scheme-registered-businesses-list"
  lazy val regime: String = "VATC"
  lazy val vat2Link: String = "https://www.gov.uk/government/publications/vat-partnership-details-vat2"
  lazy val vat51Link: String = "https://www.gov.uk/government/publications/apply-for-vat-group-registration-or-amend-your-details"
  lazy val vat5LLink: String = "https://www.gov.uk/government/publications/vat-vat-registration-land-and-property-vat-5l"
  lazy val landAndPropertyGuidance: String = "https://www.gov.uk/guidance/vat-on-land-and-property-notice-742"
  lazy val vat1Link = "https://www.gov.uk/guidance/register-for-vat"
  lazy val utrCopyLink = "https://www.tax.service.gov.uk/ask-for-copy-of-your-corporation-tax-utr"
  lazy val upscanTimeout = 20000
  lazy val upscanInterval = 2000
  lazy val VATRateDifferentGoodsURL = "https://www.gov.uk/guidance/rates-of-vat-on-different-goods-and-services"
  lazy val vat1trLink = "https://www.gov.uk/government/publications/vat-appointment-of-tax-representative-vat1tr"
}
