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

import javax.inject.Inject

import org.slf4j.{Logger, LoggerFactory}
import play.api.mvc.Call
import uk.gov.hmrc.crypto.{ApplicationCrypto, CryptoWithKeysFromConfig}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache, ShortLivedHttpCaching}
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector => Auditing}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.config.LoadAuditingConfig
import uk.gov.hmrc.play.frontend.filters.MicroserviceFilterSupport
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.whitelist.AkamaiWhitelistFilter

object FrontendAuditConnector extends Auditing {
  override lazy val auditingConfig = LoadAuditingConfig("auditing")
}

trait Hooks extends HttpHooks with HttpAuditing {
  override lazy val auditConnector: Auditing = FrontendAuditConnector
}

trait WSHttp extends
  HttpGet with WSGet with
  HttpPut with WSPut with
  HttpPatch with WSPatch with
  HttpPost with WSPost with
  HttpDelete with WSDelete with Hooks

class Http @Inject()(config: ServicesConfig) extends WSHttp {
  override def appName = config.getString("appName")
  override val hooks   = Seq(AuditingHook)
}

class FrontendAuthConnector @Inject()(val http: WSHttp, config: ServicesConfig) extends AuthConnector {
  val serviceUrl = config.baseUrl("auth")
}

class VatShortLivedHttpCaching @Inject()(val http: WSHttp, config: ServicesConfig) extends ShortLivedHttpCaching {
  override lazy val defaultSource = config.getString("appName")
  override lazy val baseUri       = config.baseUrl("cachable.short-lived-cache")
  override lazy val domain        = config.getConfString("cachable.short-lived-cache.domain",
    throw new Exception(s"Could not find config 'cachable.short-lived-cache.domain'"))
}

class VatShortLivedCache @Inject()(val shortLiveCache: ShortLivedHttpCaching) extends ShortLivedCache {
  override implicit lazy val crypto: CryptoWithKeysFromConfig = ApplicationCrypto.JsonCrypto
}

class VatSessionCache @Inject()(val http: WSHttp, config: ServicesConfig) extends SessionCache {
  override lazy val defaultSource = config.getString("appName")
  override lazy val baseUri       = config.baseUrl("cachable.session-cache")
  override lazy val domain        = config.getConfString("cachable.session-cache.domain",
    throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))
}

object WhitelistFilter extends AkamaiWhitelistFilter with MicroserviceFilterSupport {
  override def whitelist: Seq[String]   = FrontendAppConfig.whitelist
  override def excludedPaths: Seq[Call] = FrontendAppConfig.whitelistExcluded map(Call("GET", _))
  override def destination: Call        = Call("GET", "https://www.tax.service.gov.uk/outage-register-for-vat")
}
