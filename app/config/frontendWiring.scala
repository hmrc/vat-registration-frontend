/*
 * Copyright 2024 HM Revenue & Customs
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

import org.slf4j.{Logger, LoggerFactory}
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AuthClientConnector @Inject()(val http: HttpClient, config: ServicesConfig) extends PlayAuthConnector {

  override val serviceUrl: String = config.baseUrl("auth")

}

trait Logging {
  val logger: Logger = LoggerFactory.getLogger(getClass)
}
