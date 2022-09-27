/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.applicant

import fixtures.ApplicantDetailsFixtures
import models.Entity
import models.api._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.mocks.{MockApplicantDetailsService, MockEntityService}
import testHelpers.ControllerSpec
import views.html.applicant.lead_partner_entity_type

import scala.concurrent.ExecutionContext.Implicits.global

class LeadPartnerEntityControllerSpec extends ControllerSpec
  with FutureAwaits
  with DefaultAwaitTimeout
  with MockApplicantDetailsService
  with MockEntityService
  with ApplicantDetailsFixtures {

  trait Setup {
    val view: lead_partner_entity_type = app.injector.instanceOf[lead_partner_entity_type]
    val controller: LeadPartnerEntityController = new LeadPartnerEntityController(
      mockAuthClientConnector,
      mockSessionService,
      mockApplicantDetailsService,
      mockEntityService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))

    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(routes.LeadPartnerEntityController.showLeadPartnerEntityType)
  }

  "showLeadPartnerEntityType" should {
    "return OK without prepop" in new Setup {
      mockGetEntity(regId, 1)(None)
      callAuthorised(controller.showLeadPartnerEntityType) {
        status(_) mustBe OK
      }
    }

    "return OK with prepop" in new Setup {
      mockGetEntity(regId, 1)(Some(Entity(Some(testSoleTrader), Individual, Some(true), None)))
      callAuthorised(controller.showLeadPartnerEntityType) {
        status(_) mustBe OK
      }
    }
  }

  "submitLeadPartnerEntity" should {
    "return a redirect for a Sole Trader" in new Setup {
      val soleTrader = "Z1"
      val entity: Entity = Entity(None, Individual, Some(true), None)
      mockGetEntity(regId, 1)(None)
      mockUpsertEntity[PartyType](regId, 1, Individual)(entity)

      submitAuthorised(controller.submitLeadPartnerEntity, fakeRequest.withFormUrlEncodedBody("value" -> soleTrader)) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SoleTraderIdentificationController.startPartnerJourney.url)
      }
    }

    "return a redirect for NETP" in new Setup {
      val netp = "NETP"
      val entity: Entity = Entity(None, NETP, Some(true), None)

      mockGetEntity(regId, 1)(None)
      mockUpsertEntity[PartyType](regId, 1, NETP)(entity)

      submitAuthorised(controller.submitLeadPartnerEntity, fakeRequest.withFormUrlEncodedBody("value" -> netp)) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SoleTraderIdentificationController.startPartnerJourney.url)
      }
    }

    "return BAD_REQUEST with Empty data" in new Setup {
      submitAuthorised(controller.submitLeadPartnerEntity, fakeRequest.withFormUrlEncodedBody()) {
        result => status(result) mustBe BAD_REQUEST
      }
    }
  }

}
