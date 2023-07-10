/*
 * Copyright 2023 HM Revenue & Customs
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

package featureswitch.core.config

import config.FrontendAppConfig
import featureswitch.core.models.FeatureSwitch

trait FeatureSwitching {

  val FEATURE_SWITCH_ON = "true"
  val FEATURE_SWITCH_OFF = "false"


  private def getValue(key: String)(implicit appConfig: FrontendAppConfig): String = {
    sys.props.get(key).getOrElse(appConfig.servicesConfig.getConfString(key, FEATURE_SWITCH_OFF))
  }

  private def getValue(featureSwitch: FeatureSwitch)(implicit appConfig: FrontendAppConfig): String = {
    getValue(featureSwitch.configName)
  }
  def isEnabled(featureSwitch: FeatureSwitch)(implicit appConfig: FrontendAppConfig): Boolean = {
    getValue(featureSwitch).toBoolean
  }

  def enable(featureSwitch: FeatureSwitch): Unit =
    sys.props += featureSwitch.configName -> FEATURE_SWITCH_ON

  def disable(featureSwitch: FeatureSwitch): Unit =
    sys.props += featureSwitch.configName -> FEATURE_SWITCH_OFF

}
