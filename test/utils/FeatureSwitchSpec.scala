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

package utils

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec

class FeatureSwitchSpec extends PlaySpec with BeforeAndAfterEach {

  override def beforeEach() {
    System.clearProperty("feature.test")
    System.clearProperty("feature.cohoFirstHandOff")
    System.clearProperty("feature.businessActivitiesHandOff")
    System.clearProperty("feature.system-date")
    super.beforeEach()
  }

  val featureSwitch         = new FeatureSwitchManager
  val vatFeatureSwitch      = new VATRegFeatureSwitch(featureSwitch)
  val booleanFeatureSwitch  = BooleanFeatureSwitch("test", false)
  val datetimeFeatureSwitch = ValueSetFeatureSwitch("testDateTime", "2018-01-13T00:18:58")

  "getProperty" should {
    "return a disabled feature switch if the system property is undefined" in {
      featureSwitch.getProperty("test") mustBe BooleanFeatureSwitch("test", enabled = false)
    }

    "return an enabled feature switch if the system property is defined as 'true'" in {
      System.setProperty("feature.test", "true")

      featureSwitch.getProperty("test") mustBe BooleanFeatureSwitch("test", enabled = true)
    }

    "return an enabled feature switch if the system property is defined as 'false'" in {
      System.setProperty("feature.test", "false")

      featureSwitch.getProperty("test") mustBe BooleanFeatureSwitch("test", enabled = false)
    }
  }

  "systemPropertyName" should {
    "append feature. to the supplied string'" in {
      featureSwitch.systemPropertyName("test") mustBe "feature.test"
    }
  }

  "setProperty" should {

    "return a feature switch (testKey, false) when supplied with (testKey, testValue)" in {
      featureSwitch.setProperty("test", "testValue") mustBe BooleanFeatureSwitch("test", enabled = false)
    }

    "return a feature switch (testKey, true) when supplied with (testKey, true)" in {
      featureSwitch.setProperty("test", "true") mustBe BooleanFeatureSwitch("test", enabled = true)
    }

    "return ValueSetFeatureSwitch when supplied system-date and 2018-01-01T01:13:57" in {
      featureSwitch.setProperty("system-date", "2018-01-01T01:13:57") mustBe ValueSetFeatureSwitch("system-date", "2018-01-01T01:13:57")
    }
  }

  "enable" should {
    "set the value for the supplied key to 'true'" in {
      System.setProperty("feature.test", "false")

      featureSwitch.enable(booleanFeatureSwitch) mustBe BooleanFeatureSwitch("test", enabled = true)
    }
  }

  "disable" should {
    "set the value for the supplied key to 'false'" in {
      System.setProperty("feature.test", "true")

      featureSwitch.disable(booleanFeatureSwitch) mustBe BooleanFeatureSwitch("test", enabled = false)
    }
  }

  "dynamic toggling should be supported" in {
    featureSwitch.disable(booleanFeatureSwitch).enabled mustBe false
    featureSwitch.enable(booleanFeatureSwitch).enabled mustBe true
  }

  "VATRegFeatureSwitches" should {
    "return a disabled feature when the associated system property doesn't exist" in {
      vatFeatureSwitch.useIvStub.enabled mustBe false
    }

    "return an enabled feature when the associated system property is true" in {
      featureSwitch.enable(vatFeatureSwitch.useIvStub)

      vatFeatureSwitch.useIvStub.enabled mustBe true
    }

    "return a disable feature when the associated system property is false" in {
      featureSwitch.disable(vatFeatureSwitch.useIvStub)

      vatFeatureSwitch.useIvStub.enabled mustBe false
    }

    "return true if the crStubbed system property is true" in {
      System.setProperty("feature.crStubbed", "true")

      vatFeatureSwitch("crStubbed") mustBe Some(BooleanFeatureSwitch("crStubbed", true))
    }

    "return false if the crStubbed system property is false" in {
      System.setProperty("feature.crStubbed", "false")

      vatFeatureSwitch("crStubbed") mustBe Some(BooleanFeatureSwitch("crStubbed", false))
    }

    "return an empty option if a system property doesn't exist when using the apply function" in {
      vatFeatureSwitch("somethingElse") mustBe None
    }
  }
}
