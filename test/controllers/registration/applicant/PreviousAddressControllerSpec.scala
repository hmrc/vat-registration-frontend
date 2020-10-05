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

package controllers.registration.applicant

import fixtures.ApplicantDetailsFixtures
import mocks.mockservices.MockApplicantDetailsService
import models.api.ScrsAddress
import models.view.{ApplicantDetails, ContactDetailsView, FormerNameView, HomeAddressView, PreviousAddressView}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.Call
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import testHelpers.ControllerSpec
import controllers.registration.applicant.{routes => applicantRoutes}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PreviousAddressControllerSpec extends ControllerSpec
  with FutureAwaits
  with DefaultAwaitTimeout
  with MockApplicantDetailsService
  with ApplicantDetailsFixtures {

  trait Setup {
    val controller: PreviousAddressController = new PreviousAddressController(
      messagesControllerComponents,
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockApplicantDetailsService,
      mockAddressLookupService
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val address = ScrsAddress(line1 = "TestLine1", line2 = "TestLine1", postcode = Some("TE 1ST"))

  val incompleteApplicantDetails = ApplicantDetails(
    homeAddress = Some(HomeAddressView(address.id, Some(address))),
    contactDetails = Some(ContactDetailsView(Some("t@t.tt.co"))),
    formerName = Some(FormerNameView(false, None)),
    formerNameDate = None,
    previousAddress = Some(PreviousAddressView(true, Some(address)))
  )

  "show" should {
    "return OK when there's data" in new Setup {
      mockGetApplicantDetails(currentProfile)(incompleteApplicantDetails)

      callAuthorised(controller.show()) {
        status(_) mustBe OK
      }
    }

    "return OK when there's no data" in new Setup {
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)

      callAuthorised(controller.show()) {
        status(_) mustBe OK
      }
    }
  }

  "submit" should {
    val fakeRequest = FakeRequest(applicantRoutes.PreviousAddressController.show())

    "return BAD_REQUEST with Empty data" in new Setup {
      submitAuthorised(controller.submit(), fakeRequest.withFormUrlEncodedBody()) {
        result => status(result) mustBe BAD_REQUEST
      }
    }

    "return SEE_OTHER with Yes selected" in new Setup {
      mockSaveApplicantDetails(PreviousAddressView(true, None))(emptyApplicantDetails)

      submitAuthorised(controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "previousAddressQuestionRadio" -> "true"
      )) { res =>
        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some(controllers.routes.CaptureEmailAddressController.show().url)
      }
    }

    "redirect the user to TxM address capture page with No selected" in new Setup {
      when(mockAddressLookupService.getJourneyUrl(any(), any())(any(), any()))
        .thenReturn(Future.successful(Call("GET", "TxM")))

      submitAuthorised(controller.submit(),
        fakeRequest.withFormUrlEncodedBody("previousAddressQuestionRadio" -> "false")
      ) { res =>
        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some("TxM")
      }
    }
  }

  "addressLookupCallback" should {
    "save an address and redirect to next page" in new Setup {
      mockSaveApplicantDetails(PreviousAddressView(false, Some(address)))(emptyApplicantDetails)
      when(mockAddressLookupService.getAddressById(any())(any()))
        .thenReturn(Future.successful(address))

      callAuthorised(controller.addressLookupCallback("addressId")) { res =>
        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some(controllers.routes.CaptureEmailAddressController.show().url)
      }
    }
  }

  "change" should {
    "save an address and redirect to next page" in new Setup {
      when(mockAddressLookupService.getJourneyUrl(any(), any())(any(), any()))
        .thenReturn(Future.successful(Call("GET", "TxM")))

      callAuthorised(controller.change()) { res =>
        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some("TxM")
      }
    }
  }

}
