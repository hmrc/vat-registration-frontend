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

import featureswitch.core.models.FeatureSwitch
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

import javax.inject.Singleton

@Singleton
class FeatureSwitchingModule extends Module with FeatureSwitchRegistry {

  val switches: Seq[FeatureSwitch] = Seq(
    StubIncorpIdJourney,
    StubEmailVerification,
    StubAlf,
    StubIcl,
    StubSoleTraderIdentification,
    StubUpscan,
    StubBars,
    StubPartnershipIdentification,
    StubMinorEntityIdentification,
    UploadDocuments,
    WelshLanguage
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
  val displayName = "Stub Upscan Calls (Digital attachments)"
}

case object StubAlf extends FeatureSwitch {
  val configName = "feature-switch.stub-alf"
  val displayName = "Stub Address Lookup Frontend"
}

case object StubBars extends FeatureSwitch {
  val configName = "feature-switch.stub-bars"
  val displayName = "Stub Bank Account Reputation"
}

case object StubPartnershipIdentification extends FeatureSwitch {
  val configName = "feature-switch.partnership-identification"
  val displayName = "Stub Partnership Identification"
}

case object StubMinorEntityIdentification extends FeatureSwitch {
  val configName = "feature-switch.minor-entity-identification"
  val displayName = "Stub Minor Entity Identification"
}

case object UploadDocuments extends FeatureSwitch {
  override val configName: String = "feature-switch.upload-documents"
  override val displayName: String = "Enable upload documents attachment method"
}

case object WelshLanguage extends FeatureSwitch {
  override val configName: String = "feature-switch.welsh-language-frontend"
  override val displayName: String = "Enable welsh translation"
}