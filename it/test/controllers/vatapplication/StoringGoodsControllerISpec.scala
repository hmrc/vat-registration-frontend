/*
 * Copyright 2024 HM Revenue & Customs
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

import itFixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.api.vatapplication.{OverseasCompliance, StoringOverseas, StoringWithinUk, VatApplication}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class StoringGoodsControllerISpec extends ControllerISpec with ITRegistrationFixtures {

  val url = "/storing-goods-for-dispatch"

  val testOverseasCompliance: OverseasCompliance = OverseasCompliance(
    goodsToOverseas = Some(true),
    goodsToEu = Some(true)
  )

  val testVatApplication: VatApplication = fullVatApplication.copy(overseasCompliance = Some(testOverseasCompliance))

  "GET /storing-goods-for-dispatch" when {
    "the user is unauthorised" must {
      "return INTERNAL_SERVER_ERROR" in new Setup {
        given()
          .user.isNotAuthorised

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is authorised" must {
      "backend doesn't contain an answer" must {
        "return OK with the correct page" in new Setup {
          given()
            .user.isAuthorised()

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(url).get())

          res.status mustBe OK
        }
      }

      "backend contains an answer" must {
        "return OK and pre-populate the form if the answer is UK" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.getSection[VatApplication](Some(testVatApplication.copy(
              overseasCompliance = Some(testOverseasCompliance.copy(storingGoodsForDispatch = Some(StoringWithinUk))))
            ))

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(url).get())

          res.status mustBe OK
          Jsoup.parse(res.body).select("input[id=value]").hasAttr("checked") mustBe true
        }
      }

      "return OK and pre-populate the form if the answer is OVERSEAS" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](Some(testVatApplication.copy(
            overseasCompliance = Some(testOverseasCompliance.copy(storingGoodsForDispatch = Some(StoringOverseas))))
          ))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("input[id=value-2]").hasAttr("checked") mustBe true
      }
    }

    "POST /storing-goods-for-dispatch" when {
      "the user submits Storing Within the UK" must {
        "redirect to the dispatch from warehouse controller" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.replaceSection(testVatApplication.copy(
            overseasCompliance = Some(testOverseasCompliance.copy(storingGoodsForDispatch = Some(StoringWithinUk)))
          ))
            .registrationApi.getSection[VatApplication](Some(testVatApplication.copy(
            overseasCompliance = Some(testOverseasCompliance.copy(storingGoodsForDispatch = Some(StoringOverseas)))
          )))

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(url).post(Map("value" -> "UK")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.DispatchFromWarehouseController.show.url)
        }
      }

      "the user submits Storing Overseas" must {
        "redirect to the Task List page" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.replaceSection(testVatApplication.copy(
            overseasCompliance = Some(testOverseasCompliance.copy(storingGoodsForDispatch = Some(StoringOverseas)))
          ))
            .registrationApi.getSection[VatApplication](Some(testVatApplication.copy(
            overseasCompliance = Some(testOverseasCompliance.copy(storingGoodsForDispatch = Some(StoringOverseas)))
          )))

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(url).post(Map("value" -> "OVERSEAS")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
        }
      }

      "the user submits an invalid answer" must {
        "return BAD_REQUEST" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.replaceSection(testVatApplication.copy(
            overseasCompliance = Some(testOverseasCompliance.copy(storingGoodsForDispatch = Some(StoringWithinUk)))
          ))

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(url).post(""))

          res.status mustBe BAD_REQUEST
        }
      }
    }
  }
}
