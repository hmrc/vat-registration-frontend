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

package controllers.vatContact

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.vatContact.BusinessContactDetails
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class BusinessContactDetailsControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object TestBusinessContactDetailsController extends BusinessContactDetailsController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(controllers.vatContact.routes.BusinessContactDetailsController.show())

  s"GET ${controllers.vatContact.routes.BusinessContactDetailsController.show()}" should {

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing2[BusinessContactDetails]()
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestBusinessContactDetailsController.show()) {
        _ includesText "Company contact details"
      }
    }

    "return HTML when there's an answer in S4L" in {
      save4laterReturns2(validBusinessContactDetails)()

      callAuthorised(TestBusinessContactDetailsController.show) {
        _ includesText "Company contact details"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing2[BusinessContactDetails]()

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestBusinessContactDetailsController.show) {
        _ includesText "Company contact details"
      }
    }
  }

  s"POST ${controllers.vatContact.routes.BusinessContactDetailsController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(TestBusinessContactDetailsController.submit(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }
  }

  s"POST ${controllers.vatContact.routes.BusinessContactDetailsController.submit()} with a valid business contact entered" should {

    "return 303" in {
      save4laterExpectsSave[BusinessContactDetails]()
      when(mockVatRegistrationService.submitVatContact()(any())).thenReturn(validVatContact.pure)

      submitAuthorised(
        TestBusinessContactDetailsController.submit(),
        fakeRequest.withFormUrlEncodedBody("email" -> "some@email.com", "mobile" -> "0123456789")
      )(_ redirectsTo s"$contextRoot/describe-what-company-does")

      verify(mockVatRegistrationService).submitVatContact()(any())
    }
  }

}
