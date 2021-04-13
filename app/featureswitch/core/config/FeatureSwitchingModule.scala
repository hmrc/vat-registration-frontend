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

package featureswitch.core.config

import javax.inject.Singleton
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import featureswitch.core.models.FeatureSwitch

@Singleton
class FeatureSwitchingModule extends Module with FeatureSwitchRegistry {

  val switches = Seq(
    StubIncorpIdJourney,
    StubPersonalDetailsValidation,
    StubEmailVerification,
    StubIcl,
    StubSoleTraderIdentification,
    StubUpscan,
    StubBars,
    TrafficManagementPredicate,
    UseSoleTraderIdentification,
    UseUpscan,
    SaveAndContinueLater
  )

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[FeatureSwitchRegistry].to(this).eagerly()
    )
  }
}

case object StubIncorpIdJourney extends FeatureSwitch {
  val configName = "feature-switch.stub-incorp-id"
  val displayName = "Stub incorporated entity identification flow"
}

case object StubEmailVerification extends FeatureSwitch {
  val configName = "feature-switch.stub-email-verification"
  val displayName = "Stub email verification flow"
}

case object StubPersonalDetailsValidation extends FeatureSwitch {
  val configName = "feature-switch.stub-personal-details-validation"
  val displayName = "Stub personal details validation flow"
}

case object StubIcl extends FeatureSwitch {
  val configName = "feature-switch.stub-icl"
  val displayName = "Stub ICL flow"
}

case object StubSoleTraderIdentification extends FeatureSwitch {
  val configName = "feature-switch.stub-sole-trader-identification"
  val displayName = "Stub sole trader identification journey"
}

case object StubUpscan extends FeatureSwitch {
  val configName = "feature-switch.stub-upscan"
  val displayName = "Stub Upscan flow"
}

case object StubBars extends FeatureSwitch {
  val configName = "feature-switch.stub-bars"
  val displayName = "Stub Bank Account Reputation"
}

case object TrafficManagementPredicate extends FeatureSwitch {
  val configName = "feature-switch.traffic-management-predicate"
  val displayName = "Enable traffic management check in auth predicate (Must match the \"Use traffic management\" feature switch)"
}

case object UseSoleTraderIdentification extends FeatureSwitch {
  val configName = "feature-switch.use-sole-trader-identification"
  val displayName = "Use sole trader identification journey"
}

case object UseUpscan extends FeatureSwitch {
  val configName = "feature-switch.use-upscan"
  val displayName = "Use Upscan flow"
}

case object SaveAndContinueLater extends FeatureSwitch {
  val configName = "feature-switch.save-and-continue-later"
  val displayName = "Enable Save and Continue Later"
}