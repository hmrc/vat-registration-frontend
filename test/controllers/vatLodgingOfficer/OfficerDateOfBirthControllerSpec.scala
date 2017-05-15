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

import java.time.LocalDate

import builders.AuthBuilder
import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.{ScrsAddress, VatLodgingOfficer}
import models.view.vatLodgingOfficer.{OfficerDateOfBirthView, OfficerNinoView}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class OfficerDateOfBirthControllerSpec extends VatRegSpec with VatRegistrationFixture {

  object TestOfficerDateOfBirthController extends OfficerDateOfBirthController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.vatLodgingOfficer.routes.OfficerDateOfBirthController.show())

  s"GET ${routes.OfficerDateOfBirthController.show()}" should {

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = Some(VatLodgingOfficer.empty))

      when(mockS4LService.fetchAndGet[OfficerDateOfBirthView]()(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(vatScheme))

      callAuthorised(TestOfficerDateOfBirthController.show()) {
        _ includesText "What is your date of birth"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = None)

      when(mockS4LService.fetchAndGet[OfficerDateOfBirthView]()(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(vatScheme))

      callAuthorised(TestOfficerDateOfBirthController.show()) {
        _ includesText "What is your date of birth"
      }
    }
  }

  s"POST ${routes.OfficerDateOfBirthController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestOfficerDateOfBirthController.submit(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }
  }

  s"POST ${routes.OfficerDateOfBirthController.submit()} with valid DateOfBirth entered" should {

    "return 303" in {
      val returnOfficerDateOfBirthView = CacheMap("", Map("" -> Json.toJson(OfficerDateOfBirthView(LocalDate.of(1980,1,1)))))

      when(mockS4LService.saveForm[OfficerDateOfBirthView](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnOfficerDateOfBirthView))

      AuthBuilder.submitWithAuthorisedUser(
        TestOfficerDateOfBirthController.submit(),
        fakeRequest.withFormUrlEncodedBody("dob.day" -> "1", "dob.month" -> "1", "dob.year" -> "1980")
      )(_ redirectsTo s"$contextRoot/your-national-insurance-number")

    }
  }

}
