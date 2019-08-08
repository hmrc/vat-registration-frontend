/*
 * Copyright 2019 HM Revenue & Customs
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

import common.enums.VatRegStatus
import models.external.{CoHoRegisteredOfficeAddress, IncorporationInfo, OfficerList}
import play.api.Play.{configuration, current}
import play.api.libs.json.{JsObject, JsValue, Json, Reads}
import uk.gov.hmrc.play.config.ServicesConfig

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

object FrontendAppConfig extends AppConfig with ServicesConfig {

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  val contactFormServiceIdentifier = "SCRS"

  override lazy val contactFrontendPartialBaseUrl = loadConfig("microservice.services.contact-frontend.url")
  override lazy val analyticsToken                = loadConfig(s"google-analytics.token")
  override lazy val analyticsHost                 = loadConfig(s"google-analytics.host")
  override lazy val reportAProblemPartialUrl      = s"$contactFrontendPartialBaseUrl/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl        = s"$contactFrontendPartialBaseUrl/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  override val timeoutInSeconds = loadConfig("timeoutInSeconds")

  private def whitelistConfig(key: String): Seq[String] = {
    Some(new String(Base64.getDecoder.decode(configuration.getString(key).getOrElse("")), "UTF-8"))
      .map(_.split(",")).getOrElse(Array.empty).toSeq
  }

  private def loadStringConfigBase64(key : String) : String = {
    new String(Base64.getDecoder.decode(configuration.getString(key).getOrElse("")), Charset.forName("UTF-8"))
  }

  private def loadJsonConfigBase64[T](key: String)(implicit reads: Reads[T]): T = {
    val json = Json.parse(Base64.getDecoder.decode(configuration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))))
    json.validate[T].fold(
      errors => throw new Exception(s"Incorrect data for the key: $key and ##  $errors"),
      valid  => valid
    )
  }

  lazy val whitelist          = whitelistConfig("whitelist")
  lazy val whitelistExcluded  = whitelistConfig("whitelist-excluded")

  lazy val uriWhiteList     = configuration.getStringSeq("csrfexceptions.whitelist").getOrElse(Seq.empty).toSet
  lazy val csrfBypassValue  = loadStringConfigBase64("Csrf-Bypass-value")

  // Defaulted Values for default regId
  lazy val defaultCompanyName :JsValue                 = loadJsonConfigBase64[JsValue]("default-company-name")
  lazy val defaultCohoROA: CoHoRegisteredOfficeAddress = loadJsonConfigBase64[CoHoRegisteredOfficeAddress]("default-coho-registered-office-address")
  lazy val defaultOfficerList: OfficerList             = loadJsonConfigBase64[OfficerList]("default-officer-list")(OfficerList.reads)

  lazy val whitelistedPreIncorpRegIds:Seq[String]  = whitelistConfig("regIdPreIncorpWhitelist")
  lazy val whitelistedPostIncorpRegIds:Seq[String] = whitelistConfig("regIdPostIncorpWhitelist")

  lazy val noneOnsSicCodes = new String(
      Base64.getDecoder.decode(configuration.getString("noneOnsSicCodes").getOrElse("")), Charset.forName("UTF-8")
    ).split(",").toSet

}
