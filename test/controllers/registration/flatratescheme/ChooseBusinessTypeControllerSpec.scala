/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.registration.flatratescheme

import config.FrontendAppConfig
import fixtures.FlatRateFixtures
import models.{FlatRateScheme, MainBusinessActivityView, SicAndCompliance}
import org.jsoup.Jsoup
import org.jsoup.select.NodeFilter
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, VatRegSpec}
import views.html.choose_business_type

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ChooseBusinessTypeControllerSpec extends ControllerSpec with FlatRateFixtures {

  trait Setup {
    val view = app.injector.instanceOf[choose_business_type]
    val controller: ChooseBusinessTypeController = new ChooseBusinessTypeController(
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockConfigConnector,
      mockFlatRateService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  s"show" should {
    "return OK and render the page" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any()))
        .thenReturn(Future.successful(testFlatRate.copy(categoryOfBusiness = None)))

      when(mockConfigConnector.businessTypes).thenReturn(jsonBusinessTypes)

      callAuthorised(controller.show()) { result =>
        status(result) mustBe OK
        val document = Jsoup.parse(contentAsString(result))
        document.getElementsByAttributeValue("checked", "checked").size mustBe 0
      }
    }

    "return OK and render the page with radio pre selected" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any()))
        .thenReturn(Future.successful(testFlatRate))

      when(mockConfigConnector.businessTypes).thenReturn(jsonBusinessTypes)

      callAuthorised(controller.show()) { result =>
        status(result) mustBe OK
        val document = Jsoup.parse(contentAsString(result))
        val elements = document.getElementsByAttribute("checked")
        elements.size mustBe 1
        elements.first.`val` mustBe businessCategory
        document.getElementsByAttributeValue("for", elements.first.id).text mustBe "Test BusinessType"
      }
    }
  }

  s"submit" should {
    val fakeRequest = FakeRequest(routes.ChooseBusinessTypeController.submit())

    "return BAD_REQUEST with Empty data" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      when(mockConfigConnector.businessTypes).thenReturn(jsonBusinessTypes)

      submitAuthorised(controller.submit(), request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST with incorrect data" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "000"
      )

      when(mockConfigConnector.businessTypes).thenReturn(jsonBusinessTypes)

      submitAuthorised(controller.submit(), request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "redirect to the next page if everything is OK" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "019"
      )

      when(mockConfigConnector.businessTypes).thenReturn(jsonBusinessTypes)

      when(mockFlatRateService.saveBusinessType(any())(any(), any()))
        .thenReturn(Future.successful(testFlatRate))

      submitAuthorised(controller.submit(), request) { result =>
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.FlatRateController.yourFlatRatePage().url)
      }
    }
  }

}
