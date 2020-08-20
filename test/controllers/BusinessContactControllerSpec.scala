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

package controllers

import java.util.NoSuchElementException

import fixtures.VatRegistrationFixture
import models.CompanyContactDetails
import models.api.ScrsAddress
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import play.api.mvc.Call
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}

import scala.concurrent.Future

class BusinessContactControllerSpec extends ControllerSpec with VatRegistrationFixture with FutureAssertions {

  class Setup {
    val controller: BusinessContactDetailsController = new BusinessContactDetailsController(
      messagesControllerComponents,
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockBusinessContactService,
      mockPrePopulationService,
      mockAddressLookupService
    ) {
      override lazy val dropoutUrl: String = "test otrs URL"
    }

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))

    def mockGetPpopAddressList: OngoingStubbing[Future[Seq[ScrsAddress]]] = when(mockPrePopulationService.getPpobAddressList(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(Seq(scrsAddress)))
  }

  class SubmissionSetup extends Setup {
    when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(validBusinessContactDetails))

    when(mockPrePopulationService.getPpobAddressList(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(Seq(scrsAddress)))
  }

  "showing the ppob page" should {
    "return a 200" when {
      "everything is okay" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails))

        when(mockPrePopulationService.getPpobAddressList(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(Seq(scrsAddress)))

        callAuthorised(controller.showPPOB) {
          _ isA 200
        }
      }
      "everything is okay and no address exists in prepop" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails))

        when(mockPrePopulationService.getPpobAddressList(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(Seq()))

        callAuthorised(controller.showPPOB) {
          _ isA 200
        }
      }
      "everything is okay and no address exists in the business contact" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails.copy(ppobAddress = None)))

        when(mockPrePopulationService.getPpobAddressList(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(Seq(scrsAddress)))

        callAuthorised(controller.showPPOB) {
          _ isA 200
        }
      }
    }
    "throw an exception" when {
      "getBussinesContact Fails" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(throw exception))

        callAuthorised(controller.showPPOB) {
          _ failedWith exception
        }
      }
      "getPpobAddressList Fails" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails.copy(ppobAddress = None)))

        when(mockPrePopulationService.getPpobAddressList(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(throw exception))

        callAuthorised(controller.showPPOB) {
          _ failedWith exception
        }
      }
    }
  }

  "submitting the ppob page" should {
    val fakeRequest = FakeRequest(routes.BusinessContactDetailsController.showPPOB())

    "return a 400" when {
      "form is empty" in new SubmissionSetup {
        submitAuthorised(controller.submitPPOB, fakeRequest.withFormUrlEncodedBody()) {
          _ isA 400
        }
      }
    }

    "return a 303" when {
      "user selects other and redirects to alf address" in new SubmissionSetup {
        when(mockAddressLookupService.getJourneyUrl(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(Call("GET", "my-redirect-url")))
        submitAuthorised(controller.submitPPOB, fakeRequest.withFormUrlEncodedBody("ppobRadio" -> "other")) {
          _ redirectsTo "my-redirect-url"
        }
      }
      "user selects other and redirect to alf address" in new SubmissionSetup {
        when(mockBusinessContactService.updateBusinessContact[ScrsAddress](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(scrsAddress))
        submitAuthorised(controller.submitPPOB, fakeRequest.withFormUrlEncodedBody("ppobRadio" -> scrsAddress.id)) {
          _ redirectsTo routes.BusinessContactDetailsController.showCompanyContactDetails().url
        }
      }

      "user selects non-uk and redirect to drop out page address" in new SubmissionSetup {
        when(mockBusinessContactService.updateBusinessContact[ScrsAddress](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(scrsAddress))
        submitAuthorised(controller.submitPPOB, fakeRequest.withFormUrlEncodedBody("ppobRadio" -> "non-uk")) {
          _ redirectsTo routes.BusinessContactDetailsController.showPPOBDropOut().url
        }
      }

    }

    "return an exception" when {
      "the address selected is not on the list" in new SubmissionSetup {
        submitAuthorised(controller.submitPPOB, fakeRequest.withFormUrlEncodedBody("ppobRadio" -> "fake address")) {
          _ failedWith classOf[NoSuchElementException]
        }
      }
    }
  }

  "show ppob dropout page" should {
    "return a 200" in new SubmissionSetup {
      callAuthorised(controller.showPPOBDropOut) {
        _ isA 200
      }
    }
  }


  "showing the company contact details page" should {
    "return a 200" when {
      "everything is okay" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails))

        callAuthorised(controller.showCompanyContactDetails) {
          _ isA 200
        }
      }

      "everything is okay and no address exists in the business contact" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails.copy(companyContactDetails = None)))

        callAuthorised(controller.showCompanyContactDetails) {
          _ isA 200
        }
      }
    }
    "throw an exception" when {
      "getBussinesContact Fails" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(throw exception))

        callAuthorised(controller.showPPOB) {
          _ failedWith exception
        }
      }
    }
  }

  "submitting the company contact details page" should {
    val fakeRequest = FakeRequest(routes.BusinessContactDetailsController.showCompanyContactDetails())

    "return a 400" when {
      "form is empty" in new SubmissionSetup {
        submitAuthorised(controller.submitCompanyContactDetails, fakeRequest.withFormUrlEncodedBody()) {
          _ isA 400
        }
      }
    }

    "return a 303" when {
      "user selects other and redirect to the business activity discription" in new SubmissionSetup {
        when(mockBusinessContactService.updateBusinessContact[CompanyContactDetails](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails.companyContactDetails.get))
        submitAuthorised(controller.submitCompanyContactDetails, fakeRequest.withFormUrlEncodedBody("email" -> "test@email.com", "mobile" -> "1224456378387")) {
          _ redirectsTo controllers.routes.SicAndComplianceController.showBusinessActivityDescription().url
        }
      }
    }

    "return an exception" when {
      "updateBusinessContact fails" in new SubmissionSetup {
        when(mockBusinessContactService.updateBusinessContact[CompanyContactDetails](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(throw exception))

        submitAuthorised(controller.submitCompanyContactDetails, fakeRequest.withFormUrlEncodedBody("email" -> "test@email.com", "mobile" -> "1224456378387")) {
          _ failedWith exception
        }
      }
    }
  }

  "return from TXM" should {
    "return a 303" when {
      "a valid id is passed" in new Setup {
        when(mockAddressLookupService.getAddressById(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future(scrsAddress))
        when(mockBusinessContactService.updateBusinessContact[ScrsAddress](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(scrsAddress))

        callAuthorised(controller.returnFromTxm(scrsAddress.id)) {
          _ redirectsTo routes.BusinessContactDetailsController.showCompanyContactDetails().url
        }
      }
    }
    "throw an exception" when {
      "getAddressById fails" in new Setup {
        when(mockAddressLookupService.getAddressById(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future(throw exception))

        callAuthorised(controller.returnFromTxm(scrsAddress.id)) {
          _ failedWith exception
        }
      }
      "updateBusinessContact fails" in new Setup {
        when(mockAddressLookupService.getAddressById(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future(scrsAddress))
        when(mockBusinessContactService.updateBusinessContact[ScrsAddress](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future(throw exception))

        callAuthorised(controller.returnFromTxm(scrsAddress.id)) {
          _ failedWith exception
        }
      }
    }
  }
}
