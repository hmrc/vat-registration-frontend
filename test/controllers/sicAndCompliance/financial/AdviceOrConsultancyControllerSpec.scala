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
import models.S4LVatSicAndCompliance
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
  }

  val fakeRequest = FakeRequest(routes.AdviceOrConsultancyController.show())

  s"GET ${routes.AdviceOrConsultancyController.show()}" should {

    "return HTML when there's a Advice Or Consultancy model in S4L" in {
      save4laterReturnsViewModel(AdviceOrConsultancy(true))()
      submitAuthorised(AdviceOrConsultancyController.show(), fakeRequest.withFormUrlEncodedBody(
        "adviceOrConsultancyRadio" -> ""
      )) {
        _ includesText "Does the company provide &#x27;advice only&#x27; or consultancy services?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing2[AdviceOrConsultancy]()
      when(mockVatRegistrationService.getVatScheme()(Matchers.any())).thenReturn(Future.successful(validVatScheme))

      callAuthorised(AdviceOrConsultancyController.show) {
        _ includesText "Does the company provide &#x27;advice only&#x27; or consultancy services?"
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    save4laterReturnsNothing2[AdviceOrConsultancy]()
    when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(AdviceOrConsultancyController.show) {
      _ includesText "Does the company provide &#x27;advice only&#x27; or consultancy services?"
    }
  }

  s"POST ${routes.AdviceOrConsultancyController.show()} with Empty data" should {

    "return 400" in {
      submitAuthorised(AdviceOrConsultancyController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) (result => result isA 400)
    }
  }

  s"POST ${routes.AdviceOrConsultancyController.submit()} with Advice Or Consultancy Yes selected" should {

    "return 303" in {
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(Future.successful(()))
      save4laterExpectsSave[S4LVatSicAndCompliance]()
      save4laterExpectsSave[AdviceOrConsultancy]()

      submitAuthorised(AdviceOrConsultancyController.submit(), fakeRequest.withFormUrlEncodedBody(
        "adviceOrConsultancyRadio" -> "true"
      )) {
        response =>
          response redirectsTo s"$contextRoot/acts-as-intermediary"
      }

    }
  }

  s"POST ${routes.AdviceOrConsultancyController.submit()} with Advice Or Consultancy No selected" should {

    "return 303" in {
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(Future.successful(()))
      save4laterExpectsSave[AdviceOrConsultancy]()
      save4laterExpectsSave[S4LVatSicAndCompliance]()

      submitAuthorised(AdviceOrConsultancyController.submit(), fakeRequest.withFormUrlEncodedBody(
        "adviceOrConsultancyRadio" -> "false"
      )) {
        response =>
          response redirectsTo s"$contextRoot/acts-as-intermediary"
      }

    }
  }
}