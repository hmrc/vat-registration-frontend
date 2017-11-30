/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers

import helpers.VatRegSpec
import models.CurrentProfile
import org.mockito.Matchers
import org.mockito.Mockito.when
import play.api.http.Status

import scala.concurrent.Future

class TwirlViewControllerSpec extends VatRegSpec {

  object TestController extends TwirlViewController(
    ds,
    mockVATFeatureSwitch,
    mockAuthConnector,
    mockKeystoreConnector
  )

  val redirectUrl = "http://localhost:9894/check-if-you-can-register-for-vat/national-insurance-number"

  "GET" should {
    "return HTML when user is authorized to access" in {
      val params = List(("use-this-service", "Can you use this service?"))

      mockGetCurrentProfile()

      when(mockVATFeatureSwitch.disableEligibilityFrontend).thenReturn(enabledFeatureSwitch)

      forAll(params) {
        case (input, expected) =>
          callAuthorised(TestController.renderViewAuthorised(input)) {
            _ includesText expected
          }
      }
    }

    "redirect to eligibility front end if feature switch is enabled" in {
      val params = List(("use-this-service", "Can you use this service?"))

      mockGetCurrentProfile()

      when(mockVATFeatureSwitch.disableEligibilityFrontend).thenReturn(disabledFeatureSwitch)

      forAll(params) {
        case (input, expected) =>
          callAuthorised(TestController.renderViewAuthorised(input)) {
            _ redirectsTo redirectUrl
          }
      }
    }
  }
}
