/*
 * Copyright 2021 HM Revenue & Customs
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

package auth

import config.FrontendAppConfig
import controllers.callbacks.routes
import javax.inject.Inject
import play.api.Configuration

class VatExternalUrls @Inject()(appConfig: FrontendAppConfig, runModeConfiguration: Configuration) {

  private[VatExternalUrls] val companyAuthHost = appConfig.servicesConfig.getString("auth.company-auth.url")
  private[VatExternalUrls] val loginCallback   = appConfig.servicesConfig.getString("auth.login-callback.url")
  private[VatExternalUrls] val loginPath       = appConfig.servicesConfig.getString("auth.login_path")

  val loginUrl                                 = s"$companyAuthHost$loginPath"
  val continueUrl                              = s"$loginCallback${routes.SignInOutController.postSignIn()}"
  final lazy val defaultOrigin: String = {
    lazy val appName = runModeConfiguration.getOptional[String]("appName").getOrElse("undefined")
    runModeConfiguration.getOptional[String]("sosOrigin").getOrElse(appName)
  }
}
