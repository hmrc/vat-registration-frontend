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

package controllers.test

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, OK}
import helpers.VatRegSpec

class FeatureSwitchControllerSpec extends VatRegSpec {


  class Setup {
    val controller = new FeatureSwitchCtrl {
      val vatRegFeatureSwitch = mockVATFeatureSwitch
      override val featureManager = mockFeatureManager
    }
  }


  "switcher" should {
    "enable the addressService feature switch and return an OK" when {
      "addressService and addressLookUpFrontend are passed in the url" in new Setup{
        when(mockVATFeatureSwitch(ArgumentMatchers.any()))
          .thenReturn(Some(enabledFeatureSwitch))

        when(mockFeatureManager.enable(ArgumentMatchers.any()))
          .thenReturn(enabledFeatureSwitch)

        val result = await(controller.switcher("test","true")(FakeRequest()))
        assertResult(OK)(result.header.status)
      }
    }

    "disable the test service switch and return an OK" when {
      "valid service name and some other featureState is passed into the URL" in new Setup {
        when(mockVATFeatureSwitch(ArgumentMatchers.any()))
          .thenReturn(Some(enabledFeatureSwitch))

        when(mockFeatureManager.disable(ArgumentMatchers.any()))
          .thenReturn(disabledFeatureSwitch)

        val result = await(controller.switcher("test","someOtherState")(FakeRequest()))
        assertResult(OK)(result.header.status)
      }
    }

    "return a bad request" when {
      "an unknown feature is trying to be enabled" in new Setup {
        when(mockVATFeatureSwitch(ArgumentMatchers.any()))
          .thenReturn(None)

        val result = await(controller.switcher("invalidName","invalidState")(FakeRequest()))
        assertResult(BAD_REQUEST)(result.header.status)
      }
    }
  }
}
