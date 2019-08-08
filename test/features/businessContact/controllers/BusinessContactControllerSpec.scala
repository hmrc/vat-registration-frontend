/*
 * Copyright 2019 HM Revenue & Customs
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

package features.businessContact.controllers

import java.util.NoSuchElementException

import connectors.KeystoreConnector
import features.businessContact.BusinessContactService
import features.businessContact.models.CompanyContactDetails
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, FutureAssertions, MockMessages}
import mocks.AuthMock
import models.api.ScrsAddress
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import play.api.test.FakeRequest
import services.{AddressLookupService, PrePopService}
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future

class BusinessContactControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages with FutureAssertions {

  class Setup {
    val controller = new BusinessContactDetailsController {
      override val addressLookupService: AddressLookupService = mockAddressService
      override val businessContactService: BusinessContactService = mockBusinessContactService
      override val prepopService: PrePopService = mockPrePopService
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
      val messagesApi: MessagesApi = mockMessagesAPI
      val authConnector: AuthConnector = mockAuthClientConnector
      val dropoutUrl: String = "test otrs URL"
    }
    mockAllMessages
    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))

    def mockGetPpopAddressList = when(mockPrePopService.getPpobAddressList(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(Seq(scrsAddress)))
  }

  class SubmissionSetup extends Setup {
    when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any()))
      .thenReturn(Future(validBusinessContactDetails))

    when(mockPrePopService.getPpobAddressList(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(Seq(scrsAddress)))
  }

  "showing the ppob page" should {
    "return a 200" when {
      "everything is okay" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails))

        when(mockPrePopService.getPpobAddressList(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(Seq(scrsAddress)))

        callAuthorised(controller.showPPOB){
          _ isA 200
        }
      }
      "everything is okay and no address exists in prepop" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails))

        when(mockPrePopService.getPpobAddressList(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(Seq()))

        callAuthorised(controller.showPPOB){
          _ isA 200
        }
      }
      "everything is okay and no address exists in the business contact" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails.copy(ppobAddress = None)))

        when(mockPrePopService.getPpobAddressList(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(Seq(scrsAddress)))

        callAuthorised(controller.showPPOB){
          _ isA 200
        }
      }
    }
    "return a 500" when {
      "when iv is failed" in new Setup {
        mockWithCurrentProfile(Some(currentProfile.copy(ivPassed = Some(false))))

        callAuthorised(controller.showPPOB){
          _ isA 500
        }
      }
    }
    "throw an exception" when {
      "getBussinesContact Fails" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any()))
          .thenReturn(Future(throw exception))

        callAuthorised(controller.showPPOB){
          _ failedWith exception
        }
      }
      "getPpobAddressList Fails" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails.copy(ppobAddress = None)))

        when(mockPrePopService.getPpobAddressList(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(throw exception))

        callAuthorised(controller.showPPOB){
          _ failedWith exception
        }
      }
    }
  }

  "submitting the ppob page" should {
    val fakeRequest = FakeRequest(routes.BusinessContactDetailsController.showPPOB())

    "return a 400" when {
      "form is empty" in new SubmissionSetup {
        submitAuthorised(controller.submitPPOB, fakeRequest.withFormUrlEncodedBody()){
          _ isA 400
        }
      }
    }

    "return a 303" when {
      "user selects other and redirects to alf address" in new SubmissionSetup {
        when(mockAddressService.getJourneyUrl(ArgumentMatchers.any(),ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any()))
          .thenReturn(Future(Call("GET","my-redirect-url")))
        submitAuthorised(controller.submitPPOB, fakeRequest.withFormUrlEncodedBody("ppobRadio" -> "other")){
          _ redirectsTo "my-redirect-url"
        }
      }
      "user selects other and redirect to alf address" in new SubmissionSetup {
        when(mockBusinessContactService.updateBusinessContact[ScrsAddress](ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(scrsAddress))
        submitAuthorised(controller.submitPPOB, fakeRequest.withFormUrlEncodedBody("ppobRadio" -> scrsAddress.id)){
          _ redirectsTo routes.BusinessContactDetailsController.showCompanyContactDetails().url
        }
      }

      "user selects non-uk and redirect to drop out page address" in new SubmissionSetup {
        when(mockBusinessContactService.updateBusinessContact[ScrsAddress](ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(scrsAddress))
        submitAuthorised(controller.submitPPOB, fakeRequest.withFormUrlEncodedBody("ppobRadio" -> "non-uk")){
          _ redirectsTo routes.BusinessContactDetailsController.showPPOBDropOut().url
        }
      }

    }

    "return an exception" when {
      "the address selected is not on the list" in new SubmissionSetup {
        submitAuthorised(controller.submitPPOB, fakeRequest.withFormUrlEncodedBody("ppobRadio" -> "fake address")){
          _ failedWith classOf[NoSuchElementException]
        }
      }
    }
  }

  "show ppob dropout page" should {
    "return a 200" in new SubmissionSetup{
      callAuthorised(controller.showPPOBDropOut){
        _ isA 200
      }
    }
  }


  "showing the company contact details page" should {
    "return a 200" when {
      "everything is okay" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails))

        callAuthorised(controller.showCompanyContactDetails){
          _ isA 200
        }
      }

      "everything is okay and no address exists in the business contact" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails.copy(companyContactDetails = None)))

        callAuthorised(controller.showCompanyContactDetails){
          _ isA 200
        }
      }
    }
    "return a 500" when {
      "when iv is failed" in new Setup {
        mockWithCurrentProfile(Some(currentProfile.copy(ivPassed = Some(false))))

        callAuthorised(controller.showCompanyContactDetails){
          _ isA 500
        }
      }
    }
    "throw an exception" when {
      "getBussinesContact Fails" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any()))
          .thenReturn(Future(throw exception))

        callAuthorised(controller.showPPOB){
          _ failedWith exception
        }
      }
    }
  }

  "submitting the company contact details page" should {
    val fakeRequest = FakeRequest(routes.BusinessContactDetailsController.showCompanyContactDetails())

    "return a 400" when {
      "form is empty" in new SubmissionSetup {
        submitAuthorised(controller.submitCompanyContactDetails, fakeRequest.withFormUrlEncodedBody()){
          _ isA 400
        }
      }
    }

    "return a 303" when {
      "user selects other and redirect to the business activity discription" in new SubmissionSetup {
        when(mockBusinessContactService.updateBusinessContact[CompanyContactDetails](ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails.companyContactDetails.get))
        submitAuthorised(controller.submitCompanyContactDetails, fakeRequest.withFormUrlEncodedBody("email" -> "test@email.com","mobile" -> "1224456378387")){
          _ redirectsTo features.sicAndCompliance.controllers.routes.SicAndComplianceController.showBusinessActivityDescription().url
        }
      }
    }

    "return an exception" when {
      "updateBusinessContact fails" in new SubmissionSetup {
        when(mockBusinessContactService.updateBusinessContact[CompanyContactDetails](ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(throw exception))

        submitAuthorised(controller.submitCompanyContactDetails, fakeRequest.withFormUrlEncodedBody("email" -> "test@email.com","mobile" -> "1224456378387")){
          _ failedWith exception
        }
      }
    }
  }

  "return from TXM" should {
    "return a 303" when {
      "a valid id is passed" in new Setup {
        when(mockAddressService.getAddressById(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future(scrsAddress))
        when(mockBusinessContactService.updateBusinessContact[ScrsAddress](ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any()))
          .thenReturn(Future(scrsAddress))

        callAuthorised(controller.returnFromTxm(scrsAddress.id)) {
          _ redirectsTo routes.BusinessContactDetailsController.showCompanyContactDetails().url
        }
      }
    }
    "throw an exception" when {
      "getAddressById fails" in new Setup {
        when(mockAddressService.getAddressById(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future(throw exception))

        callAuthorised(controller.returnFromTxm(scrsAddress.id)) {
          _ failedWith exception
        }
      }
      "updateBusinessContact fails" in new Setup {
        when(mockAddressService.getAddressById(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future(scrsAddress))
        when(mockBusinessContactService.updateBusinessContact[ScrsAddress](ArgumentMatchers.any())(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(Future(throw exception))

        callAuthorised(controller.returnFromTxm(scrsAddress.id)) {
          _ failedWith exception
        }
      }
    }
  }
}
