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

package features.officers.controllers

import java.time.LocalDate

import connectors.KeystoreConnect
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, FutureAssertions, MockMessages, S4LMockSugar, VatRegSpec}
import models.ModelKeys.REGISTERING_OFFICER_KEY
import models.api.{DateOfBirth, Name}
import models.external.Officer
import features.officers.models.view.{LodgingOfficer, OfficerSecurityQuestionsView}
import features.officers.services.LodgingOfficerService
import play.api.test.FakeRequest
import models.S4LVatLodgingOfficer.viewModelFormat
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class OfficerSecurityQuestionsControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages with FutureAssertions {
  val mockLodgingOfficerService: LodgingOfficerService = mock[LodgingOfficerService]

  trait Setup {
    val controller: OfficerSecurityQuestionsController = new OfficerSecurityQuestionsController {
      override val lodgingOfficerService: LodgingOfficerService = mockLodgingOfficerService
      override val keystoreConnector: KeystoreConnect = mockKeystoreConnector
      override val messagesApi: MessagesApi = mockMessagesAPI
      override val authConnector: AuthConnector = mockAuthConnector
    }

    mockAllMessages
    mockWithCurrentProfile(Some(currentProfile))
  }

  val fakeRequest = FakeRequest(routes.OfficerSecurityQuestionsController.show())

  val officerSecu = OfficerSecurityQuestionsView(LocalDate.of(1998, 7, 12), "AA123456Z")
  val partialLodgingOfficer = LodgingOfficer(Some("BobBimblyBobblousBobbings"), Some(officerSecu), None, None, None, None, None)

  s"GET ${routes.OfficerSecurityQuestionsController.show()}" should {
    "return HTML and form populated" in new Setup {
      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      callAuthorised(controller.show()) {
        _ includesText "What is your date of birth"
      }
    }

    "return HTML with empty form" in new Setup {
      val emptyLodgingOfficer = LodgingOfficer(None, None, None, None, None, None, None)

      when(mockLodgingOfficerService.getLodgingOfficer(any(), any())).thenReturn(Future.successful(emptyLodgingOfficer))

      callAuthorised(controller.show()) {
        _ includesText "What is your date of birth"
      }
    }

  }

  s"POST ${routes.OfficerSecurityQuestionsController.submit()}" should {
    "return 400 with Empty data" in new Setup {
      submitAuthorised(controller.submit(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }

    "return 303 with officer in keystore" in new Setup {
      when(mockLodgingOfficerService.updateLodgingOfficer(any())(any(), any())).thenReturn(Future.successful(partialLodgingOfficer))

      submitAuthorised(controller.submit(),
        fakeRequest.withFormUrlEncodedBody("dob.day" -> "1", "dob.month" -> "1", "dob.year" -> "1980", "nino" -> testNino)
      )(_ redirectsTo s"$contextRoot/start-iv-journey")
    }
  }
}
