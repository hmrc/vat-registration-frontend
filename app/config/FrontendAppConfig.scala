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
import javax.inject.{Inject, Singleton}
import models.external.{CoHoRegisteredOfficeAddress, OfficerList}
import play.api.Mode.Mode
import play.api.{Configuration, Mode}
import play.api.libs.json.{JsValue, Json, Reads}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig


trait AppConfig {
  val analyticsToken: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val timeoutInSeconds: String
  val contactFrontendPartialBaseUrl: String
  val defaultCompanyName: JsValue
  val defaultCohoROA: CoHoRegisteredOfficeAddress
  val defaultOfficerList: OfficerList
  val whitelistedPreIncorpRegIds: Seq[String]
  val whitelistedPostIncorpRegIds: Seq[String]
  lazy val whitelistedRegIds: Seq[String] = whitelistedPostIncorpRegIds ++ whitelistedPreIncorpRegIds
}

@Singleton
class FrontendAppConfig @Inject()(val servicesConfig: ServicesConfig, runModeConfiguration: Configuration) extends AppConfig {

  private def loadConfig(key: String) = servicesConfig.getString(key)
  lazy val host: String = loadConfig("microservice.services.vat-registration-frontend.www.url")
  lazy val backendHost: String = loadConfig("microservice.services.vat-registration.www.url")

  val contactFormServiceIdentifier = "SCRS"

  lazy val contactFrontendPartialBaseUrl = loadConfig("microservice.services.contact-frontend.url")
  lazy val analyticsToken                = loadConfig(s"google-analytics.token")
  lazy val analyticsHost                 = loadConfig(s"google-analytics.host")
  lazy val reportAProblemPartialUrl      = s"$contactFrontendPartialBaseUrl/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl        = s"$contactFrontendPartialBaseUrl/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  val timeoutInSeconds = loadConfig("timeoutInSeconds")

  lazy val companyAuthHost = servicesConfig.getString("microservice.services.auth.company-auth.url")
  lazy val loginCallback   = servicesConfig.getString("microservice.services.auth.login-callback.url")
  lazy val loginPath       = servicesConfig.getString("microservice.services.auth.login_path")
  lazy val feedbackUrl = loadConfig(s"microservice.services.vat-registration-frontend.feedbackUrl")

  val loginUrl                                 = s"$companyAuthHost$loginPath"
  val continueUrl                              = s"$loginCallback${routes.SignInOutController.postSignIn()}"
  final lazy val defaultOrigin: String = {
    lazy val appName = runModeConfiguration.getString("appName").getOrElse("undefined")
    runModeConfiguration.getString("sosOrigin").getOrElse(appName)
  }

  private def whitelistConfig(key: String): Seq[String] = {
    Some(new String(Base64.getDecoder.decode(servicesConfig.getString(key)), "UTF-8"))
      .map(_.split(",")).getOrElse(Array.empty).toSeq
  }

  private def loadStringConfigBase64(key : String) : String = {
    new String(Base64.getDecoder.decode(servicesConfig.getString(key)), Charset.forName("UTF-8"))
  }

  private def loadJsonConfigBase64[T](key: String)(implicit reads: Reads[T]): T = {
    val json = Json.parse(Base64.getDecoder.decode(runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))))
    json.validate[T].fold(
      errors => throw new Exception(s"Incorrect data for the key: $key and ##  $errors"),
      valid  => valid
    )
  }

  lazy val whitelist          = whitelistConfig("whitelist")
  lazy val whitelistExcluded  = whitelistConfig("whitelist-excluded")

  lazy val uriWhiteList     = runModeConfiguration.getStringSeq("csrfexceptions.whitelist").getOrElse(Seq.empty).toSet
  lazy val csrfBypassValue  = loadStringConfigBase64("Csrf-Bypass-value")

  // Defaulted Values for default regId
  lazy val defaultCompanyName :JsValue                 = loadJsonConfigBase64[JsValue]("default-company-name")
  lazy val defaultCohoROA: CoHoRegisteredOfficeAddress = loadJsonConfigBase64[CoHoRegisteredOfficeAddress]("default-coho-registered-office-address")
  lazy val defaultOfficerList: OfficerList             = loadJsonConfigBase64[OfficerList]("default-officer-list")(OfficerList.reads)

  lazy val whitelistedPreIncorpRegIds:Seq[String]  = whitelistConfig("regIdPreIncorpWhitelist")
  lazy val whitelistedPostIncorpRegIds:Seq[String] = whitelistConfig("regIdPostIncorpWhitelist")

  lazy val noneOnsSicCodes = new String(
      Base64.getDecoder.decode(servicesConfig.getString("noneOnsSicCodes")), Charset.forName("UTF-8")
    ).split(",").toSet

  //Footer Links
  lazy val cookies: String = host + servicesConfig.getString("urls.footer.cookies")
  lazy val privacy: String = host + servicesConfig.getString("urls.footer.privacy")
  lazy val termsConditions: String = host + servicesConfig.getString("urls.footer.termsConditions")
  lazy val govukHelp: String = servicesConfig.getString("urls.footer.govukHelp")
}
