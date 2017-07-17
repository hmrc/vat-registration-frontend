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

import controllers.vatLodgingOfficer
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.ModelKeys.FORMER_NAME
import models.view.vatLodgingOfficer.FormerNameView
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest

class FormerNameControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object TestFormerNameController extends FormerNameController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(vatLodgingOfficer.routes.FormerNameController.show())

  s"GET ${vatLodgingOfficer.routes.FormerNameController.show()}" should {

    "return HTML when there's a former name in S4L" in {
      save4laterReturnsViewModel(FormerNameView(yesNo = true, formerName = Some("Smooth Handler")))()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(TestFormerNameController.show) {
        _ includesText "Have you ever changed your name?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[FormerNameView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)

      callAuthorised(TestFormerNameController.show) {
        _ includesText "Have you ever changed your name?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[FormerNameView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

      callAuthorised(TestFormerNameController.show) {
        _ includesText "Have you ever changed your name?"
      }
    }
  }

  s"POST ${vatLodgingOfficer.routes.FormerNameController.submit()}" should {

    "return 400 with Empty data" in {
      submitAuthorised(TestFormerNameController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result => result isA 400
      }

    }

    "return 303 with valid data no former name" in {
      save4laterExpectsSave[FormerNameView]()
      mockKeystoreCache[String](FORMER_NAME, dummyCacheMap)

      submitAuthorised(TestFormerNameController.submit(), fakeRequest.withFormUrlEncodedBody(
        "formerNameRadio" -> "false"
      )) {
        _ redirectsTo s"$contextRoot/your-date-of-birth"
      }

    }

    "return 303 with valid data with former name" in {
      save4laterExpectsSave[FormerNameView]()
      mockKeystoreCache[String](FORMER_NAME, dummyCacheMap)
      submitAuthorised(TestFormerNameController.submit(), fakeRequest.withFormUrlEncodedBody(
        "formerNameRadio" -> "true",
        "formerName" -> "some name"
      )) {
        _ redirectsTo s"$contextRoot/when-change"
      }
    }
  }
}
