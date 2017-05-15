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

import builders.AuthBuilder
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.sicAndCompliance.financial.ActAsIntermediary
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class ActAsIntermediaryControllerSpec extends VatRegSpec with VatRegistrationFixture {

  object ActAsIntermediaryController extends ActAsIntermediaryController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.ActAsIntermediaryController.show())

  s"GET ${routes.ActAsIntermediaryController.show()}" should {

    "return HTML when there's an Act as Intermediary model in S4L" in {
      val actAsIntermediary = ActAsIntermediary(true)

      when(mockS4LService.fetchAndGet[ActAsIntermediary]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(actAsIntermediary)))

      submitAuthorised(ActAsIntermediaryController.show(), fakeRequest.withFormUrlEncodedBody(
        "actAsIntermediaryRadio" -> ""
      )) {
        _ includesText "Does the company act as an intermediary?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[ActAsIntermediary]()(Matchers.eq(S4LKey[ActAsIntermediary]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(ActAsIntermediaryController.show) {
        _ includesText "Does the company act as an intermediary?"
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[ActAsIntermediary]()
      (Matchers.eq(S4LKey[ActAsIntermediary]), any(), any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(ActAsIntermediaryController.show) {
      _ includesText "Does the company act as an intermediary?"
    }
  }

  s"POST ${routes.ActAsIntermediaryController.show()} with Empty data" should {

    "return 400" in {
      submitAuthorised(ActAsIntermediaryController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }
  }

  s"POST ${routes.ActAsIntermediaryController.submit()} with Act As Intermediary Yes selected" should {

    "return 303" in {
      val returnCacheMapActAsIntermediary = CacheMap("", Map("" -> Json.toJson(ActAsIntermediary(true))))

      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(Future.successful(()))

      when(mockS4LService.saveForm[ActAsIntermediary](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapActAsIntermediary))

      submitAuthorised(ActAsIntermediaryController.submit(), fakeRequest.withFormUrlEncodedBody(
        "actAsIntermediaryRadio" -> "true"
      ))(_ redirectsTo s"$contextRoot/company-bank-account")
    }
  }

  s"POST ${routes.ActAsIntermediaryController.submit()} with Act As Intermediary No selected" should {

    "return 303" in {
      val returnCacheMapActAsIntermediary = CacheMap("", Map("" -> Json.toJson(ActAsIntermediary(false))))

      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(Future.successful(()))

      when(mockS4LService.saveForm[ActAsIntermediary](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMapActAsIntermediary))

      submitAuthorised(ActAsIntermediaryController.submit(), fakeRequest.withFormUrlEncodedBody(
        "actAsIntermediaryRadio" -> "false"
      ))(_ redirectsTo s"$contextRoot/charges-fees-for-introducing-clients-to-financial-service-providers")

    }
  }
}