/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.Returns
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import support.AppAndStubs

import scala.concurrent.Future

class ZeroRatedSuppliesControllerISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val url: String = controllers.routes.ZeroRatedSuppliesController.show().url

  s"GET $url" must {
    "return an OK if turnoverEstimates are found" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, None))
        .vatScheme.has("turnover-estimates-data", Json.toJson(turnOverEstimates))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "return an OK if turnoverEstimates are found and there is data to prepop" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(Some(10000), None, None, None, None))
        .vatScheme.has("turnover-estimates-data", Json.toJson(turnOverEstimates))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "return an INTERNAL_SERVER_ERROR if turnoverEstimates aren't found" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, None))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  s"POST $url" must {
    "redirect to charge expectancy if turnoverEstimates exists and form has no errors" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, None))
        .s4lContainer[Returns].isUpdatedWith(Returns(Some(10000.54), None, None, None, None))
        .vatScheme.has("turnover-estimates-data", Json.toJson(turnOverEstimates))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Json.obj(
        "zeroRatedSupplies" -> "10,000.535"
      ))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ReturnsController.chargeExpectancyPage().url)
      }
    }

    "update the page with errors if turnoverEstimates exists and form has errors" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, None))
        .vatScheme.has("turnover-estimates-data", Json.toJson(turnOverEstimates))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Json.obj(
        "zeroRatedSupplies" -> "text"
      ))

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }

    "return an INTERNAL_SERVER_ERROR if turnoverEstimates doesn't exist" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, None))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Json.obj(
        "zeroRatedSupplies" -> "10,000.53"
      ))

      whenReady(res) { result =>
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
