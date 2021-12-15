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
import models.PartnerEntity
import models.api._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.SessionService.leadPartnerEntityKey
import services.mocks.{MockApplicantDetailsService, MockPartnersService}
import testHelpers.ControllerSpec
import views.html.lead_partner_entity_type

import scala.concurrent.ExecutionContext.Implicits.global

class LeadPartnerEntityControllerSpec extends ControllerSpec
  with FutureAwaits
  with DefaultAwaitTimeout
  with MockApplicantDetailsService
  with MockPartnersService
  with ApplicantDetailsFixtures {

  trait Setup {
    val view: lead_partner_entity_type = app.injector.instanceOf[lead_partner_entity_type]
    val controller: LeadPartnerEntityController = new LeadPartnerEntityController(
      mockAuthClientConnector,
      mockSessionService,
      mockApplicantDetailsService,
      mockPartnersService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))

    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(routes.LeadPartnerEntityController.showLeadPartnerEntityType)
  }

  "showLeadPartnerEntityType" should {
    "return OK without prepop" in new Setup {
      mockGetLeadPartner(regId)(None)
      callAuthorised(controller.showLeadPartnerEntityType) {
        status(_) mustBe OK
      }
    }

    "return OK with prepop" in new Setup {
      mockGetLeadPartner(regId)(Some(PartnerEntity(testSoleTrader, Individual, isLeadPartner = true)))
      callAuthorised(controller.showLeadPartnerEntityType) {
        status(_) mustBe OK
      }
    }
  }

  "submitLeadPartnerEntity" should {
    "return a redirect for a Sole Trader" in new Setup {
      val soleTrader = "Z1"
      mockSessionCache[PartyType](leadPartnerEntityKey, Individual)

      submitAuthorised(controller.submitLeadPartnerEntity, fakeRequest.withFormUrlEncodedBody("value" -> soleTrader)) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SoleTraderIdentificationController.startPartnerJourney.url)
      }
    }

    "return a redirect for NETP" in new Setup {
      val netp = "NETP"
      mockSessionCache[PartyType](leadPartnerEntityKey, NETP)

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
