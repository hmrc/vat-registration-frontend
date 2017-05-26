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
import models.view.vatLodgingOfficer.OfficerContactDetails
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OfficerContactDetailsControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  import cats.instances.future._
  import cats.syntax.applicative._

  object TestOfficerContactDetailsController extends OfficerContactDetailsController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.show())

  s"GET ${controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.show()}" should {

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing[OfficerContactDetails]()
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestOfficerContactDetailsController.show()) {
        _ includesText "What are your contact details?"
      }
    }


    "return HTML when there's an answer in S4L" in {
      save4laterReturns(validOfficerContactDetails)

      callAuthorised(TestOfficerContactDetailsController.show) {
        _ includesText "What are your contact details?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing[OfficerContactDetails]()

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestOfficerContactDetailsController.show) {
        _ includesText "What are your contact details?"
      }
    }
  }

  s"POST ${controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(TestOfficerContactDetailsController.submit(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }
  }

  s"POST ${routes.OfficerContactDetailsController.submit()} with valid Officer Contact Details entered and default Voluntary Reg = Yes" should {

    "return 303" in {
      val returnOfficerContactDetails = CacheMap("", Map("" -> Json.toJson(validOfficerContactDetails)))
      save4laterReturns(VoluntaryRegistration.yes)
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
      when(mockS4LService.saveForm[OfficerContactDetails](any())(any(), any(), any())).thenReturn(returnOfficerContactDetails.pure)

      submitAuthorised(TestOfficerContactDetailsController.submit(),
        fakeRequest.withFormUrlEncodedBody("email" -> "some@email.com")
      )(_ redirectsTo s"$contextRoot/start-date")

    }
  }

  s"POST ${routes.OfficerContactDetailsController.submit()} with valid Officer Contact Details entered and default Voluntary Reg = No" should {

    "return 303" in {
      val returnOfficerContactDetails = CacheMap("", Map("" -> Json.toJson(OfficerContactDetails(None,None,None))))
      when(mockS4LService.saveForm[OfficerContactDetails](any())(any(), any(), any()))
        .thenReturn(returnOfficerContactDetails.pure)
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
      save4laterReturns(VoluntaryRegistration.no)

      submitAuthorised(TestOfficerContactDetailsController.submit(),
        fakeRequest.withFormUrlEncodedBody("email" -> "some@email.com")
      )(_ redirectsTo s"$contextRoot/start-date-confirmation")

    }
  }


  s"POST ${controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.submit()} with valid Officer Contact Details entered and no Voluntary Reg present" should {

    "return 303" in {
      val returnCacheMapOfficerContactDetails = CacheMap("", Map("" -> Json.toJson(validOfficerContactDetails)))

      when(mockS4LService.saveForm[OfficerContactDetails](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapOfficerContactDetails))
      save4laterReturnsNothing[VoluntaryRegistration]()

      submitAuthorised(
        TestOfficerContactDetailsController.submit(),
        fakeRequest.withFormUrlEncodedBody("email" -> "some@email.com")
      )(_ redirectsTo s"$contextRoot/start-date")

    }
  }

}
