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

import com.typesafe.config.Config
import filters.VATCSRFExceptionsFilter
import net.ceedubs.ficus.Ficus._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Request
import play.api.{Application, Configuration, Play}
import play.twirl.api.Html
import uk.gov.hmrc.play.config.{AppName, ControllerConfig}
import uk.gov.hmrc.play.frontend.bootstrap.DefaultFrontendGlobal
import uk.gov.hmrc.play.frontend.filters.{FrontendAuditFilter, FrontendLoggingFilter, MicroserviceFilterSupport}

object FrontendGlobal extends FrontendGlobal

trait FrontendGlobal extends DefaultFrontendGlobal {
  override val auditConnector       = FrontendAuditConnector
  override val loggingFilter        = LoggingFilter
  override val frontendAuditFilter  = AuditFilter


  override def csrfExceptionsFilter = new VATCSRFExceptionsFilter(FrontendAppConfig.uriWhiteList)

  //Code that needs running at app startup has been moved to app.config.startup, create new class and trait then bind in module

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html =
    views.html.pages.error.restart()

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"microservice.metrics")
}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object LoggingFilter extends FrontendLoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object AuditFilter extends FrontendAuditFilter with AppName with MicroserviceFilterSupport {
  override lazy val maskedFormFields                                    = Seq("password")
  override lazy val applicationPort                                     = None
  override lazy val auditConnector                                      = FrontendAuditConnector
  override def controllerNeedsAuditing(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsAuditing
}

object ProductionFrontendGlobal extends FrontendGlobal {
  override def filters = WhitelistFilter +: super.filters
}
