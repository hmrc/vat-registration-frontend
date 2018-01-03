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

package controllers.vatLodgingOfficer

import connectors.KeystoreConnect
import features.officers.models.view.LodgingOfficer
import features.officers.services.LodgingOfficerService
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, FutureAssertions, MockMessages}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import services.PrePopService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class CompletionCapacityControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages with FutureAssertions {
  val mockLodgingOfficerService: LodgingOfficerService = mock[LodgingOfficerService]
  val mockPPService: PrePopService = mock[PrePopService]

  trait Setup {
    val controller: CompletionCapacityController = new CompletionCapacityController {
      override val lodgingOfficerService: LodgingOfficerService = mockLodgingOfficerService
      override val prePopService: PrePopService = mockPPService
      override val keystoreConnector: KeystoreConnect = mockKeystoreConnector
      override val messagesApi: MessagesApi = mockMessagesAPI
      override val authConnector: AuthConnector = mockAuthConnector
    }

    mockAllMessages
    mockWithCurrentProfile(Some(currentProfile))
  }

  val fakeRequest = FakeRequest(controllers.vatLodgingOfficer.routes.CompletionCapacityController.show())

  s"GET ${routes.CompletionCapacityController.show()}" should {

    "return HTML with no data" in new Setup {
      val emptyLodgingOfficer = LodgingOfficer(None, None)

      when(mockPPService.getOfficerList(any(), any())).thenReturn(Future.successful(Seq(officer)))
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(emptyLodgingOfficer))

      callAuthorised(controller.show()) {
        _ includesText MOCKED_MESSAGE
      }
    }

    "return HTML with officer already saved" in new Setup {
      val partialLodgingOfficer = LodgingOfficer(Some("BobBimblyBobblousBobbings"), None)

      when(mockPPService.getOfficerList(any(), any())).thenReturn(Future.successful(Seq(officer)))
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      callAuthorised(controller.show()) {
        _ includesText MOCKED_MESSAGE
      }
    }
  }

  s"POST ${routes.CompletionCapacityController.submit()}" should {
    "return 400 with Empty data" in new Setup {
      when(mockPPService.getOfficerList(any(), any())).thenReturn(Future.successful(Seq(officer)))

      submitAuthorised(controller.submit(), fakeRequest.withFormUrlEncodedBody()
      )(_ isA 400)
    }

    "return 303 with selected completionCapacity" in new Setup {
      val lodgingOfficer = LodgingOfficer(
        Some(completionCapacity.name.id),
        None
      )

      when(mockLodgingOfficerService.updateCompletionCapacity(any())(any(), any())).thenReturn(Future.successful(lodgingOfficer))

      submitAuthorised(controller.submit(),
        fakeRequest.withFormUrlEncodedBody("completionCapacityRadio" -> completionCapacity.name.id)
      )(_ redirectsTo s"$contextRoot/pass-security")
    }
  }
}
