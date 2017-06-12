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

package controllers.sicAndCompliance

import controllers.sicAndCompliance
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import org.mockito.Matchers.any
import org.mockito.Mockito._

class ComplianceExitControllerSpec extends VatRegSpec with VatRegistrationFixture {

  object ComplianceExitController extends ComplianceExitController(mockS4LService, ds, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  s"GET ${sicAndCompliance.routes.ComplianceExitController.exit()}" should {

    "redirect the user to the first question about financial compliance" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(validSicAndCompliance.pure)
      callAuthorised(ComplianceExitController.exit) {
        result =>
          result redirectsTo s"$contextRoot/business-bank-account"
      }
    }
  }

}
