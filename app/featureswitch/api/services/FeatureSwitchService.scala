/*
 * Copyright 2022 HM Revenue & Customs
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

package featureswitch.api.services

import featureswitch.core.config.{FeatureSwitchRegistry, FeatureSwitching}
import featureswitch.core.models.FeatureSwitchSetting

import javax.inject.{Inject, Singleton}

@Singleton
class FeatureSwitchService @Inject()(featureSwitchRegistry: FeatureSwitchRegistry) extends FeatureSwitching {

  def getFeatureSwitches: Seq[FeatureSwitchSetting] =
    featureSwitchRegistry.switches.map(
      switch =>
        FeatureSwitchSetting(
          switch.configName,
          switch.displayName,
          isEnabled(switch)
        )
    )

  def updateFeatureSwitches(updatedFeatureSwitches: Seq[FeatureSwitchSetting]): Seq[FeatureSwitchSetting] = {
    updatedFeatureSwitches.foreach(
      featureSwitchSetting =>
        featureSwitchRegistry.get(featureSwitchSetting.configName) match {
          case Some(featureSwitch) =>
            if (featureSwitchSetting.isEnabled) enable(featureSwitch) else disable(featureSwitch)
        }
    )

    getFeatureSwitches
  }
}