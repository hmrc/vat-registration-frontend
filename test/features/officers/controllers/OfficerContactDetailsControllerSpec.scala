/*
 * Copyright 2017 HM Revenue & Customs
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

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.CurrentProfile
import models.view.vatLodgingOfficer.OfficerContactDetailsView
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

import scala.concurrent.Future

class OfficerContactDetailsControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends OfficerContactDetailsController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.show())

  s"GET ${controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.show()}" should {
    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[OfficerContactDetailsView]()
      when(mockVatRegistrationService.getVatScheme()(any(),any())).thenReturn(validVatScheme.pure)
      mockGetCurrentProfile()
      callAuthorised(Controller.show()) {
        _ includesText "What are your contact details?"
      }
    }


    "return HTML when there's an answer in S4L" in {
      save4laterReturnsViewModel(validOfficerContactDetailsView)()
      mockGetCurrentProfile()
      callAuthorised(Controller.show) {
        _ includesText "What are your contact details?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[OfficerContactDetailsView]()
      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(emptyVatScheme.pure)
      mockGetCurrentProfile()
      callAuthorised(Controller.show) {
        _ includesText "What are your contact details?"
      }
    }
  }

  s"POST ${controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.submit()}" should {
    "return 400 with Empty data" in {
      mockGetCurrentProfile()
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }


    "return 303 with valid Officer Contact Details entered" in {
      save4laterExpectsSave[OfficerContactDetailsView]()
      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(emptyVatScheme.pure)
      mockGetCurrentProfile()
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("email" -> "some@email.com",
                                           "daytimePhone" -> "01234 567891",
                                           "mobile" -> "01234 567891")
      )(_ redirectsTo s"$contextRoot/your-home-address")
    }
  }
}
