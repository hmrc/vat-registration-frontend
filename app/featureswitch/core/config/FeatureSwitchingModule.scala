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

  val switches = Seq(StubIncorpIdJourney, StubPersonalDetailsValidation, StubEmailVerification, TrafficManagementPredicate, StubIcl)

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

case object TrafficManagementPredicate extends FeatureSwitch {
  val configName = "feature-switch.traffic-management-predicate"
  val displayName = "Enable traffic management check in auth predicate (Must match the \"Use traffic management\" feature switch)"
}

case object StubIcl extends FeatureSwitch {
  val configName = "feature-switch.stub-icl"
  val displayName = "Stub ICL flow"
}
