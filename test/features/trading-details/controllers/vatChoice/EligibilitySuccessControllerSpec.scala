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

package controllers.vatTradingDetails.vatChoice

import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.CurrentProfile
import models.ModelKeys._
import models.external.IncorporationInfo
import org.mockito.Matchers
import org.mockito.Mockito.when
import play.api.test.FakeRequest

import scala.concurrent.Future

class EligibilitySuccessControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object TestEligibilitySuccessController extends EligibilitySuccessController(ds){
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(routes.EligibilitySuccessController.show())

  s"GET ${routes.EligibilitySuccessController.show()}" should {

    "return HTML when there's a eligibility success view in S4L" in {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(TestEligibilitySuccessController.show) {
        _ includesText "You can register for VAT using this service"
      }
    }

  }

  s"POST ${routes.EligibilitySuccessController.submit()}" should {
    "return 303 with valid data - Company NOT INCORPORATED" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, None)

      submitAuthorised(TestEligibilitySuccessController.submit(), fakeRequest.withFormUrlEncodedBody()) {
        _ redirectsTo s"$contextRoot/do-you-expect-taxable-turnover-to-be-more-than-threshold"
      }
    }

  }

  s"POST ${routes.EligibilitySuccessController.submit()}" should {
    "return 303 with valid data - Company INCORPORATED" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, Some(testIncorporationInfo))

      submitAuthorised(TestEligibilitySuccessController.submit(), fakeRequest.withFormUrlEncodedBody()) {
        _ redirectsTo s"$contextRoot/gone-over-threshold"
      }
    }

  }
}
