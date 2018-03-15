/*
 * Copyright 2018 HM Revenue & Customs
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

import config.WSHttp
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse

class BusinessRegistrationConnectorSpec extends VatRegSpec with VatRegistrationFixture {
  class Setup() {
    val connector = new BusinessRegistrationConnect {
      override val businessRegistrationUrl: String = "tst-url"
      override val businessRegistrationUri: String = "tst-url"
      override val http: WSHttp = mockWSHttp
    }
  }


  val regId = "testRegId"
  val brResponseJson =
    s"""
      |{
      |   "registrationID" : "$regId"
      |}
    """.stripMargin

  "Calling getBusinessRegistrationID" should {
    "retrieve a registration ID" when {
      "there is a BR document" in new Setup() {
        mockHttpGET[HttpResponse]("tst-urltst-url", HttpResponse(OK, Some(Json.parse(brResponseJson))))

        await(connector.getBusinessRegistrationID) mustBe Some(regId)
      }
    }

    "retrieve no registration ID" when {
      "there is not a BR document" in new Setup() {
        mockHttpGET[HttpResponse]("tst-urltst-url", HttpResponse(NOT_FOUND))

        await(connector.getBusinessRegistrationID) mustBe None
      }

      "the BR document does not have a regId" in new Setup() {
        mockHttpGET[HttpResponse]("tst-urltst-url", HttpResponse(OK, Some(Json.parse("{}"))))

        await(connector.getBusinessRegistrationID) mustBe None
      }
    }
  }

}