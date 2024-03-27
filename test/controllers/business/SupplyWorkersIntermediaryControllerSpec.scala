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

package controllers.business

import fixtures.VatRegistrationFixture
import models.api.UkCompany
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.mocks.{BusinessServiceMock, MockApplicantDetailsService, MockVatRegistrationService}
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.sicandcompliance.IntermediarySupply

import scala.concurrent.Future

class SupplyWorkersIntermediaryControllerSpec extends ControllerSpec with FutureAwaits with FutureAssertions with DefaultAwaitTimeout
  with VatRegistrationFixture with BusinessServiceMock with MockApplicantDetailsService with MockVatRegistrationService {

  trait Setup {
    val view: IntermediarySupply = app.injector.instanceOf[IntermediarySupply]
    val controller: SupplyWorkersIntermediaryController = new SupplyWorkersIntermediaryController(
      mockAuthClientConnector,
      mockSessionService,
      mockBusinessService,
      mockApplicantDetailsService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))

    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(controllers.business.routes.SupplyWorkersIntermediaryController.show)
  }

  s"GET ${controllers.business.routes.SupplyWorkersIntermediaryController.show}" should {
    "return OK when there's a Temporary Contracts model in backend" in new Setup {
      mockGetBusiness(Future.successful(validBusiness))
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockgetApplicantNameForTransactorFlow(currentProfile)(None)

      callAuthorised(controller.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
      }
    }

    "return OK where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetBusiness(Future.successful(validBusinessWithNoDescriptionAndLabour))
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockgetApplicantNameForTransactorFlow(currentProfile)(None)

      callAuthorised(controller.show) { result =>
        status(result) mustBe OK
      }
    }
  }

  s"POST ${controllers.business.routes.SupplyWorkersIntermediaryController.submit}" should {
    "return BAD_REQUEST with Empty data" in new Setup {
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockgetApplicantNameForTransactorFlow(currentProfile)(None)
      mockPartyType(Future.successful(UkCompany))
      submitAuthorised(controller.submit, fakeRequest.withMethod("POST").withFormUrlEncodedBody(
      )) {
        result => status(result) mustBe BAD_REQUEST
      }
    }

    "redirect to Imports or Exports or OBI with Yes selected for UkCompany" in new Setup {
      mockUpdateBusiness(Future.successful(validBusiness))
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockgetApplicantNameForTransactorFlow(currentProfile)(None)
      mockPartyType(Future.successful(UkCompany))

      submitAuthorised(controller.submit, fakeRequest.withMethod("POST").withFormUrlEncodedBody("value" -> "true")) { response =>
        status(response) mustBe SEE_OTHER
        redirectLocation(response).getOrElse("") mustBe controllers.routes.TaskListController.show.url
      }
    }
  }

}