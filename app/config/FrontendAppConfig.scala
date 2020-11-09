/*
 * Copyright 2020 HM Revenue & Customs
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

import java.nio.charset.Charset
import java.util.Base64

import controllers.callbacks.routes
import featureswitch.core.config._
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.{JsValue, Json, Reads}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait AppConfig {
  val host: String
  val analyticsToken: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val betaFeedbackUrl: String
  val timeoutInSeconds: String
  val contactFrontendPartialBaseUrl: String
}

@Singleton
class FrontendAppConfig @Inject()(val servicesConfig: ServicesConfig, runModeConfiguration: Configuration) extends AppConfig with FeatureSwitching {

  private def loadConfig(key: String) = servicesConfig.getString(key)

  lazy val hostUrl: String = loadConfig("microservice.services.vat-registration-frontend.www.url")
  lazy val host: String = servicesConfig.baseUrl("vat-registration-frontend.internal")
  lazy val backendHost: String = servicesConfig.baseUrl("vat-registration")
  lazy val incorpIdHost: String = servicesConfig.baseUrl("incorporated-entity-identification-frontend")
  lazy val personalDetailsValidationFrontendUrl: String = loadConfig("microservice.services.personal-details-validation-frontend.url")
  lazy val emailVerificationBaseUrl: String = servicesConfig.baseUrl("email-verification")

  val contactFormServiceIdentifier = "vrs"

  lazy val feedbackFrontendUrl = loadConfig("microservice.services.feedback-frontend.url")
  lazy val feedbackUrl = s"$feedbackFrontendUrl/feedback/vat-registration"
  lazy val contactFrontendPartialBaseUrl = servicesConfig.baseUrl("contact-frontend")
  lazy val analyticsToken = loadConfig(s"google-analytics.token")
  lazy val analyticsHost = loadConfig(s"google-analytics.host")
  lazy val reportAProblemPartialUrl = s"$contactFrontendPartialBaseUrl/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactFrontendPartialBaseUrl/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUrl: String = s"$contactFrontendPartialBaseUrl/contact/beta-feedback?service=$contactFormServiceIdentifier"

  val timeoutInSeconds = loadConfig("timeoutInSeconds")

  lazy val companyAuthHost = servicesConfig.getString("microservice.services.auth.company-auth.url")

  lazy val loginCallback = servicesConfig.getString("microservice.services.auth.login-callback.url")
  lazy val loginPath = servicesConfig.getString("microservice.services.auth.login_path")

  lazy val loginUrl = s"$companyAuthHost$loginPath"
  lazy val continueUrl = s"$loginCallback${routes.SignInOutController.postSignIn()}"

  final lazy val defaultOrigin: String = {
    lazy val appName = runModeConfiguration.getString("appName").getOrElse("undefined")
    runModeConfiguration.getString("sosOrigin").getOrElse(appName)
  }

  private def loadStringConfigBase64(key: String): String = {
    new String(Base64.getDecoder.decode(servicesConfig.getString(key)), Charset.forName("UTF-8"))
  }

  private def loadJsonConfigBase64[T](key: String)(implicit reads: Reads[T]): T = {
    val json = Json.parse(Base64.getDecoder.decode(runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))))
    json.validate[T].fold(
      errors => throw new Exception(s"Incorrect data for the key: $key and ##  $errors"),
      valid => valid
    )
  }

  lazy val csrfBypassValue = loadStringConfigBase64("Csrf-Bypass-value")

  lazy val noneOnsSicCodes = new String(
    Base64.getDecoder.decode(servicesConfig.getString("noneOnsSicCodes")), Charset.forName("UTF-8")
  ).split(",").toSet

  //Footer Links
  lazy val cookies: String = host + servicesConfig.getString("urls.footer.cookies")
  lazy val privacy: String = host + servicesConfig.getString("urls.footer.privacy")
  lazy val termsConditions: String = host + servicesConfig.getString("urls.footer.termsConditions")
  lazy val govukHelp: String = servicesConfig.getString("urls.footer.govukHelp")

  def getCreateIncorpIdJourneyUrl(): String =
    if (isEnabled(StubIncorpIdJourney)) {
      s"$host/register-for-vat/test-only/api/incorp-id-journey"
    } else s"$incorpIdHost/incorporated-entity-identification/api/journey"

  def getIncorpIdDetailsUrl(journeyId: String): String =
    if (isEnabled(StubIncorpIdJourney)) {
      s"$host/register-for-vat/test-only/api/incorp-id-journey/$journeyId"
    } else s"$incorpIdHost/incorporated-entity-identification/api/journey/$journeyId"

  def incorpIdCallbackUrl: String = s"$hostUrl/register-for-vat/incorp-id-callback"

  lazy val personalDetailsValidationHost: String = servicesConfig.baseUrl("personal-details-validation")

  def getRetrievePersonalDetailsValidationResultUrl(validationId: String): String =
    if (isEnabled(StubPersonalDetailsValidation)) {
      s"$host/register-for-vat/test-only/personal-details-validation/$validationId"
    } else {
      s"$personalDetailsValidationHost/personal-details-validation/$validationId"
    }

  def getPersonalDetailsValidationJourneyUrl(): String =
    if (isEnabled(StubPersonalDetailsValidation)) {
      controllers.registration.applicant.routes.PersonalDetailsValidationController.personalDetailsValidationCallback("testValidationId").url
    } else {
      s"$personalDetailsValidationFrontendUrl/personal-details-validation/start"
    }

  def getPersonalDetailsCallbackUrl(): String =
    s"$hostUrl/register-for-vat/personal-details-validation-callback"

  def requestEmailVerificationPasscodeUrl(): String =
    if (isEnabled(StubEmailVerification)) s"$host/register-for-vat/test-only/api/request-passcode"
    else s"$emailVerificationBaseUrl/email-verification/request-passcode"

  def verifyEmailVerificationPasscodeUrl(): String =
    if (isEnabled(StubEmailVerification)) s"$host/register-for-vat/test-only/api/verify-passcode"
    else s"$emailVerificationBaseUrl/email-verification/verify-passcode"

  lazy val privacyNoticeUrl = "https://www.gov.uk/government/publications/data-protection-act-dpa-information-hm-revenue-and-customs-hold-about-you/data-protection-act-dpa-information-hm-revenue-and-customs-hold-about-you"

  lazy val vatRatesUrl = "https://www.gov.uk/guidance/rates-of-vat-on-different-goods-and-services"

  lazy val govukVat: String = servicesConfig.getString("urls.govukVat")
  lazy val govukMtd: String = servicesConfig.getString("urls.govukMtd")
  lazy val govukSoftware: String = servicesConfig.getString("urls.govukSoftware")
}
