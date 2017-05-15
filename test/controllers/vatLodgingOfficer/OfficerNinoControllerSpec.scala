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

import builders.AuthBuilder
import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.{VatLodgingOfficer, VatScheme}
import models.view.vatLodgingOfficer.OfficerNinoView
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class OfficerNinoControllerSpec extends VatRegSpec with VatRegistrationFixture {

  object TestOfficerNinoController extends OfficerNinoController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.vatLodgingOfficer.routes.OfficerNinoController.show())

  s"GET ${routes.OfficerNinoController.show()}" should {

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = Some(VatLodgingOfficer.empty))

      when(mockS4LService.fetchAndGet[OfficerNinoView]()(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(vatScheme))

      callAuthorised(TestOfficerNinoController.show()) {
        _ includesText "What is your National Insurance number"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = None)

      when(mockS4LService.fetchAndGet[OfficerNinoView]()(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(vatScheme))

      callAuthorised(TestOfficerNinoController.show()) {
        _ includesText "What is your National Insurance number"
      }
    }
  }

  s"POST ${routes.OfficerNinoController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestOfficerNinoController.submit(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }
  }

  s"POST ${routes.OfficerNinoController.submit()} with valid Nino entered and default Voluntary Reg = YES" should {

    "return 303 (to /start-date)" in {
      val returnOfficerNinoView = CacheMap("", Map("" -> Json.toJson(OfficerNinoView("NB686868C"))))

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(emptyVatScheme))

      when(mockS4LService.saveForm[OfficerNinoView](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnOfficerNinoView))

      when(mockS4LService.fetchAndGet[VoluntaryRegistration]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(VoluntaryRegistration.yes)))

      AuthBuilder.submitWithAuthorisedUser(
        TestOfficerNinoController.submit(),
        fakeRequest.withFormUrlEncodedBody("nino" -> "NB686868C")
      )(_ redirectsTo s"$contextRoot/start-date")

    }
  }

  s"POST ${routes.OfficerNinoController.submit()} with valid Nino entered and default Voluntary Reg = No" should {

    "return 303 (to /start-date-confirmation)" in {
      val returnOfficerNinoView = CacheMap("", Map("" -> Json.toJson(OfficerNinoView("NB686868C"))))

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(emptyVatScheme))

      when(mockS4LService.saveForm[OfficerNinoView](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnOfficerNinoView))

      when(mockS4LService.fetchAndGet[VoluntaryRegistration]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(VoluntaryRegistration.no)))

      AuthBuilder.submitWithAuthorisedUser(
        TestOfficerNinoController.submit(),
        fakeRequest.withFormUrlEncodedBody("nino" -> "NB686868C")
      )(_ redirectsTo s"$contextRoot/start-date-confirmation")

    }
  }

  s"POST ${routes.OfficerNinoController.submit()} with valid Nino entered and no Voluntary Reg present" should {

    "return 303 (to /start-date-confirmation)" in {
      val returnOfficerNinoView = CacheMap("", Map("" -> Json.toJson(OfficerNinoView("NB686868C"))))

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(emptyVatScheme))

      when(mockS4LService.saveForm[OfficerNinoView](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnOfficerNinoView))

      when(mockS4LService.fetchAndGet[VoluntaryRegistration]()(any(), any(), any()))
        .thenReturn(Future.successful(None))

      AuthBuilder.submitWithAuthorisedUser(
        TestOfficerNinoController.submit(),
        fakeRequest.withFormUrlEncodedBody("nino" -> "NB686868C")
      )(_ redirectsTo s"$contextRoot/start-date")

    }
  }
}

