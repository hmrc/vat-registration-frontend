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

package controllers.sicAndCompliance.financial

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.{CurrentProfile, S4LVatSicAndCompliance}
import models.view.sicAndCompliance.financial.AdviceOrConsultancy
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class AdviceOrConsultancyControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object AdviceOrConsultancyController extends AdviceOrConsultancyController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(routes.AdviceOrConsultancyController.show())

  s"GET ${routes.AdviceOrConsultancyController.show()}" should {
    "return HTML when there's a Advice Or Consultancy model in S4L" in {
      save4laterReturnsViewModel(AdviceOrConsultancy(true))()

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      submitAuthorised(AdviceOrConsultancyController.show(), fakeRequest.withFormUrlEncodedBody(
        "adviceOrConsultancyRadio" -> ""
      )) {
        _ includesText "Does the company provide &#x27;advice only&#x27; or consultancy services?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[AdviceOrConsultancy]()
      when(mockVatRegistrationService.getVatScheme()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(validVatScheme))
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      callAuthorised(AdviceOrConsultancyController.show) {
        _ includesText "Does the company provide &#x27;advice only&#x27; or consultancy services?"
      }
    }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    save4laterReturnsNoViewModel[AdviceOrConsultancy]()
    when(mockVatRegistrationService.getVatScheme()(Matchers.any(), Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))
    when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(currentProfile)))
      callAuthorised(AdviceOrConsultancyController.show) {
        _ includesText "Does the company provide &#x27;advice only&#x27; or consultancy services?"
      }
    }
  }

  s"POST ${routes.AdviceOrConsultancyController.show()}" should {
    "return 400 with Empty data" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      submitAuthorised(AdviceOrConsultancyController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) (result => result isA 400)
    }

    "return 303 with Advice Or Consultancy Yes selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any(), any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturns(S4LVatSicAndCompliance())
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      submitAuthorised(AdviceOrConsultancyController.submit(), fakeRequest.withFormUrlEncodedBody(
        "adviceOrConsultancyRadio" -> "true"
      )) {
        response => response redirectsTo s"$contextRoot/acts-as-intermediary"
      }
    }

    "return 303 with Advice Or Consultancy No selected" in {
      when(mockVatRegistrationService.submitSicAndCompliance()(any(), any())).thenReturn(Future.successful(validSicAndCompliance))
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturns(S4LVatSicAndCompliance())
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      submitAuthorised(AdviceOrConsultancyController.submit(), fakeRequest.withFormUrlEncodedBody(
        "adviceOrConsultancyRadio" -> "false"
      )) {
        response => response redirectsTo s"$contextRoot/acts-as-intermediary"
      }
    }
  }
}