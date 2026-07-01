/*
 * Copyright 2026 HM Revenue & Customs
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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, getRequestedFor, urlEqualTo}
import config.FrontendAppConfig
import itutil.IntegrationSpecBase
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import play.api.http.Status.OK
import play.api.libs.json.JsArray
import play.api.test.Helpers._
import support.AppAndStubs

import scala.concurrent.ExecutionContextExecutor
import com.github.tomakehurst.wiremock.client.WireMock._

class BankHolidaysConnectorISpec
  extends IntegrationSpecBase
    with AppAndStubs
    with ScalaFutures
    with Matchers {

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  lazy val connector: BankHolidaysConnector = app.injector.instanceOf[BankHolidaysConnector]
  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val cacheId = "all_users"

  "getBankHolidaysFromApi" should {

    "return the response from the Bank Holidays API" in {

      stubGet(
        "/bank-holidays.json",
        OK,
        """
          {
            "england-and-wales": {
              "division": "england-and-wales",
              "events": [
                {
                  "title": "New Year's Day",
                  "date": "2026-01-01"
                }
              ]
            },
            "scotland": {
              "division": "scotland",
              "events": []
            },
            "northern-ireland": {
              "division": "northern-ireland",
              "events": []
            }
          }
        """
      )

      val response =
        await(connector.getBankHolidaysFromApi)

      response.status mustBe OK
      val events =
        (response.json \ "england-and-wales" \ "events").as[JsArray]

      events.value.size mustBe 1
    }

    "return a 500 response when the API returns 500" in {

      stubGet(
        "/bank-holidays.json",
        500,
        """{"message":"Internal Server Error"}"""
      )

      val response =
        await(connector.getBankHolidaysFromApi)

      response.status mustBe 500
    }

    "return an empty events list" in {

      stubGet(
        "/bank-holidays.json",
        OK,
        """
          {
            "england-and-wales": {
              "division": "england-and-wales",
              "events": []
            },
            "scotland": {
              "division": "scotland",
              "events": []
            },
            "northern-ireland": {
              "division": "northern-ireland",
              "events": []
            }
          }
        """
      )

      val response =
        await(connector.getBankHolidaysFromApi)

      response.status mustBe OK
      val events =
        (response.json \ "england-and-wales" \ "events").as[JsArray]

      events.value.size mustBe 0
    }

    "include the Accept header" in {

      stubGet(
        "/bank-holidays.json",
        OK,
        """{
          "england-and-wales":{"events":[]},
          "scotland":{"events":[]},
          "northern-ireland":{"events":[]}
        }"""
      )

      await(connector.getBankHolidaysFromApi)

      verify(
        getRequestedFor(urlEqualTo("/bank-holidays.json"))
          .withHeader(
            "Accept",
            equalTo("application/vnd.hmrc.1.0+json")
          )
      )
    }
  }
}