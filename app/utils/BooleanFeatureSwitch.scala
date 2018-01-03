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

package utils

import javax.inject.Inject

sealed trait FeatureSwitch {
  def name: String
  def enabled: Boolean
}

case class BooleanFeatureSwitch(name: String, enabled: Boolean) extends FeatureSwitch

class FeatureSwitchManager extends FeatureManager

trait FeatureManager {

  private[utils] def systemPropertyName(name: String) = s"feature.$name"

  private[utils] def getProperty(name: String): FeatureSwitch = {
    val value = sys.props.get(systemPropertyName(name))

    value match {
      case Some("true") => BooleanFeatureSwitch(name, enabled = true)
      case _ => BooleanFeatureSwitch(name, enabled = false)
    }
  }

  private[utils] def setProperty(name: String, value: String): FeatureSwitch = {
    sys.props += ((systemPropertyName(name), value))
    getProperty(name)
  }

  def enable(fs: FeatureSwitch): FeatureSwitch  = setProperty(fs.name, "true")
  def disable(fs: FeatureSwitch): FeatureSwitch = setProperty(fs.name, "false")
}

class VATRegFeatureSwitch @Inject()(injManager: FeatureSwitchManager) extends VATRegFeatureSwitches {
  override val vatEligibilityUrl: String  = "disableEligibilityFrontend"
  override val ivStubbed: String          = "ivStubbed"
  override val manager: FeatureManager    = injManager
}

trait VATRegFeatureSwitches {

  val vatEligibilityUrl: String
  val ivStubbed: String
  val manager: FeatureManager

  def disableEligibilityFrontend: FeatureSwitch = manager.getProperty(vatEligibilityUrl)
  def useIvStub: FeatureSwitch                  = manager.getProperty(ivStubbed)

  def apply(name: String): Option[FeatureSwitch] = name match {
    case `vatEligibilityUrl`  => Some(disableEligibilityFrontend)
    case `ivStubbed`          => Some(useIvStub)
    case _                    => None
  }
}
