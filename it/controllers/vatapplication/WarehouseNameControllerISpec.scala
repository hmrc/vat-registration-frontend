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

class WarehouseNameControllerISpec extends ControllerISpec {

  val url: String = routes.WarehouseNameController.show.url
  val testOverseasCompliance: OverseasCompliance = OverseasCompliance(Some(true), Some(true), Some(StoringWithinUk), Some(true), Some(testWarehouseNumber))

  s"GET $url" must {
    "Return OK when there is no value for 'fulfilmentWarehouseName' saved" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("warehouseName").attr("value") mustBe ""
      }
    }

    "Return OK with prepop when there is a value for 'fulfilmentWarehouseName' in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection(Some(VatApplication(overseasCompliance =
        Some(testOverseasCompliance.copy(fulfilmentWarehouseName = Some(testWarehouseName)))
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("warehouseName").attr("value") mustBe testWarehouseName
      }
    }

    "Return OK with prepop when there is a value for 'fulfilmentWarehouseName' in backend" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(VatApplication(overseasCompliance =
        Some(testOverseasCompliance.copy(fulfilmentWarehouseName = Some(testWarehouseName)))
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementById("warehouseName").attr("value") mustBe testWarehouseName
      }
    }
  }

  s"POST $url" must {
    "redirect to the Task List page when the answer has a name" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection[VatApplication](VatApplication(overseasCompliance =
        Some(testOverseasCompliance.copy(fulfilmentWarehouseName = Some(testWarehouseName)))
      ))
        .registrationApi.getSection[VatApplication](Some(VatApplication(overseasCompliance =
        Some(testOverseasCompliance.copy(fulfilmentWarehouseName = Some(testWarehouseName)))
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient(url).post(Map("warehouseName" -> testWarehouseName))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }

    "return a bad request when the answer is empty" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient(url).post(Map("warehouseName" -> ""))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
        Jsoup.parse(result.body).getElementById("warehouseName-error").text mustBe "Error: Enter the business name of the Fulfilment Warehouse"
      }
    }

    "return a bad request when the answer has invalid characters" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = buildClient(url).post(Map("warehouseName" -> "|"))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
        Jsoup.parse(result.body).getElementById("warehouseName-error").text mustBe "Error: Enter a business name that starts with a number or letter"
      }
    }
  }

}
