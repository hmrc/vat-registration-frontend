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

package featuretoggle

import config.FrontendAppConfig
import featuretoggle.ConfigurableValue.ConfigurableValue
import featuretoggle.FeatureSwitch.FeatureSwitch
import utils.LoggingUtil

import scala.sys.SystemProperties

trait FeatureToggleSupport extends LoggingUtil {

  def getValue(key: String)(implicit appConfig: FrontendAppConfig): String = {
    sys.props.get(key).getOrElse(appConfig.servicesConfig.getString(key))
  }

  def getValue(featureSwitch: FeatureSwitch)(implicit appConfig: FrontendAppConfig
  ): String = {
    getValue(featureSwitch.name)
  }

  def isEnabled(featureSwitch: FeatureSwitch)(implicit appConfig: FrontendAppConfig): Boolean = {
    getValue(featureSwitch).toBoolean
  }

  def setValue(key: String, value: String): SystemProperties = {
    sys.props += key -> value
  }

  def setValue(featureSwitch: FeatureSwitch, value: String): SystemProperties = {
    setValue(featureSwitch.name, value)
  }

  def enable(featureSwitch: FeatureSwitch): SystemProperties = {
    logger.debug(s"[FeatureToggleSupport][enable] ${featureSwitch.name} enabled")
    setValue(featureSwitch, true.toString)
  }

  def disable(featureSwitch: FeatureSwitch): SystemProperties = {
    logger.debug(s"[FeatureToggleSupport][disable] ${featureSwitch.name} disabled")
    setValue(featureSwitch, false.toString)
  }

}
object FeatureToggleSupport extends FeatureToggleSupport
