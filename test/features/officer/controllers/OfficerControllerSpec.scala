/*
 * Copyright 2018 HM Revenue & Customs
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

package features.officer.controllers

import java.time.LocalDate

import connectors.KeystoreConnect
import features.officer.models.view._
import features.officer.services.LodgingOfficerService
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, FutureAssertions, MockMessages}
import models.api.ScrsAddress
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.{AddressLookupService, PrePopService}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class OfficerControllerSpec extends ControllerSpec with FutureAwaits with DefaultAwaitTimeout
                            with VatRegistrationFixture with MockMessages with FutureAssertions {
  val mockLodgingOfficerService: LodgingOfficerService = mock[LodgingOfficerService]
  val mockPPService: PrePopService = mock[PrePopService]
  val mockAddressService = mock[AddressLookupService]

  val officerSecu = SecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA123456Z")
  val partialLodgingOfficer = LodgingOfficer(Some(CompletionCapacityView(officer.name.id, Some(officer))), Some(officerSecu), None, None, None, None, None)

  trait Setup {
    val controller: OfficerController = new OfficerController {
      override val lodgingOfficerService: LodgingOfficerService = mockLodgingOfficerService
      override val prePopService: PrePopService = mockPPService
      override val keystoreConnector: KeystoreConnect = mockKeystoreConnector
      override val messagesApi: MessagesApi = mockMessagesAPI
      override val authConnector: AuthConnector = mockAuthConnector
      override val addressLookupService: AddressLookupService = mockAddressService
    }

    mockAllMessages
    mockWithCurrentProfile(Some(currentProfile))
  }

  s"GET ${routes.OfficerController.showCompletionCapacity()}" should {
    "return HTML with no data" in new Setup {
      val emptyLodgingOfficer = LodgingOfficer(None, None, None, None, None, None, None)

      when(mockPPService.getOfficerList(any(), any())).thenReturn(Future.successful(Seq(officer)))
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(emptyLodgingOfficer))

      callAuthorised(controller.showCompletionCapacity()) {
        _ includesText MOCKED_MESSAGE
      }
    }

    "return HTML with officer already saved" in new Setup {
      val partialLodgingOfficer = LodgingOfficer(Some(CompletionCapacityView(officer.name.id, Some(officer))), None, None, None, None, None, None)

      when(mockPPService.getOfficerList(any(), any())).thenReturn(Future.successful(Seq(officer)))
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      callAuthorised(controller.showCompletionCapacity()) {
        _ includesText MOCKED_MESSAGE
      }
    }
  }

  s"POST ${routes.OfficerController.submitCompletionCapacity()}" should {
    val fakeRequest = FakeRequest(routes.OfficerController.showCompletionCapacity())

    "return 400 with Empty data" in new Setup {
      when(mockPPService.getOfficerList(any(), any())).thenReturn(Future.successful(Seq(officer)))

      submitAuthorised(controller.submitCompletionCapacity(), fakeRequest.withFormUrlEncodedBody()
      )(_ isA 400)
    }

    "return 303 with officer completion capacity saved" in new Setup {
      val lodgingOfficer = LodgingOfficer(
        Some(CompletionCapacityView(officer.name.id, Some(officer))),
        None,
        None,
        None,
        None,
        None,
        None
      )

      when(mockPPService.getOfficerList(any(), any())).thenReturn(Future.successful(Seq(officer)))

      when(mockLodgingOfficerService.saveLodgingOfficer(any())(any(), any())).thenReturn(Future.successful(lodgingOfficer))

      submitAuthorised(controller.submitCompletionCapacity(),
        fakeRequest.withFormUrlEncodedBody("completionCapacityRadio" -> officer.name.id)
      )(_ redirectsTo s"${routes.OfficerController.showSecurityQuestions.url}")
    }
  }

  s"GET ${routes.OfficerController.showSecurityQuestions()}" should {
    "return HTML and form populated" in new Setup {
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      callAuthorised(controller.showSecurityQuestions()) {
        _ includesText MOCKED_MESSAGE
      }
    }

    "return HTML with empty form" in new Setup {
      val emptyLodgingOfficer = LodgingOfficer(None, None, None, None, None, None, None)

      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(emptyLodgingOfficer))

      callAuthorised(controller.showSecurityQuestions()) {
        _ includesText MOCKED_MESSAGE
      }
    }

  }

  s"POST ${routes.OfficerController.submitSecurityQuestions()}" should {
    val fakeRequest = FakeRequest(routes.OfficerController.showSecurityQuestions())

    "return 400 with Empty data" in new Setup {
      submitAuthorised(controller.submitSecurityQuestions(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }

    "return 303 with officer security saved" in new Setup {
      when(mockLodgingOfficerService.saveLodgingOfficer(any())(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      submitAuthorised(controller.submitSecurityQuestions(),
        fakeRequest.withFormUrlEncodedBody("dob.day" -> "1", "dob.month" -> "1", "dob.year" -> "1980", "nino" -> testNino)
      )(_ redirectsTo s"${controllers.iv.routes.IdentityVerificationController.redirectToIV.url}")
    }
  }

  s"GET ${routes.OfficerController.showFormerName()}" should {
    val partialIncompleteLodgingOfficer = LodgingOfficer(
      Some(CompletionCapacityView(officer.name.id, Some(officer))),
      Some(officerSecu),
      None,
      None,
      Some(FormerNameView(true, Some("Old Name"))),
      None,
      None)

    "return 200 when there's data" in new Setup {
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialIncompleteLodgingOfficer))

      callAuthorised(controller.showFormerName()) {
        _ includesText MOCKED_MESSAGE
      }
    }

    "return 200 when there's no data" in new Setup {
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      callAuthorised(controller.showFormerName()) {
        _ includesText MOCKED_MESSAGE
      }
    }
  }

  s"POST ${routes.OfficerController.submitFormerName()}" should {
    val fakeRequest = FakeRequest(routes.OfficerController.showSecurityQuestions())

    "return 400 with Empty data" in new Setup {
      submitAuthorised(controller.submitFormerName(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }

    "return 303 with valid data no former name" in new Setup {
      when(mockLodgingOfficerService.saveLodgingOfficer(any())(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      submitAuthorised(controller.submitFormerName(), fakeRequest.withFormUrlEncodedBody(
        "formerNameRadio" -> "false"
      )) {
        _ redirectsTo s"${routes.OfficerController.showContactDetails().url}"
      }
    }

    "return 303 with valid data with former name" in new Setup {
      when(mockLodgingOfficerService.saveLodgingOfficer(any())(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      submitAuthorised(controller.submitFormerName(), fakeRequest.withFormUrlEncodedBody(
        "formerNameRadio" -> "true",
        "formerName" -> "some name"
      )) {
        _ redirectsTo s"${routes.OfficerController.showFormerNameDate.url}"
      }
    }
  }

  s"GET ${routes.OfficerController.showFormerNameDate()}" should {
    val partialIncompleteLodgingOfficerWithData = LodgingOfficer(
      Some(CompletionCapacityView(officer.name.id, Some(officer))),
      Some(officerSecu),
      None,
      None,
      Some(FormerNameView(true, Some("Old Name"))),
      Some(FormerNameDateView(LocalDate.of(2000, 6, 23))),
      None)
    val partialIncompleteLodgingOfficerNoData = LodgingOfficer(
      Some(CompletionCapacityView(officer.name.id, Some(officer))),
      Some(officerSecu),
      None,
      None,
      Some(FormerNameView(true, Some("Old Name"))),
      None,
      None)

    "throw an IllegalStateException when the former name is missing" in new Setup {
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      callAuthorised(controller.showFormerNameDate()) { f =>
        an[IllegalStateException] shouldBe thrownBy(await(f))
      }
    }

    "return 200 when there's data" in new Setup {
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialIncompleteLodgingOfficerWithData))

      callAuthorised(controller.showFormerNameDate()) {
        _ includesText MOCKED_MESSAGE
      }
    }

    "return 200 when there's no data" in new Setup {
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialIncompleteLodgingOfficerNoData))

      callAuthorised(controller.showFormerNameDate()) {
        _ includesText MOCKED_MESSAGE
      }
    }
  }

  s"POST ${routes.OfficerController.submitFormerNameDate()}" should {
    val fakeRequest = FakeRequest(routes.OfficerController.showFormerNameDate())

    "return 400 with Empty data" in new Setup {
      submitAuthorised(controller.submitFormerNameDate(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }

    "return 303 with Former name Date selected" in new Setup {
      when(mockLodgingOfficerService.saveLodgingOfficer(any())(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      submitAuthorised(controller.submitFormerNameDate(), fakeRequest.withFormUrlEncodedBody(
          "formerNameDate.day" -> "1",
          "formerNameDate.month" -> "1",
          "formerNameDate.year" -> "2017"
        )) {
        _ redirectsTo s"${routes.OfficerController.showContactDetails().url}"
      }
    }
  }

  s"GET ${routes.OfficerController.showContactDetails()}" should {
    "return 200 when there's data" in new Setup {
      val partialIncompleteLodgingOfficer = LodgingOfficer(
        Some(CompletionCapacityView(officer.name.id, Some(officer))),
        Some(officerSecu),
        None,
        Some(ContactDetailsView(Some("t@t.tt.co"))),
        Some(FormerNameView(true, Some("Old Name"))),
        None,
        None)

      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialIncompleteLodgingOfficer))

      callAuthorised(controller.showContactDetails()) {
        _ includesText MOCKED_MESSAGE
      }
    }

    "return 200 when there's no data" in new Setup {
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      callAuthorised(controller.showContactDetails()) {
        _ includesText MOCKED_MESSAGE
      }
    }
  }

  s"POST ${routes.OfficerController.submitContactDetails()}" should {
    val fakeRequest = FakeRequest(routes.OfficerController.showContactDetails())

    "return 400 with Empty data" in new Setup {
      submitAuthorised(controller.submitContactDetails(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }

    "return 303 with valid Contact Details entered" in new Setup {
      when(mockLodgingOfficerService.saveLodgingOfficer(any())(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      submitAuthorised(controller.submitContactDetails(), fakeRequest.withFormUrlEncodedBody(
        "email" -> "some@email.com",
        "daytimePhone" -> "01234 567891",
        "mobile" -> "01234 567891"
      )) {
        _ redirectsTo s"${routes.OfficerController.showHomeAddress().url}"
      }
    }
  }

  val address = ScrsAddress(line1 = "TestLine1", line2 = "TestLine1", postcode = Some("TE 1ST"))
  s"GET ${routes.OfficerController.showHomeAddress()}" should {
    "return 200 when there's data" in new Setup {
      val partialIncompleteLodgingOfficer = LodgingOfficer(
        Some(CompletionCapacityView(officer.name.id, Some(officer))),
        Some(officerSecu),
        Some(HomeAddressView(address.id, Some(address))),
        Some(ContactDetailsView(Some("t@t.tt.co"))),
        Some(FormerNameView(true, Some("Old Name"))),
        None,
        None)

      when(mockPPService.getOfficerAddressList(any())(any(), any())).thenReturn(Future.successful(Seq(address)))
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialIncompleteLodgingOfficer))

      callAuthorised(controller.showHomeAddress()) {
        _ includesText MOCKED_MESSAGE
      }
    }

    "return 200 when there's no data" in new Setup {
      when(mockPPService.getOfficerAddressList(any())(any(), any())).thenReturn(Future.successful(Seq(address)))
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      callAuthorised(controller.showHomeAddress()) {
        _ includesText MOCKED_MESSAGE
      }
    }
  }

  s"POST ${routes.OfficerController.submitHomeAddress()}" should {
    val fakeRequest = FakeRequest(routes.OfficerController.showHomeAddress())

    "return 400 with Empty data" in new Setup {
      when(mockPPService.getOfficerAddressList(any())(any(), any())).thenReturn(Future.successful(Seq(address)))

      submitAuthorised(controller.submitHomeAddress(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }

    "return 303 with valid Home address entered" in new Setup {
      when(mockLodgingOfficerService.saveLodgingOfficer(any())(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      submitAuthorised(controller.submitHomeAddress(), fakeRequest.withFormUrlEncodedBody(
        "homeAddressRadio" -> address.id
      )) {
        _ redirectsTo s"${routes.OfficerController.showPreviousAddress().url}"
      }
    }

    "redirect the user to TxM address capture page with 'other address' selected" in new Setup {
      when(mockAddressService.getJourneyUrl(any(), any())(any(), any())).thenReturn(Future.successful(Call("GET", "TxM")))

      submitAuthorised(controller.submitHomeAddress(),
        fakeRequest.withFormUrlEncodedBody("homeAddressRadio" -> "other")
      )(_ redirectsTo "TxM")
    }
  }

  s"GET ${routes.OfficerController.acceptFromTxmHomeAddress()}" should {
    "save an address and redirect to next page" in new Setup {
      when(mockLodgingOfficerService.saveLodgingOfficer(any())(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))
      when(mockAddressService.getAddressById(any())(any())).thenReturn(Future.successful(address))

      callAuthorised(controller.acceptFromTxmHomeAddress("addressId")) {
        _ redirectsTo s"${routes.OfficerController.showPreviousAddress().url}"
      }
    }
  }

  s"GET ${routes.OfficerController.showPreviousAddress()}" should {
    "return 200 when there's data" in new Setup {
      val partialIncompleteLodgingOfficer = LodgingOfficer(
        Some(CompletionCapacityView(officer.name.id, Some(officer))),
        Some(officerSecu),
        Some(HomeAddressView(address.id, Some(address))),
        Some(ContactDetailsView(Some("t@t.tt.co"))),
        Some(FormerNameView(false, None)),
        None,
        Some(PreviousAddressView(true, Some(address))))

      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialIncompleteLodgingOfficer))

      callAuthorised(controller.showPreviousAddress()) {
        _ includesText MOCKED_MESSAGE
      }
    }

    "return 200 when there's no data" in new Setup {
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      callAuthorised(controller.showPreviousAddress()) {
        _ includesText MOCKED_MESSAGE
      }
    }
  }

  s"POST ${routes.OfficerController.submitPreviousAddress()}" should {
    val fakeRequest = FakeRequest(routes.OfficerController.showPreviousAddress())

    "return 400 with Empty data" in new Setup {
      submitAuthorised(controller.submitPreviousAddress(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }

    "return 303 with Yes selected" in new Setup {
      submitAuthorised(controller.submitPreviousAddress(), fakeRequest.withFormUrlEncodedBody(
        "previousAddressQuestionRadio" -> "true"
      )) {
        _ redirectsTo s"${controllers.vatContact.ppob.routes.PpobController.show.url}"
      }
    }

    "redirect the user to TxM address capture page with No selected" in new Setup {
      when(mockAddressService.getJourneyUrl(any(), any())(any(), any())).thenReturn(Future.successful(Call("GET", "TxM")))

      submitAuthorised(controller.submitPreviousAddress(),
        fakeRequest.withFormUrlEncodedBody("previousAddressQuestionRadio" -> "false")
      )(_ redirectsTo "TxM")
    }
  }

  s"GET ${routes.OfficerController.acceptFromTxmPreviousAddress()}" should {
    "save an address and redirect to next page" in new Setup {
      when(mockLodgingOfficerService.saveLodgingOfficer(any())(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))
      when(mockAddressService.getAddressById(any())(any())).thenReturn(Future.successful(address))

      callAuthorised(controller.acceptFromTxmPreviousAddress("addressId")) {
        _ redirectsTo s"${controllers.vatContact.ppob.routes.PpobController.show.url}"
      }
    }
  }

  s"GET ${routes.OfficerController.changePreviousAddress()}" should {

    "save an address and redirect to next page" in new Setup {
      when(mockAddressService.getJourneyUrl(any(), any())(any(), any())).thenReturn(Future.successful(Call("GET", "TxM")))

      callAuthorised(controller.changePreviousAddress()) {
        _ redirectsTo "TxM"
      }
    }
  }
}
