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
import models.api.VatLodgingOfficer
import models.view.vatLodgingOfficer.OfficerNinoView
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global

class OfficerNinoControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  import cats.instances.future._
  import cats.syntax.applicative._

  object TestOfficerNinoController extends OfficerNinoController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.vatLodgingOfficer.routes.OfficerNinoController.show())

  s"GET ${routes.OfficerNinoController.show()}" should {

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = Some(VatLodgingOfficer.empty))
      save4laterReturnsNothing[OfficerNinoView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(vatScheme.pure)

      callAuthorised(TestOfficerNinoController.show()) {
        _ includesText "What is your National Insurance number"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing[OfficerNinoView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.copy(lodgingOfficer = None).pure)

      callAuthorised(TestOfficerNinoController.show()) {
        _ includesText "What is your National Insurance number"
      }
    }
  }

  s"POST ${routes.OfficerNinoController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(TestOfficerNinoController.submit(), fakeRequest.withFormUrlEncodedBody()) { result =>
        result isA 400
      }
    }

  }

  s"POST ${routes.OfficerNinoController.submit()} with valid Nino entered and default Voluntary Reg = YES" should {

    "return 303 (to /start-date)" in {
      val returnOfficerNinoView = CacheMap("", Map("" -> Json.toJson(OfficerNinoView("NB686868C"))))
      save4laterReturns(VoluntaryRegistration.yes)
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
      when(mockS4LService.save[OfficerNinoView](any())(any(), any(), any())).thenReturn(returnOfficerNinoView.pure)

      submitAuthorised(TestOfficerNinoController.submit(),
        fakeRequest.withFormUrlEncodedBody("nino" -> "NB686868C")
      )(_ redirectsTo s"$contextRoot/start-date")

    }
  }

  s"POST ${routes.OfficerNinoController.submit()} with valid Nino entered and default Voluntary Reg = No" should {

    "return 303 (to /start-date-confirmation)" in {
      val returnOfficerNinoView = CacheMap("", Map("" -> Json.toJson(OfficerNinoView("NB686868C"))))
      when(mockS4LService.save[OfficerNinoView](any())(any(), any(), any()))
        .thenReturn(returnOfficerNinoView.pure)
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
      save4laterReturns(VoluntaryRegistration.no)

      submitAuthorised(TestOfficerNinoController.submit(),
        fakeRequest.withFormUrlEncodedBody("nino" -> "NB686868C")
      )(_ redirectsTo s"$contextRoot/start-date-confirmation")

    }
  }

  s"POST ${routes.OfficerNinoController.submit()} with valid Nino entered and no Voluntary Reg present" should {

    "return 303 (to /start-date-confirmation)" in {
      val returnOfficerNinoView = CacheMap("", Map("" -> Json.toJson(OfficerNinoView("NB686868C"))))

      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
      when(mockS4LService.save[OfficerNinoView](any())(any(), any(), any())).thenReturn(returnOfficerNinoView.pure)
      save4laterReturnsNothing[VoluntaryRegistration]()

      submitAuthorised(TestOfficerNinoController.submit(), fakeRequest.withFormUrlEncodedBody("nino" -> "NB686868C")) {
        _ redirectsTo s"$contextRoot/start-date"
      }

    }
  }
}

