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

import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.vatLodgingOfficer.OfficerNinoView
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest

class OfficerNinoControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends OfficerNinoController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.vatLodgingOfficer.routes.OfficerNinoController.show())

  s"GET ${routes.OfficerNinoController.show()}" should {

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing2[OfficerNinoView]()
      val vatScheme = validVatScheme.copy(lodgingOfficer = Some(validLodgingOfficer))
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(vatScheme.pure)

      callAuthorised(Controller.show()) {
        _ includesText "What is your National Insurance number"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing2[OfficerNinoView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.copy(lodgingOfficer = None).pure)

      callAuthorised(Controller.show()) {
        _ includesText "What is your National Insurance number"
      }
    }

  }

  s"POST ${routes.OfficerNinoController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody()) { result =>
        result isA 400
      }
    }

  }

  s"POST ${routes.OfficerNinoController.submit()} with valid Nino entered" should {

    "return 303" in {
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
      save4laterExpectsSave[OfficerNinoView]()

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("nino" -> "NB686868C")) {
        _ redirectsTo s"$contextRoot/your-contact-details"
      }
    }

  }
}

