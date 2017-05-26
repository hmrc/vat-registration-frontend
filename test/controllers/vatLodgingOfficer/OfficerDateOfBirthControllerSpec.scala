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
import models.view.vatLodgingOfficer.OfficerDateOfBirthView
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest

class OfficerDateOfBirthControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends OfficerDateOfBirthController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.vatLodgingOfficer.routes.OfficerDateOfBirthController.show())

  s"GET ${routes.OfficerDateOfBirthController.show()}" should {

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = Some(validLodgingOfficer))
      save4laterReturnsNothing2[OfficerDateOfBirthView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(vatScheme.pure)

      callAuthorised(Controller.show()) {
        _ includesText "What is your date of birth"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing2[OfficerDateOfBirthView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.copy(lodgingOfficer = None).pure)

      callAuthorised(Controller.show()) {
        _ includesText "What is your date of birth"
      }
    }
  }

  s"POST ${routes.OfficerDateOfBirthController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }

  }

  s"POST ${routes.OfficerDateOfBirthController.submit()} with valid DateOfBirth entered" should {

    "return 303" in {
      save4laterExpectsSave[OfficerDateOfBirthView]()
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("dob.day" -> "1", "dob.month" -> "1", "dob.year" -> "1980")
      )(_ redirectsTo s"$contextRoot/your-national-insurance-number")

    }
  }

}
