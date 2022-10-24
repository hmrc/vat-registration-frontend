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

package controllers.vatapplication

import itutil.ControllerISpec
import models.api.vatapplication.{OverseasCompliance, StoringWithinUk, VatApplication}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.test.Helpers._

class WarehouseNumberControllerISpec extends ControllerISpec {

  val url: String = routes.WarehouseNumberController.show.url
  val testOverseasCompliance: OverseasCompliance = OverseasCompliance(Some(true), Some(true), Some(StoringWithinUk), Some(true))

  s"GET $url" must {
    "Return OK when there is no value for 'fulfilmentWarehouseNumber' saved" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(overseasCompliance = Some(testOverseasCompliance)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("warehouseNumber").attr("value") mustBe ""
      }
    }

    "Return OK with prepop when there is a value for 'fulfilmentWarehouseNumber' in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection(Some(VatApplication(overseasCompliance =
        Some(testOverseasCompliance.copy(fulfilmentWarehouseNumber = Some(testWarehouseNumber)))
      )))
        .s4lContainer[VatApplication].isEmpty

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("warehouseNumber").attr("value") mustBe testWarehouseNumber
      }
    }

    "Return OK with prepop when there is a value for 'fulfilmentWarehouseNumber' in S4L" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(overseasCompliance =
        Some(testOverseasCompliance.copy(fulfilmentWarehouseNumber = Some(testWarehouseNumber)))
      ))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("warehouseNumber").attr("value") mustBe testWarehouseNumber
      }
    }
  }

  s"POST $url" must {
    "redirect to the fulfilment warehouse name page when the answer is a valid number" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(overseasCompliance = Some(testOverseasCompliance)))
        .s4lContainer[VatApplication].isUpdatedWith(VatApplication(overseasCompliance =
        Some(testOverseasCompliance.copy(fulfilmentWarehouseNumber = Some(testWarehouseNumber)))
      ))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Map("warehouseNumber" -> testWarehouseNumber))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.WarehouseNameController.show.url)
      }
    }

    "return a bad request when the answer is not formatted correctly" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Map("warehouseNumber" -> "1234567890123456789012345678901"))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
        Jsoup.parse(result.body).getElementById("warehouseNumber-error").text mustBe "Error: Enter a Fulfilment Warehouse number in the correct format"
      }
    }

    "return a bad request when the answer is empty" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Map("warehouseNumber" -> ""))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
        Jsoup.parse(result.body).getElementById("warehouseNumber-error").text mustBe "Error: Enter the fulfilment warehouse number"
      }
    }

    "return a bad request when the answer has invalid characters" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Map("warehouseNumber" -> "/"))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
        Jsoup.parse(result.body).getElementById("warehouseNumber-error").text mustBe "Error: Enter a number that does not contain special characters"
      }
    }
  }

}
