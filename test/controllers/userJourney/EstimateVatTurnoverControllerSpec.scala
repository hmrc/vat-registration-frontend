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

package controllers.userJourney

import builders.AuthBuilder
import enums.CacheKeys
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.view.EstimateVatTurnover
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.{Format, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class EstimateVatTurnoverControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object TestEstimateVatTurnoverController extends EstimateVatTurnoverController(mockS4LService, mockVatRegistrationService, ds) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.EstimateVatTurnoverController.show())

  s"GET ${routes.EstimateVatTurnoverController.show()}" should {

    "return HTML Estimate Vat Turnover page with no data in the form" in {
      when(mockS4LService.fetchAndGet[EstimateVatTurnover](Matchers.eq(CacheKeys.EstimateVatTurnover.toString))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(validEstimateVatTurnover)))

      AuthBuilder.submitWithAuthorisedUser(TestEstimateVatTurnoverController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "turnoverEstimate" -> ""
      )){

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Estimated VAT taxable turnover for the next 12 months")
      }
    }

    "return HTML when there's nothing in S4L" in {
      when(mockS4LService.fetchAndGet[EstimateVatTurnover](Matchers.eq(CacheKeys.EstimateVatTurnover.toString))
        (Matchers.any[HeaderCarrier](), Matchers.any[Format[EstimateVatTurnover]]()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestEstimateVatTurnoverController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Estimated VAT taxable turnover for the next 12 months")
      }
    }


  }


  s"POST ${routes.EstimateVatTurnoverController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestEstimateVatTurnoverController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe  Status.BAD_REQUEST
      }

    }
  }

  s"POST ${routes.EstimateVatTurnoverController.submit()} with a valid turnover estimate entered" should {

    "return 303" in {
      val returnCacheMapEstimateVatTurnover = CacheMap("", Map("" -> Json.toJson(validEstimateVatTurnover)))

      when(mockS4LService.saveForm[EstimateVatTurnover]
        (Matchers.eq(CacheKeys.EstimateVatTurnover.toString), Matchers.any())
        (Matchers.any[HeaderCarrier](), Matchers.any[Format[EstimateVatTurnover]]()))
        .thenReturn(Future.successful(returnCacheMapEstimateVatTurnover))

      AuthBuilder.submitWithAuthorisedUser(TestEstimateVatTurnoverController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "turnoverEstimate" -> "50000"
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe  "/vat-registration/zero-rated-sales"
      }

    }
  }

}
