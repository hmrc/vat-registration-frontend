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

package controllers.registration.applicant

import fixtures.ApplicantDetailsFixtures
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.mocks.MockApplicantDetailsService
import testHelpers.ControllerSpec
import views.html.lead_partner_entity_type
import scala.concurrent.ExecutionContext.Implicits.global

class LeadPartnerEntityControllerSpec extends ControllerSpec
  with FutureAwaits
  with DefaultAwaitTimeout
  with MockApplicantDetailsService
  with ApplicantDetailsFixtures {

  //TO DO To be updated when new API is implemented

  trait Setup {
    val view: lead_partner_entity_type = app.injector.instanceOf[lead_partner_entity_type]
    val controller: LeadPartnerEntityController = new LeadPartnerEntityController(
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockApplicantDetailsService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))

    val fakeRequest = FakeRequest(routes.LeadPartnerEntityController.showLeadPartnerEntityType())
  }

  "showLeadPartnerEntityType" should {
    "return OK" in new Setup {
      callAuthorised(controller.showLeadPartnerEntityType()) {
        status(_) mustBe OK
      }
    }
  }

  //TO DO This will be updated when the new API is implemented

  "submitLeadPartnerEntity" should {
    "return a BAD_REQUEST" in new Setup {
      callAuthorised(controller.submitLeadPartnerEntity) {
        status(_) mustBe BAD_REQUEST //This is only so because the new API is not yet implemented
      }
    }
  }

}
