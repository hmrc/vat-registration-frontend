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
      save4laterReturnsNoViewModel[BusinessContactDetails]()
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestBusinessContactDetailsController.show()) {
        _ includesText "Company contact details"
      }
    }

    "return HTML when there's an answer in S4L" in {
      val businessContactDetails = BusinessContactDetails(
        email = "test@foo.com",
        daytimePhone = Some("123"),
        mobile = Some("123"),
        website = None)

      save4laterReturnsViewModel(businessContactDetails)()

      callAuthorised(TestBusinessContactDetailsController.show) {
        _ includesText "Company contact details"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[BusinessContactDetails]()

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestBusinessContactDetailsController.show) {
        _ includesText "Company contact details"
      }
    }
  }

  s"POST ${controllers.vatContact.routes.BusinessContactDetailsController.submit()}" should {

    "return 400 with Empty data" in {
      submitAuthorised(TestBusinessContactDetailsController.submit(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }

    "return 303 with a valid business contact entered" in {
      save4laterExpectsSave[BusinessContactDetails]()
      when(mockVatRegistrationService.submitVatContact()(any())).thenReturn(validVatContact.pure)

      submitAuthorised(
        TestBusinessContactDetailsController.submit(),
        fakeRequest.withFormUrlEncodedBody(
          "email" -> "some@email.com",
          "daytimePhone" -> "0123456789",
          "mobile" -> "0123456789")
      )(_ redirectsTo s"$contextRoot/where-will-company-carry-out-most-of-its-business-activities")

      verify(mockVatRegistrationService).submitVatContact()(any())
    }
  }

}
