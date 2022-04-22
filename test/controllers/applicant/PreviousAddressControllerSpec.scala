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

import common.enums.AddressLookupJourneyIdentifier.{addressThreeYearsOrLess, applicantAddressThreeYearsOrLess}
import controllers.applicant.{routes => applicantRoutes}
import fixtures.ApplicantDetailsFixtures
import models.api.{Address, NETP, UkCompany}
import models.external.{EmailAddress, EmailVerified}
import models.view.{HomeAddressView, PreviousAddressView}
import models.{ApplicantDetails, TelephoneNumber}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.Call
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.mocks.{MockApplicantDetailsService, MockVatRegistrationService}
import testHelpers.ControllerSpec
import views.html.applicant.previous_address

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PreviousAddressControllerSpec extends ControllerSpec
  with FutureAwaits
  with DefaultAwaitTimeout
  with MockApplicantDetailsService
  with ApplicantDetailsFixtures
  with MockVatRegistrationService {

  trait Setup {
    val controller: PreviousAddressController = new PreviousAddressController(
      mockAuthClientConnector,
      mockSessionService,
      mockApplicantDetailsService,
      mockAddressLookupService,
      vatRegistrationServiceMock,
      app.injector.instanceOf[previous_address]
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val address = Address(line1 = "TestLine1", line2 = Some("TestLine1"), postcode = Some("TE 1ST"), addressValidated = true)

  val incompleteApplicantDetails = ApplicantDetails(
    homeAddress = Some(HomeAddressView(address.id, Some(address))),
    emailAddress = Some(EmailAddress("test@t.test")),
    emailVerified = Some(EmailVerified(true)),
    telephoneNumber = Some(TelephoneNumber("1234")),
    hasFormerName = Some(false),
    formerName = None,
    formerNameDate = None,
    previousAddress = Some(PreviousAddressView(true, Some(address)))
  )

  "show" should {
    "return OK when there's data" in new Setup {
      mockGetApplicantDetails(currentProfile)(incompleteApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }
    "return OK when there's no data" in new Setup {
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }

    "return OK when there's data and the user is a transactor" in new Setup {
      mockGetApplicantDetails(currentProfile)(incompleteApplicantDetails)
      mockIsTransactor(Future.successful(true))
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }

    "return OK when there's no data and the user is a transactor" in new Setup {
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockIsTransactor(Future.successful(true))
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }
  }

  "submit" should {
    val fakeRequest = FakeRequest(applicantRoutes.PreviousAddressController.show)

    "return BAD_REQUEST with Empty data" in new Setup {
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))
      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody()) {
        result => status(result) mustBe BAD_REQUEST
      }
    }
    "return SEE_OTHER with Yes selected" in new Setup {
      mockPartyType(Future.successful(UkCompany))
      mockSaveApplicantDetails(PreviousAddressView(true, None))(emptyApplicantDetails)

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody(
        "previousAddressQuestionRadio" -> "true"
      )) { res =>
        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some(routes.CaptureEmailAddressController.show.url)
      }
    }
    "redirect the user to TxM address capture page with No selected" in new Setup {
      mockPartyType(Future.successful(UkCompany))
      when(mockAddressLookupService.getJourneyUrl(any(), any(), ArgumentMatchers.eq(false))(any()))
        .thenReturn(Future.successful(Call("GET", "TxM")))
      when(vatRegistrationServiceMock.isTransactor(any(), any())).thenReturn(Future.successful(false))

      submitAuthorised(controller.submit,
        fakeRequest.withFormUrlEncodedBody("previousAddressQuestionRadio" -> "false")
      ) { res =>
        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some("/register-for-vat/current-address/changePreviousAddress")
      }
    }
    "redirect to International address capture for NETP when No selected" in new Setup {
      mockPartyType(Future.successful(NETP))

      submitAuthorised(controller.submit,
        fakeRequest.withFormUrlEncodedBody("previousAddressQuestionRadio" -> "false")
      ) { res =>
        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some(routes.InternationalPreviousAddressController.show.url)
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
        redirectLocation(res) mustBe Some(routes.CaptureEmailAddressController.show.url)
      }
    }
  }

  "previousAddress" when {
    "non transactor journey" should {
      "redirect to ALF with default journeyId" in new Setup {
        when(mockAddressLookupService.getJourneyUrl(ArgumentMatchers.eq(addressThreeYearsOrLess), any(), ArgumentMatchers.eq(false))(any()))
          .thenReturn(Future.successful(Call("GET", "TxM")))
        when(vatRegistrationServiceMock.isTransactor(any(), any())).thenReturn(Future.successful(false))

        callAuthorised(controller.previousAddress()) { res =>
          status(res) mustBe SEE_OTHER
          redirectLocation(res) mustBe Some("TxM")
        }
      }
    }
    "transactor journey" should {
      "redirect to ALF with applicant journeyId" in new Setup {
        when(mockAddressLookupService.getJourneyUrl(ArgumentMatchers.eq(applicantAddressThreeYearsOrLess), any(), ArgumentMatchers.eq(false))(any()))
          .thenReturn(Future.successful(Call("GET", "TxM")))
        when(vatRegistrationServiceMock.isTransactor(any(), any())).thenReturn(Future.successful(true))

        callAuthorised(controller.previousAddress()) { res =>
          status(res) mustBe SEE_OTHER
          redirectLocation(res) mustBe Some("TxM")
        }
      }
    }
  }

}