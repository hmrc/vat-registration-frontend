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

import itutil.ControllerISpec
import models.api.returns.{OverseasCompliance, Returns, StoringWithinUk}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class WarehouseNameControllerISpec extends ControllerISpec {

  val url: String = routes.WarehouseNameController.show.url
  val testOverseasCompliance: OverseasCompliance = OverseasCompliance(Some(true), Some(true), Some(StoringWithinUk), Some(true), Some(testWarehouseNumber))

  s"GET $url" must {
    "Return OK when there is no value for 'fulfilmentWarehouseName' saved" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(overseasCompliance = Some(testOverseasCompliance)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("warehouseName").attr("value") mustBe ""
      }
    }

    "Return OK with prepop when there is a value for 'fulfilmentWarehouseName' in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .vatScheme.has("returns", Json.toJson(Returns(overseasCompliance =
        Some(testOverseasCompliance.copy(fulfilmentWarehouseName = Some(testWarehouseName)))
      )))
        .s4lContainer[Returns].isEmpty

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("warehouseName").attr("value") mustBe testWarehouseName
      }
    }

    "Return OK with prepop when there is a value for 'fulfilmentWarehouseName' in S4L" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(overseasCompliance =
        Some(testOverseasCompliance.copy(fulfilmentWarehouseName = Some(testWarehouseName)))
      ))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("warehouseName").attr("value") mustBe testWarehouseName
      }
    }
  }

  s"POST $url" must {
    "redirect to the Northern Ireland Protocol page when the answer has a name" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(overseasCompliance = Some(testOverseasCompliance)))
        .s4lContainer[Returns].isUpdatedWith(Returns(overseasCompliance =
        Some(testOverseasCompliance.copy(fulfilmentWarehouseNumber = Some(testWarehouseName)))
      ))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Json.obj("warehouseName" -> testWarehouseName))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SellOrMoveNipController.show.url)
      }
    }

    "return a bad request when the answer is empty" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Json.obj("warehouseName" -> ""))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
        Jsoup.parse(result.body).getElementById("warehouseName-error").text mustBe "Error: Enter the business name of the Fulfilment Warehouse"
      }
    }

    "return a bad request when the answer has invalid characters" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Json.obj("warehouseName" -> "|"))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
        Jsoup.parse(result.body).getElementById("warehouseName-error").text mustBe "Error: Enter a business name that starts with a number or letter"
      }
    }
  }

}
