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

package testControllers.registration.business

import controllers.registration.business.PpobAddressController
import fixtures.VatRegistrationFixture
import models.api.Address
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.Call
import testHelpers.{ControllerSpec, FutureAssertions}

import scala.concurrent.Future

class PpobAddressControllerSpec extends ControllerSpec with VatRegistrationFixture with FutureAssertions {

  class Setup {
    val testController = new PpobAddressController(
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockBusinessContactService,
      mockAddressLookupService
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))

    when(mockBusinessContactService.getBusinessContact(any(), any(), any()))
      .thenReturn(Future(validBusinessContactDetails))
  }

  "startJourney" should {
    "redirect to ALF" in new Setup {
      when(mockAddressLookupService.getJourneyUrl(any(), any())(any()))
        .thenReturn(Future.successful(Call("GET", "TxM")))

      callAuthorised(testController.startJourney) { res =>
        status(res) mustBe SEE_OTHER
      }
    }
  }

  "callback" should {
    "return a 303" when {
      "a valid id is passed" in new Setup {
        when(mockAddressLookupService.getAddressById(any())(any()))
          .thenReturn(Future(testAddress))

        when(mockBusinessContactService.updateBusinessContact[Address](any())(any(), any(), any()))
          .thenReturn(Future(testAddress))

        callAuthorised(testController.callback(testAddress.id)) { res =>
          redirectLocation(res) mustBe Some(controllers.registration.business.routes.BusinessContactDetailsController.show().url)
        }
      }
    }
    "throw an exception" when {
      "getAddressById fails" in new Setup {
        when(mockAddressLookupService.getAddressById(any())(any())).thenReturn(Future(throw exception))

        callAuthorised(testController.callback(testAddress.id)) {
          _ failedWith exception
        }
      }
      "updateBusinessContact fails" in new Setup {
        when(mockAddressLookupService.getAddressById(any())(any())).thenReturn(Future(testAddress))
        when(mockBusinessContactService.updateBusinessContact[Address](any())(any(), any(), any())).thenReturn(Future(throw exception))

        callAuthorised(testController.callback(testAddress.id)) {
          _ failedWith exception
        }
      }
    }
  }

}
