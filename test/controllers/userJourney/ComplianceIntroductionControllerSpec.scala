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

package controllers.userJourney

import controllers.userJourney.sicAndCompliance.ComplianceIntroductionController
import helpers.VatRegSpec
import play.api.test.Helpers._

class ComplianceIntroductionControllerSpec extends VatRegSpec {

  object ComplianceIntroductionController extends ComplianceIntroductionController(mockS4LService, ds) {
    override val authConnector = mockAuthConnector
  }

  s"GET ${sicAndCompliance.routes.ComplianceIntroductionController.show()}" should {

    "display the introduction page to a set of compliance questions" in {
      callAuthorised(ComplianceIntroductionController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Tell us more")
      }
    }
  }

  s"POST ${sicAndCompliance.routes.ComplianceIntroductionController.submit()}" should {

    "redirect the user to the next page in the flow" in {
      callAuthorised(ComplianceIntroductionController.submit, mockAuthConnector) {
        result =>
          status(result) mustBe SEE_OTHER
      }
    }
  }

}
