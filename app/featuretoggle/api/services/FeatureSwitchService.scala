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

package featuretoggle.api.services

import config.FrontendAppConfig
import featuretoggle.{FeatureSwitch, FeatureToggleSupport}
import featuretoggle.core.models.FeatureSwitchSetting

import javax.inject.{Inject, Singleton}

@Singleton
class FeatureSwitchService @Inject()()(implicit appConfig: FrontendAppConfig) extends FeatureToggleSupport {

  def getFeatureSwitches: Seq[FeatureSwitchSetting] =
    FeatureSwitch.featureSwitches.map(
      switch =>
        FeatureSwitchSetting(
          switch.name,
          switch.displayText,
          isEnabled(switch)
        )
    )

  def updateFeatureSwitches(updatedFeatureSwitches: Seq[FeatureSwitchSetting]): Seq[FeatureSwitchSetting] = {
    updatedFeatureSwitches.foreach(
      featureSwitchSetting =>
        FeatureSwitch.get(featureSwitchSetting.name).collect {
          case featureSwitch => if (featureSwitchSetting.isEnabled) enable(featureSwitch) else disable(featureSwitch)
        }
    )

    getFeatureSwitches
  }
}