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

package controllers

import featureswitch.core.config.FeatureSwitching
import fixtures.VatRegistrationFixture
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.compliance_introduction

class ComplianceIntroductionControllerSpec extends ControllerSpec with FutureAssertions with VatRegistrationFixture with FeatureSwitching {
  val mockComplianceIntroductionView: compliance_introduction = app.injector.instanceOf[compliance_introduction]

  class Setup {
    val controller = new ComplianceIntroductionController(
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockComplianceIntroductionView
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "saveIclSicCodes" should {
    "redirect and save" when {
      s"GET ${routes.ComplianceIntroductionController.show}" should {
        "display the introduction page to a set of compliance questions" in new Setup {
          callAuthorised(controller.show) {
            status(_) mustBe OK
          }
        }
      }

      s"POST ${routes.ComplianceIntroductionController.submit}" should {
        "redirect the user to the SIC code selection page" in new Setup {
          callAuthorised(controller.submit) {
            result =>
              result redirectsTo controllers.registration.sicandcompliance.routes.SupplyWorkersController.show.url
          }
        }
      }
    }
  }
}