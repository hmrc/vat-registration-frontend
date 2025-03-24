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

package featuretoggle

object FeatureSwitch {

  val featureSwitches: Seq[FeatureSwitch] = Seq(
    StubIncorpIdJourney,
    StubEmailVerification,
    StubAlf,
    StubIcl,
    StubSoleTraderIdentification,
    StubUpscan,
    StubBars,
    StubPartnershipIdentification,
    StubMinorEntityIdentification,
    VrsNewAttachmentJourney,
    TaxableTurnoverJourney
  )

  def apply(str: String): FeatureSwitch =
    featureSwitches find (_.name == str) match {
      case Some(switch) => switch
      case None => throw new IllegalArgumentException("Invalid feature switch: " + str)
    }

  def get(string: String): Option[FeatureSwitch] = featureSwitches find (_.name == string)

  sealed trait FeatureSwitch {
    val name: String
    val displayText: String
    val hint: Option[String] = None
  }

  case object StubIncorpIdJourney extends FeatureSwitch {
    val name = "feature-switch.stub-incorp-id"
    val displayText = "Stub incorporated entity identification flow"
  }

  case object StubEmailVerification extends FeatureSwitch {
    val name = "feature-switch.stub-email-verification"
    val displayText = "Stub email verification flow"
  }

  case object StubIcl extends FeatureSwitch {
    val name = "feature-switch.stub-icl"
    val displayText = "Stub ICL flow"
  }

  case object StubSoleTraderIdentification extends FeatureSwitch {
    val name = "feature-switch.stub-sole-trader-identification"
    val displayText = "Stub sole trader identification journey"
  }

  case object StubUpscan extends FeatureSwitch {
    val name = "feature-switch.stub-upscan"
    val displayText = "Stub Upscan Calls (Digital attachments)"
  }

  case object StubAlf extends FeatureSwitch {
    val name = "feature-switch.stub-alf"
    val displayText = "Stub Address Lookup Frontend"
  }

  case object StubBars extends FeatureSwitch {
    val name = "feature-switch.stub-bars"
    val displayText = "Stub Bank Account Reputation"
  }

  case object StubPartnershipIdentification extends FeatureSwitch {
    val name = "feature-switch.partnership-identification"
    val displayText = "Stub Partnership Identification"
  }

  case object StubMinorEntityIdentification extends FeatureSwitch {
    val name = "feature-switch.minor-entity-identification"
    val displayText = "Stub Minor Entity Identification"
  }

  case object VrsNewAttachmentJourney extends FeatureSwitch {
    val name = "feature-switch.vRSNewAttachmentJourney"
    val displayText: String = "VRS New Attachment Journey"
  }
  case object TaxableTurnoverJourney extends FeatureSwitch {
    val name = "feature-switch.taxableTurnoverJourney"
    val displayText: String = "Taxable Turnover Journey"
  }

}
