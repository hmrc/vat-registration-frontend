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

package controllers.registration.returns

import featureswitch.core.config.NorthernIrelandProtocol
import itutil.ControllerISpec
import models.api.returns.{OverseasCompliance, Returns, StoringWithinUk}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class DispatchFromWarehouseControllerISpec extends ControllerISpec {

  val url: String = routes.DispatchFromWarehouseController.show.url
  val testOverseasCompliance: OverseasCompliance = OverseasCompliance(Some(true), Some(true), Some(StoringWithinUk))

  s"GET $url" must {
    "Return OK when there is no value for 'usingWarehouse' in the backend" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(overseasCompliance = Some(testOverseasCompliance)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "Return OK with prepop when there is a value for 'usingWarehouse' in the backend" in new Setup {
      given()
        .user.isAuthorised
        .vatScheme.has("returns", Json.toJson(Returns(overseasCompliance = Some(testOverseasCompliance.copy(usingWarehouse = Some(false))))))
        .s4lContainer[Returns].isEmpty

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "No"
      }
    }

    "Return OK with prepop when there is a value for 'usingWarehouse' in S4L" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(overseasCompliance = Some(testOverseasCompliance.copy(usingWarehouse = Some(true)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "Yes"
      }
    }
  }

  s"POST $url" must {
    "redirect to the fulfilment warehouse number page when the answer is yes" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(overseasCompliance = Some(testOverseasCompliance)))
        .s4lContainer[Returns].isUpdatedWith(Returns(overseasCompliance = Some(testOverseasCompliance.copy(usingWarehouse = Some(true)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.WarehouseNumberController.show.url)
      }
    }

    "redirect to the returns frequency page when the answer is no" in new Setup {
      disable(NorthernIrelandProtocol)
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(overseasCompliance = Some(testOverseasCompliance)))
        .s4lContainer[Returns].isUpdatedWith(Returns(overseasCompliance = Some(testOverseasCompliance.copy(usingWarehouse = Some(false)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Json.obj("value" -> "false"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.ReturnsController.returnsFrequencyPage.url)
      }
    }

    "redirect to the Northern Ireland Protocol page when the answer is no" in new Setup {
      enable(NorthernIrelandProtocol)
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(overseasCompliance = Some(testOverseasCompliance)))
        .s4lContainer[Returns].isUpdatedWith(Returns(overseasCompliance = Some(testOverseasCompliance.copy(usingWarehouse = Some(false)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Json.obj("value" -> "false"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SellOrMoveNipController.show.url)
      }
    }

    "return a bad request when the answer is invalid" in new Setup {
      given()
        .user.isAuthorised

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Json.obj("value" -> ""))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
        Jsoup.parse(result.body).getElementById("value-error").text mustBe "Error: Select yes if the business will dispatch goods from a Fulfilment House Due Diligence Scheme (FHDDS) registered warehouse"
      }
    }
  }

}
