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

import builders.AuthBuilder
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class VoluntaryRegistrationReasonControllerSpec extends VatRegSpec with VatRegistrationFixture {
  implicit val hc = HeaderCarrier()
  val mockVatRegistrationService = mock[VatRegistrationService]

  object TestVoluntaryRegistrationReasonController extends VoluntaryRegistrationReasonController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.VoluntaryRegistrationReasonController.show())

  s"GET ${routes.VoluntaryRegistrationReasonController.show()}" should {

    "return HTML Voluntary Registration Reason page with no Selection" in {
      val voluntaryRegistrationReason = VoluntaryRegistrationReason("")

      when(mockS4LService.fetchAndGet[VoluntaryRegistrationReason]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(voluntaryRegistrationReason)))

      AuthBuilder.submitWithAuthorisedUser(TestVoluntaryRegistrationReasonController.show(), fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationReasonRadio" -> ""
      )) {
        _ includesText "Which one of the following apply to the company?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[VoluntaryRegistrationReason]()(Matchers.eq(S4LKey[VoluntaryRegistrationReason]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestVoluntaryRegistrationReasonController.show) {
        _ includesText "Which one of the following apply to the company?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      when(mockS4LService.fetchAndGet[VoluntaryRegistrationReason]()(Matchers.eq(S4LKey[VoluntaryRegistrationReason]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestVoluntaryRegistrationReasonController.show) {
        _ includesText "Which one of the following apply to the company?"
      }
    }
  }

  s"POST ${routes.VoluntaryRegistrationReasonController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestVoluntaryRegistrationReasonController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }
  }

  s"POST ${routes.VoluntaryRegistrationReasonController.submit()} with Voluntary Registration Reason selected Sells" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(VoluntaryRegistrationReason.sells)))

      when(mockS4LService.saveForm[VoluntaryRegistrationReason](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMap))

      AuthBuilder.submitWithAuthorisedUser(TestVoluntaryRegistrationReasonController.submit(), fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationReasonRadio" -> VoluntaryRegistrationReason.SELLS
      ))(_ redirectsTo s"$contextRoot/start-date")
    }
  }

  s"POST ${routes.VoluntaryRegistrationReasonController.submit()} with Voluntary Registration Reason selected Intends to sell" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(VoluntaryRegistrationReason.intendsToSell)))

      when(mockS4LService.saveForm[VoluntaryRegistrationReason](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMap))

      AuthBuilder.submitWithAuthorisedUser(TestVoluntaryRegistrationReasonController.submit(), fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationReasonRadio" -> VoluntaryRegistrationReason.SELLS
      ))(_ redirectsTo s"$contextRoot/start-date")
    }
  }

  s"POST ${routes.VoluntaryRegistrationReasonController.submit()} with Voluntary Registration selected No" should {

    "redirect to the welcome page" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(VoluntaryRegistrationReason.neither)))

      when(mockS4LService.clear()(any[HeaderCarrier]())).thenReturn(Future.successful(validHttpResponse))
      when(mockS4LService.saveForm[VoluntaryRegistrationReason](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMap))
      when(mockVatRegistrationService.deleteVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(()))

      AuthBuilder.submitWithAuthorisedUser(TestVoluntaryRegistrationReasonController.submit(), fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationReasonRadio" -> VoluntaryRegistrationReason.NEITHER
      ))(_ redirectsTo contextRoot)
    }
  }

}
