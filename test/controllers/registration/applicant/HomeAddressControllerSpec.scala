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

package controllers.registration.applicant

import common.enums.AddressLookupJourneyIdentifier.{applicantAddress, homeAddress}
import controllers.registration.applicant.{routes => applicantRoutes}
import fixtures.ApplicantDetailsFixtures
import models.{ApplicantDetails, TelephoneNumber}
import models.api.Address
import models.external.{EmailAddress, EmailVerified, Name}
import models.view.HomeAddressView
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.Call
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import services.mocks.MockApplicantDetailsService
import testHelpers.ControllerSpec
import org.mockito.ArgumentMatchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HomeAddressControllerSpec extends ControllerSpec
  with FutureAwaits
  with DefaultAwaitTimeout
  with MockApplicantDetailsService
  with ApplicantDetailsFixtures {

  trait Setup {
    val controller: HomeAddressController = new HomeAddressController(
      mockAuthClientConnector,
      mockSessionService,
      mockApplicantDetailsService,
      mockAddressLookupService,
      mockVatRegistrationService
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val address = Address(line1 = "TestLine1", line2 = Some("TestLine1"), postcode = Some("TE 1ST"), addressValidated = true)

  val partialIncompleteApplicantDetails = ApplicantDetails(
    homeAddress = Some(HomeAddressView(address.id, Some(address))),
    emailAddress = Some(EmailAddress("test@t.test")),
    emailVerified = Some(EmailVerified(true)),
    telephoneNumber = Some(TelephoneNumber("1234")),
    hasFormerName = Some(false),
    formerName = Some(Name(Some("Old"), last = "Name")),
    formerNameDate = None,
    previousAddress = None
  )

  "redirectToAlf" when {
    "non transactor journey" should {
      "redirect to ALF with default journeyId" in new Setup {
        mockGetApplicantDetails(currentProfile)(partialIncompleteApplicantDetails)
        when(mockAddressLookupService.getJourneyUrl(ArgumentMatchers.eq(homeAddress), any())(any()))
          .thenReturn(Future.successful(Call("GET", "TxM")))
        when(mockVatRegistrationService.isTransactor(any(), any())).thenReturn(Future.successful(false))

        callAuthorised(controller.redirectToAlf) { res =>
          status(res) mustBe SEE_OTHER
        }
      }
    }
    "transactor journey" should {
      "redirect to ALF with applicant journeyId" in new Setup {
        mockGetApplicantDetails(currentProfile)(partialIncompleteApplicantDetails)
        when(mockAddressLookupService.getJourneyUrl(ArgumentMatchers.eq(applicantAddress), any())(any()))
          .thenReturn(Future.successful(Call("GET", "TxM")))
        when(mockVatRegistrationService.isTransactor(any(), any())).thenReturn(Future.successful(true))

        callAuthorised(controller.redirectToAlf) { res =>
          status(res) mustBe SEE_OTHER
        }
      }
    }
  }

  "addressLookupCallback" should {
    "save an address and redirect to next page" in new Setup {
      mockSaveApplicantDetails(HomeAddressView(address.id, Some(address)))(emptyApplicantDetails)
      when(mockAddressLookupService.getAddressById(any())(any()))
        .thenReturn(Future.successful(address))

      callAuthorised(controller.addressLookupCallback("addressId")) { res =>
        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some(applicantRoutes.PreviousAddressController.show.url)
      }
    }
  }

}
