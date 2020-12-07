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

package featureswitch.frontend.config

import config.FrontendAppConfig
import featureswitch.frontend.models.FeatureSwitchProvider
import javax.inject.{Inject, Singleton}

@Singleton
class FeatureSwitchProviderConfig @Inject()(config: FrontendAppConfig) {

  lazy val frontendBaseUrl: String = config.host
  lazy val frontendFeatureSwitchUrl = s"$frontendBaseUrl/register-for-vat/test-only/api/feature-switches"
  lazy val frontendFeatureSwitchProvider: FeatureSwitchProvider = FeatureSwitchProvider(
    id = "vat-registration-frontend",
    appName = "Vat Registration Frontend",
    url = frontendFeatureSwitchUrl
  )

  lazy val backendBaseUrl = config.backendHost
  lazy val backendFeatureSwitchUrl = s"$backendBaseUrl/vatreg/test-only/api/feature-switches"
  lazy val backendFeatureSwitchProvider: FeatureSwitchProvider = FeatureSwitchProvider(
    id = "vat-registration",
    appName = "Vat Registration Backend",
    url = backendFeatureSwitchUrl
  )

  lazy val eligibilityFeatureSwitchProvider: FeatureSwitchProvider = FeatureSwitchProvider(
    id = "vat-registration-eligibility-frontend",
    appName = "Vat Registration Eligibility",
    url = s"${config.eligibilityHost}/check-if-you-can-register-for-vat/test-only/api/feature-switches"
  )

  lazy val featureSwitchProviders: Seq[FeatureSwitchProvider] =
    Seq(frontendFeatureSwitchProvider, eligibilityFeatureSwitchProvider, backendFeatureSwitchProvider)
}
